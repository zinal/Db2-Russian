// RefreshHiveZookeeper.groovy

import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.WatchedEvent
import java.util.concurrent.CountDownLatch

public ZooKeeper connect(String host) {
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

println "RefreshHiveZookeeper.groovy v0.1 2021-09-20"

// End Of File
