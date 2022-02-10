# Connecting to Greenplum using Db2 Federation

In addition to this document, there are also official instructions from IBM:
- [ODBC example](https://www.ibm.com/support/pages/how-access-data-greenplum-using-db2-federation-server);
- [the second ODBC example](https://www.ibm.com/support/pages/how-query-greenplum-using-db2-federation-through-odbc-driver);
- [JDBC example](https://www.ibm.com/support/pages/how-query-greenplum-data-source-using-federation-server-through-jdbc-driver).


## 1. Server object creation

### 1.1 Option 1: using the ODBC wrapper

Currently (Db2 11.5.7) ODBC access to Greenplum requires the manual setup
of the additional option: `db2_select_col_only 'N'`. If it is not set up
properly, `COUNT` and other functions are not pushed down to the source
server, which may lead to additional traffic and slower query execution.

Server object creation command is shown below:

```SQL
CREATE SERVER gp_srv TYPE greenplum OPTIONS 
  (host '10.240.173.242', port '5433', dbname 'adb',
   pushdown 'Y',
   db2_maximal_pushdown 'Y',
   db2_same_str_comp_semantics 'Y',
   db2_select_col_only 'N');
```

### 1.2 Option 2: Using the JDBC wrapper

Wrapper creation for JDBC is shown below (single wrapper is needed for all server objects).

```SQL
create wrapper "gpjdbc" library 'libdb2rcjdbc.so' options(db2_fenced 'y');
```

We need to check/validate the path to the JDBC driver package.
Typically the shipped driver sits in the `sqllib/federation/jdbc/lib/FOgreenplum.jar`
file inside the instance owner home directory.

Example locations for the `sqllib`:
- `/home/db2inst1` (regular Db2 installation);
- `/mnt/blumeta0/home/db2inst1` (typical Db2 in container, including Db2 Warehouse, DV and BigSQL services).

Server object creation command is shown below:

```SQL
CREATE SERVER gp_srv TYPE GREENPLUM WRAPPER "gpjdbc" OPTIONS 
  (DRIVER_CLASS  'com.ibm.fluidquery.jdbc.greenplum.GreenplumDriver' ,
   DRIVER_PACKAGE  '<instance_homedir>/sqllib/federation/jdbc/lib/FOgreenplum.jar',
   URL  'jdbc:ibm:greenplum://<hostname>:5432;DatabaseName=<dbname>',
   pushdown 'Y',
   db2_maximal_pushdown 'Y',
   db2_same_str_comp_semantics 'Y');
```

## 2 Authentication details

The simplest option is to configure the user mapping for the `PUBLIC` user, which will be applied
in all cases when the particular user does not have his own mapping.

```SQL
CREATE USER MAPPING FOR public SERVER gp_srv OPTIONS
  (REMOTE_AUTHID '<gp_username>',
   REMOTE_PASSWORD '<gp_password>');
```

## 3 Checking the connection

```SQL
SET PASSTHRU gp_srv;

select 1;

select cast(table_schema as varchar(60)), count(*)
from information_schema.tables
group by table_schema order by table_schema;

SET PASSTHRU RESET;
```

Connection errors may mean any of the following:
- the network details are incorrect (host, port);
- firewall issues;
- access denied due to invalid username or password.

## 4 Creating the nicknames

```SQL
CREATE SCHEMA gp;
CREATE NICKNAME gp.schema1_table1 FOR gp_srv."schema1"."table1";
CREATE NICKNAME gp.schema1_table2 FOR gp_srv."schema1"."table2";
CREATE NICKNAME gp.schema2_table3 FOR gp_srv."schema2"."table3";
```
