#! /bin/sh
# Startup script for Zookeeper-based Hive federation on Db2 program.

set -e
set +u

. download-dependencies.sh

XCP=.:$MAIN_DB2
for x in `find .cache/zk/lib -type f -name \*.jar`; do
    XCP="$XCP":"$x"
done

$MAIN_GROOVY/bin/groovy -cp "$XCP" RefreshHiveZookeeper.groovy

# End Of File
