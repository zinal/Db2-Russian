// RefreshHiveZookeeper.groovy

import org.apache.zookeeper.*
import static org.apache.zookeeper.Watcher.Event.KeeperState
import java.util.concurrent.CountDownLatch
import java.nio.charset.StandardCharsets

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

// Установить соединение с Db2
java.sql.Connection connectDb2(Properties props) {
    String url = props.getProperty("db2.url")
    if (url==null || url.length()==0) {
        return null
    }
    String username = props.getProperty("db2.username")
    String password = props.getProperty("db2.password")
    if (username==null || username.length()==0)
        return java.sql.DriverManager.getConnection(url)
    return java.sql.DriverManager.getConnection(url, username, password)
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
    System.exit(0)
}

Map<String, String> hiveConn = parseHiveParams(hiveParams)
println "Hive parameters retrieved from ZooKeeper: " + hiveConn.toString()

println "Connecting to Db2..."

Class.forName("com.ibm.db2.jcc.DB2Driver")
connectDb2(props).withCloseable { con ->
    println "Connection established, retrieving SERVER details..."
}

// End Of File
