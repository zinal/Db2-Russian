// RefreshHiveZookeeper.groovy
// Данный скрипт автоматически читает параметры подключения Hive из ZooKeeper и Db2,
// сравнивает их и применяет изменения к соответствующему псевдониму Db2.

import groovy.sql.Sql
import java.util.concurrent.CountDownLatch
import java.nio.charset.StandardCharsets
import org.apache.zookeeper.*
import static org.apache.zookeeper.Watcher.Event.KeeperState

// Установить соединение с ZooKeeper
ZooKeeper zookeeper(Properties props) {
    final String host = props.getProperty("zk.host", "localhost")
    final CountDownLatch connectionLatch = new CountDownLatch(1);
    final ZooKeeper zoo = new ZooKeeper(host, 2000, new Watcher() {
        public void process(WatchedEvent we) {
            if (we.getState() == KeeperState.SyncConnected) {
                connectionLatch.countDown();
            }
        }
    });
    connectionLatch.await();
    return zoo;
}

// Разобрать строчку параметров подключения Hive из ZooKeeper
Map<String, String> parseHiveParams(String hiveParams) {
    final String[] items = hiveParams.split('[;]')
    final Map<String, String> ret = new HashMap<>()
    for (String i : items) {
        String[] pair = i.split('[=]')
        if (pair.length > 1)
            ret.put(pair[0], pair[1])
    }
    return ret
}


// Запуск основного скрипта с выводом номера версии
println "RefreshHiveZookeeper.groovy v0.1 2021-09-20"


// Прочитать конфигурационный файл
final Properties props = new Properties()
new File("RefreshHiveZookeeper.xml").withInputStream {
    props.loadFromXML(it)
}

// Установить значения системных настроек
for (String name : props.keySet()) {
    if (name.length() > 4 && name.startsWith("SYS.")) {
        System.setProperty(name.substring(4), props.getProperty(name))
    }
}

String hiveParams = null;

println "Connecting to ZooKeeper..."
zookeeper(props).withCloseable { zk ->
    println "Reading ZooKeeper data..."
    String ns = props.getProperty("zk.ns", "hiveserver2")
    if (!ns.startsWith("/")) {
        ns = "/" + ns
    }
    List<String> children = new ArrayList<>(
        zk.getChildren(ns, false)
    )
    Collections.sort(children) // желательна стабильность
    for (String child : children) {
        byte[] chData = zk.getData(ns + "/" + child, false, null)
        if (chData!=null && chData.length > 0) {
            hiveParams = new String(chData, StandardCharsets.UTF_8)
            break
        }
    }
}

if (hiveParams==null) {
    println "ZooKeeper does not contain any Hive reference"
    System.exit(1)
}

final Map<String, String> hiveConn = parseHiveParams(hiveParams)
println "Hive parameters retrieved from ZooKeeper: " + hiveConn.toString()

final String serverName = props.getProperty("db2.server")

println "Connecting to Db2..."
Sql.withInstance(
    props.getProperty("db2.url"),
    props.getProperty("db2.username"),
    props.getProperty("db2.password"),
    "com.ibm.db2.jcc.DB2Driver"
) { sql ->

    println "Connection established, retrieving SERVER details..."
    String curHost = null;
    String curPort = null;
    String curPrinc = null;
    def text = '''
SELECT h.setting AS xhost, p.setting AS xport, x.setting AS xprinc
FROM syscat.serveroptions h, syscat.serveroptions p, syscat.serveroptions x
WHERE h.option='HOST' AND p.option='PORT' AND x.option='SERVER_PRINCIPAL_NAME'
  AND h.servername=:serv AND h.servername=p.servername AND h.servername=x.servername
    '''
    sql.eachRow(text, [serv: serverName]) { row ->
        curHost = row.getString(1)
        curPort = row.getString(2)
        curPrinc = row.getString(3)
    }

    if (curHost==null || curPort==null) {
        println "Hive SERVER object does not exist, terminating..."
        System.exit(1)
    }

    String trueHost = hiveConn.get("hive.server2.thrift.bind.host")
    String truePort = hiveConn.get("hive.server2.thrift.port")
    String truePrinc = hiveConn.get("hive.server2.authentication.kerberos.principal")
    truePrinc = truePrinc.replace('_HOST', trueHost)

    println "Current SERVER object refers to " + curHost + " at " + curPort
    println "True SERVER object refers to " + trueHost + " at " + truePort

    boolean changes = true
    if (trueHost!=null && trueHost.equals(curHost)
        && truePort!=null && truePort.equals(curPort)
        && truePrinc!=null && truePrinc.equals(curPrinc)
    ) {
        changes = false
        println("No changes, nothing to update.")
    }

    if (changes) {
        final StringBuilder sqlAlter = new StringBuilder();
        sqlAlter.append("ALTER SERVER ").append(serverName).append(" OPTIONS(");
        boolean needComma = false;
        if (trueHost!=null && !trueHost.equals(curHost)) {
            if (needComma) sqlAlter.append(", "); else needComma = true;
            sqlAlter.append("SET HOST '").append(trueHost).append("'")
        }
        if (truePort!=null && !truePort.equals(curPort)) {
            if (needComma) sqlAlter.append(", "); else needComma = true;
            sqlAlter.append("SET PORT '").append(truePort).append("'")
        }
        if (truePrinc!=null && !truePrinc.equals(curPrinc)) {
            if (needComma) sqlAlter.append(", "); else needComma = true;
            sqlAlter.append("SET SERVER_PRINCIPAL_NAME '").append(truePrinc).append("'")
        }
        sqlAlter.append(")")

        println "SQL: " + sqlAlter.toString()
        sql.execute(sqlAlter.toString())

        println("Changes applied.")
    }
}

// End Of File
