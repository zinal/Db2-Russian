// RefreshHiveZookeeper.groovy

import org.apache.zookeeper.*
import static org.apache.zookeeper.Watcher.Event.KeeperState
import java.util.concurrent.CountDownLatch
import java.nio.charset.StandardCharsets

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

// Запуск основного скрипта с выводом номера версии
println "RefreshHiveZookeeper.groovy v0.1 2021-09-20"

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
    Collections.sort(children)
    for (String child : children) {
        byte[] chData = zk.getData(ns + "/" + child, false, null)
        if (chData!=null && chData.length > 0) {
            hiveParams = new String(chData, StandardCharsets.UTF_8)
        }
    }
}

if (hiveParams==null) {
    println "ZooKeeper does not contain current Hive reference"
    System.exit(0)
}

println "Hive parameters: " + hiveParams

// End Of File
