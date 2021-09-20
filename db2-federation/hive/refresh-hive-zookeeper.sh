#! /bin/sh

set -e
set +u

. download-dependencies.sh

$MAIN_GROOVY/bin/groovy RefreshHiveZookeeper.groovy RefreshHiveZookeeper.xml
