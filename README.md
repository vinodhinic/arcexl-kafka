# Kafka - Level 0
This is recipe based training on Apache Kafka for ArcExL. Level-0 covers basic concepts of Kafka. KStreams and KConnect are out of scope for this training. 

## Setup
* IDE - We prefer that you work on Intellij
* Gradle
* Java 8 or above/Kotlin
* Spring/Spring Boot
* Postgres DB
  * Create 2 databases - `arcexl_prod`, `arcexl_uat`
  * Create `arcexl` schema in both DBs
  * Create superuser `postgres` with password `admin`
* Dbeaver 
* Kafka (latest : 2.5.0)
--------------------

<details>
<summary>Postgres Linux Installation</summary>

#### Set your local postgres working directory - do this in every new terminal window / shell
`export TEST_PG_ROOT=/codemill/$USER/postgres`
#### Create postgres working directory if it doesn't exist - this only needs to be done once
`mkdir -p $TEST_PG_ROOT`
#### Download Postgres .tar.gz - this only needs to be done once
* `wget https://sbp.enterprisedb.com/getfile.jsp?fileid=12354 -O $TEST_PG_ROOT/pgsql.tar.gz`
* `tar xzf $TEST_PG_ROOT/pgsql.tar.gz`
#### Initialize the database - this only needs to be done once
* `rm -rf $TEST_PG_ROOT/db`
* `mkdir -p $TEST_PG_ROOT/db`
* `$TEST_PG_ROOT/pgsql/bin/initdb $TEST_PG_ROOT/db`
#### Start a local postgres server - this runs postgres in the foreground, can be shut down with ctrl-c and restarted as many times as you'd like
`$TEST_PG_ROOT/pgsql/bin/postgres -D $TEST_PG_ROOT/db --unix_socket_directories=$TEST_PG_ROOT --wal_level=minimal --archive_mode=off --max_wal_senders=0 --checkpoint_timeout=30 --archive_command=/bin/true --max_wal_size=256MB`
#### Connect via psql - optional to test that your server works. You can also use dbeaver
`$TEST_PG_ROOT/pgsql/bin/psql --host=$TEST_PG_ROOT -d postgres`
#### Create user and assign password and provide owner permissions to run DDLs via flyway - username & password should match the properties mentioned in application.properties
```
 CREATE USER postgres SUPERUSER;
 CREATE DATABASE arcexl_prod WITH OWNER postgres;
 CREATE DATABASE arcexl_uat WITH OWNER postgres;
 ALTER USER postgres WITH PASSWORD 'admin';
```
#### Create schema arcexl in both prod and uat DB
```
$TEST_PG_ROOT/pgsql/bin/psql -U postgres
\l <to list database>
\c <db_name> 
CREATE SCHEMA IF NOT EXISTS arcexl; 
```

</details>

-----------------
<details>
<summary>Postgres windows installation</summary>

* initdb
```
C:\Users\vino\Downloads\pgsql\bin>initdb -D "C:\Users\vino\Downloads\pgsql\datadir"
The files belonging to this database system will be owned by user "vino".
This user must also own the server process.

The database cluster will be initialized with locale "English_United States.1252".
The default database encoding has accordingly been set to "WIN1252".
The default text search configuration will be set to "english".

Data page checksums are disabled.

creating directory C:/Users/vino/Downloads/pgsql/datadir ... ok
creating subdirectories ... ok
selecting dynamic shared memory implementation ... windows
selecting default max_connections ... 100
selecting default shared_buffers ... 128MB
selecting default time zone ... Asia/Calcutta
creating configuration files ... ok
running bootstrap script ... ok
performing post-bootstrap initialization ... ok
syncing data to disk ... ok

initdb: warning: enabling "trust" authentication for local connections
You can change this by editing pg_hba.conf or using the option -A, or
--auth-local and --auth-host, the next time you run initdb.

Success. You can now start the database server using:

    pg_ctl -D ^"C^:^\Users^\vino^\Downloads^\pgsql^\datadir^" -l logfile start
```
* Start postgres server	
```
C:\Users\vino\Downloads\pgsql\bin>pg_ctl -D "C:\Users\vino\Downloads\pgsql\datadir" start
```

* Use client (or use dbeaver) and give permissions to user "postgres" and assign password too.
```
C:\Users\vino\Downloads\pgsql\bin>psql -d postgres
psql (12.2)
WARNING: Console code page (437) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
Type "help" for help.

postgres=# CREATE USER postgres SUPERUSER;
CREATE ROLE
postgres=# ALTER USER postgres WITH PASSWORD 'admin';
ALTER ROLE
postgres=# CREATE DATABASE arcexl_prod WITH OWNER postgres;
postgres=# CREATE DATABASE arcexl_uat WITH OWNER postgres;
```

* Create schema in each DB
```
C:\Users\vino\Downloads\pgsql\bin>psql -U postgres
postgres=# \c arcexl_prod
You are now connected to database "arcexl_prod" as user "postgres".
arcexl_prod=# create schema if not exists arcexl;
```

</details>

------------------

<details>
<summary>Connecting to postgres from Dbeaver</summary>

* New -> Dbeaver -> Database Connection -> PostgreSQL
* Host : localhost 
* Port : 5432
* Database : arcexl_prod or arcexl_uat
* User : postgres
* password : admin (select save password locally)
</details>

---------

## Recipe 1 - Setting up a Spring project

### Exercise

* Set up spring dependencies in gradle
* Add domain object for StockPrice
```
data class StockPrice(val stockSymbol: String,
                         val date: LocalDate,
                         val price: Double)
```
* create table `stock_price`
```
CREATE TABLE IF NOT EXISTS stock_price (
                    stock_symbol varchar(100),
                    "date" date,
                    price numeric,
                    constraint stock_price_pk primary key (stock_symbol, "date")
                    )
```
* DAO support 

```
public interface StockPriceDao {
    void insertStockPrice(StockPrice stockPrice);
    StockPrice selectStockPrice(String stockSymbol, LocalDate date);
    List<StockPrice> selectAllStockPrice();
    void deleteStockPrice(String stockSymbol, LocalDate date);
}
```

* Support prod and uat profile to connect to the respective databases
* Test the DAO methods - if you are connected to prod/uat to run test, the test data should be rolled back after each (successful/failed) test

### Module
Name : setup

### Focus Points
* Clearing test data after each run
* Kotlin data classes and extension functions
* Kotlin test
* Check out the DAO Implementation at kotlin - delegate pattern
* Mybatis annotation mappers
* Flyway

Note that it is not 100% kotlin. I don't want to add another learning curve here. If you already know a little Kotlin, some features I have used here would be an useful reference to you.
**It is not mandatory to stick to the module provided here**. All we need at this point, is to have an application that connects to prod/uat db based on the profile and supports read/write dao on stock_price table.  

------------ 
