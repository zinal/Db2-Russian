# Пример команд по настройке Db2 HADR + Tivoli SA MP на двух серверах CentOS 7

## Описание конфигурации
Имена узлов: `alpha` (адрес `192.168.122.202`) и `beta` (адрес `192.168.122.204`).

Оба сервера видят "стабильный" внешний сетевой адрес `192.168.122.1` (в данном случае - основной маршрутизатор сети).

База данных `hatest` первоначально создана на сервере `alpha`.

## Блоки команд по настройке

### Установка обязательных пакетов

```bash
# Db2 requirements
yum install -y ksh libaio pam.i686 libstdc++ compat-libstdc++-33
yum install -y libstdc++.i686 compat-libstdc++-33.i686
# Graphical installation environment
yum install -y tigervnc-server xterm
# Java fonts bug - workaround (necessary for graphical install)
yum install -y dejavu-serif-fonts
# RSCT specifics
yum install -y net-tools perl-Sys-Syslog
```

### Первичная настройка исходной базы данных

```bash
db2 update db cfg for hatest using logprimary 4
db2 update db cfg for hatest using logsecond 60
db2 update db cfg for hatest using logfilsiz 8192
db2 update db cfg for hatest using logindexbuild on
db2 update db cfg for hatest using blocknonlogged on
mkdir /home/db2inst1/alogs
db2 update db cfg for hatest using logarchmeth1 disk:/home/db2inst1/alogs
db2 update db cfg for hatest using logarchcompr1 on
db2 update db cfg for hatest using trackmod on
```

### Активация HADR на узле "alpha"

```bash
db2 update db cfg for hatest using hadr_syncmode nearsync
db2 update db cfg for hatest using hadr_local_host 192.168.64.128
db2 update db cfg for hatest using hadr_local_svc 52001
db2 update db cfg for hatest using hadr_target_list 192.168.64.129:52001
db2 update db cfg for hatest using hadr_remote_host 192.168.64.129
db2 update db cfg for hatest using hadr_remote_svc 52001
db2 update db cfg for hatest using hadr_remote_inst db2inst1
db2 update db cfg for hatest using hadr_peer_window 7200
```

### Активация HADR на узле "beta"

```bash
db2 update db cfg for hatest using hadr_syncmode nearsync
db2 update db cfg for hatest using hadr_local_host 192.168.64.129
db2 update db cfg for hatest using hadr_local_svc 52001
db2 update db cfg for hatest using hadr_target_list 192.168.64.128:52001
db2 update db cfg for hatest using hadr_remote_host 192.168.64.128
db2 update db cfg for hatest using hadr_remote_svc 52001
db2 update db cfg for hatest using hadr_remote_inst db2inst1
db2 update db cfg for hatest using hadr_peer_window 7200
```

## Сценарий действий с выводом команд

### Этап 1. Операции на узле "alpha"

```
[db2inst1@alpha ~]$ cat /etc/centos-release
CentOS Linux release 7.5.1804 (Core) 
[db2inst1@alpha ~]$ 
[db2inst1@alpha ~]$ db2level
DB21085I  This instance or install (instance name, where applicable: 
"db2inst1") uses "64" bits and DB2 code release "SQL11013" with level 
identifier "0204010F".
Informational tokens are "DB2 v11.1.3.3", "s1807091300", "DYN1807091300AMD64", 
and Fix Pack "3b".
Product is installed at "/opt/ibm/db2/V11.1".

[db2inst1@alpha ~]$ 
[db2inst1@alpha ~]$ db2 update db cfg for hatest using logprimary 4
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using logsecond 60
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using logfilsiz 8192
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using logindexbuild on
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using blocknonlogged on
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ mkdir /home/db2inst1/alogs
[db2inst1@alpha ~]$ db2 update db cfg for hatest using logarchmeth1 disk:/home/db2inst1/alogs
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using logarchcompr1 on
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using trackmod on
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ 
[db2inst1@alpha ~]$ db2 backup db hatest compress

Backup successful. The timestamp for this backup image is : 20181012140633

[db2inst1@alpha ~]$ scp HATEST.0.db2inst1.DBPART000.20181012140633.001 beta:/home/db2inst1/
db2inst1@beta's password: 
HATEST.0.db2inst1.DBPART000.20181012140633.001         100%   48MB  56.7MB/s   00:00    
[db2inst1@alpha ~]$ 
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_syncmode nearsync
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_local_host alpha
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_local_svc 52001
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_target_list beta:52001
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_remote_host beta
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_remote_svc 52001
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_remote_inst db2inst1
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@alpha ~]$ db2 update db cfg for hatest using hadr_peer_window 7200
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
```

### Этап 2. Операции на узле "beta"

```
[db2inst1@beta ~]$ 
[db2inst1@beta ~]$ cat /etc/redhat-release 
CentOS Linux release 7.5.1804 (Core) 
[db2inst1@beta ~]$ 
[db2inst1@beta ~]$ db2level
DB21085I  This instance or install (instance name, where applicable: 
"db2inst1") uses "64" bits and DB2 code release "SQL11013" with level 
identifier "0204010F".
Informational tokens are "DB2 v11.1.3.3", "s1807091300", "DYN1807091300AMD64", 
and Fix Pack "3b".
Product is installed at "/opt/ibm/db2/V11.1".

[db2inst1@beta ~]$ 
[db2inst1@beta ~]$ db2 restore db hatest
DB20000I  The RESTORE DATABASE command completed successfully.
[db2inst1@beta ~]$ 
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_syncmode nearsync
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_local_host beta
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_local_svc 52001
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_target_list alpha:52001
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_remote_host alpha
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_remote_svc 52001
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_remote_inst db2inst1
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ db2 update db cfg for hatest using hadr_peer_window 7200
DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
[db2inst1@beta ~]$ 
[db2inst1@beta ~]$ db2 start hadr on db hatest as standby
DB20000I  The START HADR ON DATABASE command completed successfully.
[db2inst1@beta ~]$ 
```

### Этап 3. Операции на узле "alpha"

```
[db2inst1@alpha ~]$ db2 start hadr on db hatest as primary
DB20000I  The START HADR ON DATABASE command completed successfully.
[db2inst1@alpha ~]$ 
[db2inst1@alpha ~]$ 
[db2inst1@alpha ~]$ db2pd -db hatest -hadr

Database Member 0 -- Database HATEST -- Active -- Up 0 days 00:00:47 -- Date 2018-10-12-14.38.48.516301

                            HADR_ROLE = PRIMARY
                          REPLAY_TYPE = PHYSICAL
                        HADR_SYNCMODE = NEARSYNC
                           STANDBY_ID = 1
                        LOG_STREAM_ID = 0
                           HADR_STATE = PEER
                           HADR_FLAGS = TCP_PROTOCOL
                  PRIMARY_MEMBER_HOST = alpha
                     PRIMARY_INSTANCE = db2inst1
                       PRIMARY_MEMBER = 0
                  STANDBY_MEMBER_HOST = beta
                     STANDBY_INSTANCE = db2inst1
                       STANDBY_MEMBER = 0
                  HADR_CONNECT_STATUS = CONNECTED
             HADR_CONNECT_STATUS_TIME = 12.10.2018 14:38:08.341443 (1539344288)
          HEARTBEAT_INTERVAL(seconds) = 30
                     HEARTBEAT_MISSED = 0
                   HEARTBEAT_EXPECTED = 1
                HADR_TIMEOUT(seconds) = 120
        TIME_SINCE_LAST_RECV(seconds) = 10
             PEER_WAIT_LIMIT(seconds) = 0
           LOG_HADR_WAIT_CUR(seconds) = 0,000
    LOG_HADR_WAIT_RECENT_AVG(seconds) = 0,000000
   LOG_HADR_WAIT_ACCUMULATED(seconds) = 0,000
                  LOG_HADR_WAIT_COUNT = 0
SOCK_SEND_BUF_REQUESTED,ACTUAL(bytes) = 0, 87040
SOCK_RECV_BUF_REQUESTED,ACTUAL(bytes) = 0, 369280
            PRIMARY_LOG_FILE,PAGE,POS = S0000000.LOG, 0, 44836001
            STANDBY_LOG_FILE,PAGE,POS = S0000000.LOG, 0, 44836001
                  HADR_LOG_GAP(bytes) = 0
     STANDBY_REPLAY_LOG_FILE,PAGE,POS = S0000000.LOG, 0, 44836001
       STANDBY_RECV_REPLAY_GAP(bytes) = 4055985
                     PRIMARY_LOG_TIME = 12.10.2018 14:06:39.000000 (1539342399)
                     STANDBY_LOG_TIME = 12.10.2018 14:06:39.000000 (1539342399)
              STANDBY_REPLAY_LOG_TIME = 12.10.2018 14:06:39.000000 (1539342399)
         STANDBY_RECV_BUF_SIZE(pages) = 4300
             STANDBY_RECV_BUF_PERCENT = 0
           STANDBY_SPOOL_LIMIT(pages) = 524288
                STANDBY_SPOOL_PERCENT = 0
                   STANDBY_ERROR_TIME = NULL
                 PEER_WINDOW(seconds) = 7200
                      PEER_WINDOW_END = 12.10.2018 16:38:39.000000 (1539351519)
             READS_ON_STANDBY_ENABLED = N

[db2inst1@alpha ~]$ 
```

### Этап 4. Установка Tivoli SA MP на узле "alpha"

```
[root@alpha universal]# LANG=C ./db2/linuxamd64/tsamp/installSAM --noprereqcheck
installSAM: WARNING: prerequisite check was bypassed.
SAM is currently not installed.
...
Subsystem         Group            PID     Status 
 ctrmc            rsct             19590   active
installSAM: Warning: Must set CT_MANAGEMENT_SCOPE=2 

installSAM: All packages were installed successfully.
[root@alpha universal]# 

[root@alpha ~]# LANG=C /opt/ibm/db2/V11.1/install/tsamp/db2cptsa 
DBI1099I  The DB2 High Availability (HA) scripts for the IBM Tivoli
      System Automation for Multiplatforms (SA MP) were successfully
      installed in /usr/sbin/rsct/sapolicies/db2.
...
[root@alpha ~]# cd /home/Ditto/universal/db2/linuxamd64/tsamp/efix/
[root@alpha efix]# ./install.sh 
IBM Tivoli System Automation for Multiplatforms - efix installer v1.16
Installing efix DB2-4103-efix5 for RSCT 3.1.5.12 and SAMP 4.1.0.3 on x86_linux_2
=> /lib/libct_ffdc.so
=> /lib/libsrc.so
=> /usr/sbin/rsct/trctmplts/_CFD.tpl
=> /usr/sbin/rsct/bin/IBM.ConfigRMd
=> /usr/lib/libribme.so
=> /usr/lib/libhsalock.so
=> /usr/sbin/rsct/bin/samversion
=> /usr/sbin/rsct/bin/CHARM_LOGIC_R3.DVS
=> /usr/sbin/rsct/bin/IBM.RecoveryRMd
=> /usr/sbin/rsct/bin/IBM.GblResRMd
=> /usr/sbin/rsct/bin/lssam
0513-071 The ctrmc Subsystem has been added.
0513-059 The ctrmc Subsystem has been started. Subsystem PID is 2649.
Finished installing efix. To remove the efix use uninstall.sh from this deliverable.

++ links for softdog.ko.xz -> softdog.ko

[root@alpha ~]# 
[root@alpha ~]# preprpnode alpha beta
[root@alpha ~]# 
```

### Этап 5. Установка Tivoli SA MP на узле "beta"

```
[root@beta universal]# LANG=C ./db2/linuxamd64/tsamp/installSAM --noprereqcheck
installSAM: WARNING: prerequisite check was bypassed.
SAM is currently not installed.
...
Subsystem         Group            PID     Status 
 ctrmc            rsct             18642   active
installSAM: Warning: Must set CT_MANAGEMENT_SCOPE=2 

installSAM: All packages were installed successfully.
[root@beta universal]#

[root@beta ~]# /opt/ibm/db2/V11.1/install/tsamp/db2cptsa
DBI1099I  The DB2 High Availability (HA) scripts for the IBM Tivoli
      System Automation for Multiplatforms (SA MP) were successfully
      installed in /usr/sbin/rsct/sapolicies/db2.
...

++ install efix!
++ links for softdog.ko.xz -> softdog.ko

[root@beta ~]# preprpnode beta alpha
[root@beta ~]# 
```


### Этап 6. Настройка Tivoli SA MP на узле "beta"

```
[db2inst1@beta ~]$ db2haicu
Welcome to the DB2 High Availability Instance Configuration Utility (db2haicu).

You can find detailed diagnostic information in the DB2 server diagnostic log file called db2diag.log. 
Also, you can use the utility called db2pd to query the status of the cluster domains you create.

For more information about configuring your clustered environment using db2haicu, 
see the topic called 'DB2 High Availability Instance Configuration Utility (db2haicu)' 
in the DB2 Information Center.

db2haicu determined the current DB2 database manager instance is 'db2inst1'. 
The cluster configuration that follows will apply to this instance.

db2haicu is collecting information on your current setup. 
This step may take some time as db2haicu will need to activate 
all databases for the instance to discover all paths ...
When you use db2haicu to configure your clustered environment, 
you create cluster domains. For more information, see the topic 
'Creating a cluster domain with db2haicu' in the DB2 Information Center. 
db2haicu is searching the current machine for an existing active cluster domain ...
db2haicu did not find a cluster domain on this machine. 
db2haicu will now query the system for information about cluster nodes to create a new cluster domain ...

db2haicu did not find a cluster domain on this machine. 
To continue configuring your clustered environment for high availability, 
you must create a cluster domain; otherwise, db2haicu will exit.

Create a domain and continue? [1]
1. Yes
2. No
1
Create a unique name for the new domain:
db2dom0
Nodes must now be added to the new domain.
How many cluster nodes will the domain 'db2dom0' contain?
2
Enter the host name of a machine to add to the domain:
alpha
Enter the host name of a machine to add to the domain:
beta
db2haicu can now create a new domain containing the 2 machines that you specified. 
If you choose not to create a domain now, db2haicu will exit.

Create the domain now? [1]
1. Yes
2. No
1
Creating domain 'db2dom0' in the cluster ...
Creating domain 'db2dom0' in the cluster was successful.
You can now configure a quorum device for the domain. For more information, 
see the topic "Quorum devices" in the DB2 Information Center. 
If you do not configure a quorum device for the domain, then a human operator 
will have to manually intervene if subsets of machines in the cluster lose connectivity.

Configure a quorum device for the domain called 'db2dom0'? [1]
1. Yes
2. No
2
The cluster manager found the following total number of network interface 
cards on the machines in the cluster domain: '2'.  
You can add a network to your cluster domain using the db2haicu utility.

Create networks for these network interface cards? [1]
1. Yes
2. No
1
Enter the name of the network for the network interface card: 'eth0' on cluster node: 'beta.ccru.ibm.com'
1. Create a new public network for this network interface card.
2. Create a new private network for this network interface card.
3. Skip this step.
Enter selection:
1
Are you sure you want to add the network interface card 'eth0' 
on cluster node 'beta.ccru.ibm.com' to the network 'db2_public_network_0'? [1]
1. Yes
2. No
1
Adding network interface card 'eth0' on cluster node 'beta.ccru.ibm.com' 
to the network 'db2_public_network_0' ...
Adding network interface card 'eth0' on cluster node 'beta.ccru.ibm.com' 
to the network 'db2_public_network_0' was successful.
Enter the name of the network for the network interface card: 
'eth0' on cluster node: 'alpha.ccru.ibm.com'
1. db2_public_network_0
2. Create a new public network for this network interface card.
3. Create a new private network for this network interface card.
4. Skip this step.
Enter selection:
1
Are you sure you want to add the network interface card 'eth0' 
on cluster node 'alpha.ccru.ibm.com' to the network 'db2_public_network_0'? [1]
1. Yes
2. No
1
Adding network interface card 'eth0' on cluster node 'alpha.ccru.ibm.com' 
to the network 'db2_public_network_0' ...
Adding network interface card 'eth0' on cluster node 'alpha.ccru.ibm.com' 
to the network 'db2_public_network_0' was successful.
Retrieving high availability configuration parameter for instance 'db2inst1' ...
The cluster manager name configuration parameter (high availability configuration parameter) 
is not set. For more information, see the topic "cluster_mgr - Cluster manager name 
configuration parameter" in the DB2 Information Center. 
Do you want to set the high availability configuration parameter?
The following are valid settings for the high availability configuration parameter:
  1.TSA
  2.Vendor
Enter a value for the high availability configuration parameter: [1]
1
Setting a high availability configuration parameter for instance 'db2inst1' to 'TSA'.
Adding DB2 database partition '0' to the cluster ...
Adding DB2 database partition '0' to the cluster was successful.
Do you want to validate and automate HADR failover for the HADR database 'HATEST'? [1]
1. Yes
2. No
1
Adding HADR database 'HATEST' to the domain ...
HADR database 'HATEST' has been determined to be valid for high availability. 
However, the database cannot be added to the cluster from this node because 
db2haicu detected this node is the standby for HADR database 'HATEST'. 
Run db2haicu on the primary for HADR database 'HATEST' to configure 
the database for automated failover.
All cluster configurations have been completed successfully. db2haicu exiting ...
[db2inst1@beta ~]$ 
```


### Этап 7. Настройка Tivoli SA MP на узле "alpha"

```
[db2inst1@alpha ~]$ db2haicu
Welcome to the DB2 High Availability Instance Configuration Utility (db2haicu).

You can find detailed diagnostic information in the DB2 server diagnostic 
log file called db2diag.log. Also, you can use the utility called db2pd to query 
the status of the cluster domains you create.

For more information about configuring your clustered environment using db2haicu, 
see the topic called 'DB2 High Availability Instance Configuration Utility (db2haicu)' 
in the DB2 Information Center.

db2haicu determined the current DB2 database manager instance is 'db2inst1'. 
The cluster configuration that follows will apply to this instance.

db2haicu is collecting information on your current setup. This step may take some time 
as db2haicu will need to activate all databases for the instance to discover all paths ...
When you use db2haicu to configure your clustered environment, you create cluster domains. 
For more information, see the topic 'Creating a cluster domain with db2haicu' in the 
DB2 Information Center. 
db2haicu is searching the current machine for an existing active cluster domain ...
db2haicu found a cluster domain called 'db2dom0' on this machine. 
The cluster configuration that follows will apply to this domain.

Retrieving high availability configuration parameter for instance 'db2inst1' ...
The cluster manager name configuration parameter (high availability configuration parameter) 
is not set. For more information, see the topic "cluster_mgr - Cluster manager name 
configuration parameter" in the DB2 Information Center. 
Do you want to set the high availability configuration parameter?
The following are valid settings for the high availability configuration parameter:
  1.TSA
  2.Vendor
Enter a value for the high availability configuration parameter: [1]
1
Setting a high availability configuration parameter for instance 'db2inst1' to 'TSA'.
Adding DB2 database partition '0' to the cluster ...
Adding DB2 database partition '0' to the cluster was successful.
Do you want to validate and automate HADR failover for the HADR database 'HATEST'? [1]
1. Yes
2. No
1
Adding HADR database 'HATEST' to the domain ...
Adding HADR database 'HATEST' to the domain was successful.
Do you want to configure a virtual IP address for the HADR database 'HATEST'? [1]
1. Yes
2. No
2
Do you want to configure mount point monitoring for the HADR database 'HATEST'? [2]
1. Yes
2. No
2
All cluster configurations have been completed successfully. db2haicu exiting ...
[db2inst1@alpha ~]$ 
```


### Этап 8. Настройка устройства кворума Tivoli SA MP на узле "alpha"

```
[db2inst1@alpha DIAG0000]$ db2haicu
Welcome to the DB2 High Availability Instance Configuration Utility (db2haicu).

You can find detailed diagnostic information in the DB2 server diagnostic log file 
called db2diag.log. Also, you can use the utility called db2pd to query the status 
of the cluster domains you create.

For more information about configuring your clustered environment using db2haicu, 
see the topic called 'DB2 High Availability Instance Configuration Utility (db2haicu)' 
in the DB2 Information Center.

db2haicu determined the current DB2 database manager instance is 'db2inst1'. 
The cluster configuration that follows will apply to this instance.

db2haicu is collecting information on your current setup. 
This step may take some time as db2haicu will need to activate all databases 
for the instance to discover all paths ...
When you use db2haicu to configure your clustered environment, you create cluster domains. 
For more information, see the topic 'Creating a cluster domain with db2haicu' 
in the DB2 Information Center. 
db2haicu is searching the current machine for an existing active cluster domain ...
db2haicu found a cluster domain called 'db2dom0' on this machine. 
The cluster configuration that follows will apply to this domain.

Select an administrative task by number from the list below:
  1. Add or remove cluster nodes.
  2. Add or remove a network interface.
  3. Add, remove or modify HADR databases.
  4. Add or remove an IP address.
  5. Move DB2 database partitions and HADR databases for scheduled maintenance.
  6. Create a new quorum device for the domain.
  7. Destroy the domain.
  8. Exit.
Enter your selection:
6
Are you sure you want to create a new quorum device for the domain 'db2dom0'? [1]
1. Yes
2. No
1
The following is a list of supported quorum device types:
  1. Network Quorum
Enter the number corresponding to the quorum device type to be used: [1]
1
Specify the network address of the quorum device:
192.168.122.1
Configuring quorum device for domain 'db2dom0' ...
Configuring quorum device for domain 'db2dom0' was successful.
Do you want to make any other changes to the cluster configuration? [1]
1. Yes
2. No
2
All cluster configurations have been completed successfully. db2haicu exiting ...
[db2inst1@alpha DIAG0000]$ 
```