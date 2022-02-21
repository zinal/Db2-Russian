# Connecting to Greenplum using Db2 Federation

In addition to this document, there are also official instructions from IBM:
- [ODBC example](https://www.ibm.com/support/pages/how-access-data-greenplum-using-db2-federation-server);
- [the second ODBC example](https://www.ibm.com/support/pages/how-query-greenplum-using-db2-federation-through-odbc-driver);
- [JDBC example](https://www.ibm.com/support/pages/how-query-greenplum-data-source-using-federation-server-through-jdbc-driver).


## 1. Server object creation

### 1.1 Option 1: using the ODBC wrapper

Currently (Db2 11.5.7) ODBC access to Greenplum requires the manual setup
of the additional option: `DB2_SELECT_COL_ONLY 'N'`. If it is not set up
properly, `COUNT` and other functions are not pushed down to the source
server, which may lead to additional traffic and slower query execution.

For pushdown of operations of character strings, `DB2_SAME_STR_COMP_SEMANTICS 'Y'`
option is needed as well. To handle long strings, currently the character
string limit should be defined as `DB2_LONGSTRING_LIMIT '32672'`.

In order to enable the pushdown of sorts over strings, `COLLATING_SEQUENCE 'Y'`
needs to be specified.

If the character strings do not contain trailing blanks, or if the comparison
logic for the trailing blanks is not important, `VARCHAR_NO_TRAILING_BLANKS 'Y'`
needs to be specified for more complete pushdown.

Ideally the remote database codepage for Greenplum should be specified
in the server options. Typically Db2 databases are currently created
with UTF-8 codepage (1208). By default, Greenplum uses UTF-8 as well.
The codepage needs to be specified for the server object.

Server object creation command is shown below:

```SQL
CREATE SERVER gp_srv TYPE greenplum OPTIONS 
  (HOST '10.240.173.242', PORT '5433', DBNAME 'adb',
   PUSHDOWN 'Y',
   DB2_MAXIMAL_PUSHDOWN 'Y',
   DB2_SELECT_COL_ONLY 'N',
   DB2_SAME_STR_COMP_SEMANTICS 'Y',
   DB2_LONGSTRING_LIMIT '32672',
   COLLATING_SEQUENCE 'Y',
   VARCHAR_NO_TRAILING_BLANKS 'Y',
   CODEPAGE '1208');
```

### 1.2 Option 2: Using the JDBC wrapper

Wrapper creation for JDBC is shown below (single wrapper is needed for all server objects).

```SQL
CREATE WRAPPER gpjdbc_srv LIBRARY 'libdb2rcjdbc.so' OPTIONS (DB2_FENCED 'Y');
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
   PUSHDOWN 'Y',
   DB2_MAXIMAL_PUSHDOWN 'Y',
   DB2_SAME_STR_COMP_SEMANTICS 'Y');
```

Please note that the additional options for pushdown might be needed here as well (not fully tested).

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

Some character data types on Greenplum are treated as CLOB by the
Db2 Federation. This leads to loss of query pushdown capabilities
when such string is returned in the result set. In order to overcome
this issue, the more limited datatype (like `VARCHAR(100)`) should
be used instead, with the proper maximum length defined.

Changing the local data types of the existing nicknames can be
performed by the `ALTER NICKNAME` statement, as shown below:

```SQL
ALTER NICKNAME gp.schema1_table1 ALTER COLUMN column1 LOCAL TYPE VARCHAR(100);
```

To check the actual required length (after setting some initial defaults),
the following statement can be used (which pushes down to Greenplum
after the column's local data type is switched from `CLOB` to `VARCHAR(N)`):

```SQL
SELECT MAX(LENGTH(columns1) FROM gp.schema1_table1;
```
