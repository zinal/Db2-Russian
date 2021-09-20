// RefreshHiveZookeeper.groovy

import org.apache.zookeeper.*
import java.util.concurrent.CountDownLatch

// Прочитать конфигурационный файл
Properties props = new Properties()
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
    final String host = props.getProperty("zk.host")
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

println "** Connecting to ZooKeeper..."
zookeeper(props).withCloseable { zk ->
    println "** Reading ZooKeeper data..."
}

// End Of File
