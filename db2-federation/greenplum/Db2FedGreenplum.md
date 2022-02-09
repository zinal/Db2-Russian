# Connecting to Greenplum using Db2 Federation

In addition to this document, there is also an
[official instruction from IBM](https://www.ibm.com/support/pages/how-query-greenplum-data-source-using-federation-server-through-jdbc-driver)

Currently (Db2 11.5.7) ODBC access to Greenplum is possible, but query offload is limited.
For example, `COUNT` and `COUNT_BIG` are not offloaded.
Therefore, JDBC access is described below.

## Wrapper creation for JDBC

```SQL
create wrapper "gpjdbc" library 'libdb2rcjdbc.so' options(db2_fenced 'y');
```

## Check the path to the JDBC driver package

Typically the shipped driver sits in the `sqllib/federation/jdbc/lib/FOgreenplum.jar`
file inside the instance owner home directory.

Example locations for the `sqllib`:
- `/home/db2inst1` (regular Db2 installation);
- `/mnt/blumeta0/home/db2inst1` (typical Db2 in container, including Db2 Warehouse, DV and BigSQL services).

## Server object creation

```SQL
CREATE SERVER GP_SRV TYPE GREENPLUM WRAPPER "gpjdbc" OPTIONS 
  (DRIVER_CLASS  'com.ibm.fluidquery.jdbc.greenplum.GreenplumDriver' ,
   DRIVER_PACKAGE  '<instance_homedir>/sqllib/federation/jdbc/lib/FOgreenplum.jar',
   URL  'jdbc:ibm:greenplum://<hostname>:5432;DatabaseName=<dbname>',
   pushdown 'Y',
   db2_maximal_pushdown 'Y',
   db2_same_str_comp_semantics 'Y');
```

## Authentication details

The simplest option is to configure the user mapping for the `PUBLIC` user, which will be applied
in all cases when the particular user does not have his own mapping.

```SQL
create user mapping for public server GP_SRV options
  (REMOTE_AUTHID '<gp_username>',
   REMOTE_PASSWORD '<gp_password>');
```

## Checking the connection

```SQL
SET PASSTHRU GP_SRV;

select cast(table_schema as varchar(60)), count(*)
from information_schema.tables
group by table_schema;

SET PASSTHRU RESET;
```

Connection errors may mean any of the following:
- the network details are incorrect (host, port);
- firewall issues;
- access denied due to invalid username or password.

## Creating the nicknames

```SQL
CREATE SCHEMA GP;
CREATE NICKNAME GP.SCHEMA1_TABLE1 FOR GP_SRV."schema1"."table1";
```
