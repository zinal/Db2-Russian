#! /bin/sh

set -e
set +u

CURL=`which curl`
if [ -z "$CURL" ]; then
  echo "ERROR: curl is not installed" >&2
  exit 1
fi

# Create the cache directory if it does not exist
[ -d .cache ] || mkdir .cache

# Distributions directory
[ -d .cache/ditto ] || mkdir .cache/ditto

# Grab ZooKeeper
DITTO_ZK=.cache/ditto/zookeeper.tgz
if [ ! -f $DITTO_ZK ]; then
  $CURL -o $DITTO_ZK https://downloads.apache.org/zookeeper/zookeeper-3.7.0/apache-zookeeper-3.7.0-bin.tar.gz
fi
