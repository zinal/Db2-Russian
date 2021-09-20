#! /bin/sh

set -e
set +u

URL_ZK='https://downloads.apache.org/zookeeper/zookeeper-3.7.0/apache-zookeeper-3.7.0-bin.tar.gz'
URL_DB2='https://repo1.maven.org/maven2/com/ibm/db2/jcc/11.5.6.0/jcc-11.5.6.0.jar'
URL_GROOVY='https://groovy.jfrog.io/ui/api/v1/download?repoKey=dist-release-local&path=groovy-zips%252Fapache-groovy-binary-3.0.9.zip'

CURL=`which curl`
if [ -z "$CURL" ]; then
  echo "ERROR: curl is not installed" >&2
  exit 1
fi

UNZIP=`which unzip`
if [ -z "$UNZIP" ]; then
  echo "ERROR: unzip is not installed" >&2
  exit 1
fi

if [ ! -z "$JAVA_HOME" ]; then
  JAVA="$JAVA_HOME"/bin/java
else
  JAVA=`which java`
fi
if [ -z $JAVA ] || [ ! -x $JAVA ]; then
  echo "ERROR: java is not installed" >&2
  exit 1
fi

# Create the cache directory if it does not exist
[ -d .cache ] || mkdir .cache
# Distributions directory
[ -d .cache/ditto ] || mkdir .cache/ditto
# Jars directory
[ -d .cache/jars ] || mkdir .cache/jars

# Grab ZooKeeper
DITTO_ZK=.cache/ditto/zookeeper.tgz
if [ ! -f $DITTO_ZK ]; then
  echo "NOTE: downloading ZooKeeper" >&2
  $CURL -o $DITTO_ZK $URL_ZK
fi

MAIN_ZK=.cache/zk
if [ ! -f $MAIN_ZK/bin/zkCli.sh ]; then
  echo "NOTE: unpacking ZooKeeper" >&2
  rm -rf $MAIN_ZK
  mkdir $MAIN_ZK
  tar xf $DITTO_ZK -C $MAIN_ZK --strip-components=1
fi

MAIN_ZK_JAR=.cache/jars/zookeeper.jar
if [ ! -f $MAIN_ZK_JAR ]; then
  cp $MAIN_ZK/lib/zookeeper-[0-9]*.[0-9]*.[0-9]*.jar $MAIN_ZK_JAR
fi

# Grab Db2 Client
DITTO_DB2=.cache/ditto/db2jcc4.jar
if [ ! -f $DITTO_DB2 ]; then
  echo "NOTE: downloading Db2 Client" >&2
  $CURL -o $DITTO_DB2 $URL_DB2
fi

MAIN_DB2=.cache/jars/db2jcc4.jar
if [ ! -f $MAIN_DB2  ]; then
  cp $DITTO_DB2 $MAIN_DB2
fi

# Grab Groovy
DITTO_GROOVY=.cache/ditto/groovy.zip
if [ ! -f $DITTO_GROOVY ]; then
  echo "NOTE: downloading Groovy" >&2
  $CURL -o $DITTO_GROOVY $URL_GROOVY
fi

MAIN_GROOVY=.cache/groovy
if [ ! -f $MAIN_GROOVY/bin/groovy ]; then
  echo "NOTE: unpacking Groovy" >&2
  rm -rf $MAIN_GROOVY
  rm -rf .cache/temp
  mkdir .cache/temp
  (cd .cache/temp && $UNZIP -q ../../$DITTO_GROOVY)
  TEMP_GROOVY=.cache/temp/`(cd .cache/temp && ls)`
  mv $TEMP_GROOVY $MAIN_GROOVY
  rm -rf .cache/temp
fi
