// RefreshHiveZookeeper.groovy

import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.WatchedEvent
import java.util.concurrent.CountDownLatch

// Установить соединение с ZooKeeper
ZooKeeper connect(String host) {
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

// Установить значения системных настроек
void processSystemProperties(Properties props) {
    for (String name : props.keySet()) {
        if (name.length() > 4 && name.startsWith("SYS.")) {
            System.setProperty(name.substring(4), props.getProperty(name))
        }
    }
}

// Запуск основного скрипта с выводом номера версии
println "RefreshHiveZookeeper.groovy v0.1 2021-09-20"

Properties props = new Properties()
new File("RefreshHiveZookeeper.xml").withInputStream {
    props.loadFromXML(it)
}

processSystemProperties(props)



// End Of File
