#! /bin/sh

set -e
set +u

. download-dependencies.sh

$MAIN_GROOVY/bin/groovy -cp '.ditto/zk/lib/*.jar' RefreshHiveZookeeper.groovy RefreshHiveZookeeper.xml
