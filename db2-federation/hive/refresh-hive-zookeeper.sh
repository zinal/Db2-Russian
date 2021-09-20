#! /bin/sh

set -e
set +u

. download-dependencies.sh

XCP=$MAIN_DB2:$MAIN_ZK_JAR
$MAIN_GROOVY/bin/groovy -cp "$XCP" RefreshHiveZookeeper.groovy
