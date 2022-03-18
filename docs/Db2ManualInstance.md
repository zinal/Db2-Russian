# Ручное создание экземпляра Db2

```bash
groupadd db2iadm1
# groupadd db2fadm1
useradd -g db2iadm1 db2inst1
useradd -g db2iadm1 db2fenc1

mkdir /home/db2inst1
chown db2inst1:db2iadm1 /home/db2inst1
mkdir /home/db2fenc1
chown db2fenc1:db2iadm1 /home/db2fenc1
```

/etc/services:
```
db2c_db2inst1           50001/tcp
db2c_db2inst2           50002/tcp
```

```bash
cd /opt/ibm/db2/V11.1
./instance/db2icrt -p 50000 -u db2fenc1 db2inst1
./instance/db2icrt -p db2c_db2inst1 -u db2fenc1 db2inst1
./instance/db2icrt -p db2c_db2inst2 -u db2fenc2 db2inst2

useradd -g db2iadm1 db2inst2
useradd -g db2fadm1 db2fenc2
./instance/db2icrt -p 50001 -u db2fenc2 db2inst2
./instance/db2icrt -p db2c_db2inst2 -u db2fenc2 db2inst2
```

# /etc/sysctl.conf:
```
# 256 * RAM size, GBytes
kernel.shmmni = 512

# RAM size, bytes
# kernel.shmmax = 2147483648
kernel.shmmax = 8589934592

# 2 x RAM in default pages
#kernel.shmall = 1048576
kernel.shmall = 4194304

# kernel.sem = SEMMSL SEMMNS SEMOPM SEMMNI
#  SEMMNI 256 * RAM size, Gbytes
kernel.sem = 250 256000 32 512

# 1024 * RAM size, GBytes]=-9900000000==8
kernel.msgmni = 2048
kernel.msgmax = 65536
kernel.msgmnb = 65536

vm.swappiness = 5
vm.overcommit_memory = 0

fs.file-max = 6815744
net.ipv4.ip_local_port_range = 9000 65535

# Oracle requirement, may be good for Db2 as well
net.core.rmem_default = 262144
net.core.rmem_max = 4194304
net.core.wmem_default = 262144
net.core.wmem_max = 4194304

# For DB2 v9.7 ONLY
kernel.randomize_va_space = 0
```

# /etc/security/limits.d/30-db2.conf
```
@db2iadm1           soft    nproc           8192
@db2iadm1           hard    nproc           8192
@db2iadm1           soft    nofile          65536
@db2iadm1           hard    nofile          65536
@db2iadm1           soft    stack           16384
@db2iadm1           hard    stack           16384
```

```
************ AIX ************
mkgroup id=999 db2iadm1
mkgroup id=998 db2fadm1
mkgroup id=997 dasadm1
mkuser id=1004 pgrp=db2iadm1 groups=db2iadm1 home=/home/db2inst1 db2inst1
mkuser id=1003 pgrp=db2fadm1 groups=db2fadm1 home=/home/db2fenc1 db2fenc1
mkuser id=1002 pgrp=dasadm1 groups=dasadm1 home=/home/dasusr1 dasusr1

chuser fsize=-1 fsize_hard=-1 data=-1 data_hard=-1 \
  stack=-1 stack_hard=-1 rss=-1 rss_hard=-1 \
  nofiles=-1 nofiles_hard=-1 db2inst1
```
