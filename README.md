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

## Recipe 2 - Setting up Kafka

### Installation & Creating Brokers

As few of you don't have Linux setup, I am explicitly doing Kafka Windows installation so that I can help unblock you with installation issues, if any.

* Download and extract the latest kafka.
* Open CMD and navigate to <kafka>/bin/windows
* Execute `kafka-topics.bat` 
    * If you see `The input line is too long. The syntax of the command is incorrect.` error, then rename you kafka folder from `kafka_2.12-2.5.0` to `kafka`
    * Reopen CMD and try again
* Create a folder `arcexlData` under `kafka` folder
    * create 2 folders under  `arcexlData` - `zookeeper` and `kafka`
    ```
    kafka
    |______bin
    |______config
    |______arcexlData
            |________kafka
            |________zookeeper
    ```
* Edit `zookeeper.properties` under `kafka/config` - Mind the forward slash
`dataDir=C:/Users/vino/Downloads/kafka/arcexlData/zookeeper`
*Edit `server.properties` under `kafka/config`
`log.dirs=C:/Users/chockali/Downloads/kafka/arcexlData/kafka` 
* Open a new CMD window and Start Zookeeper
`C:\Users\vino\Downloads\kafka>bin\windows\kafka-server-start.bat config\server.properties`
    * You should see that it is binding to port 2181
    
        ```[2020-05-17 01:23:56,813] INFO binding to port 0.0.0.0/0.0.0.0:2181 (org.apache.zookeeper.server.NIOServerCnxnFactory)```
  
* Open another new CMD window and Start Kafka Server
`C:\Users\vino\Downloads\kafka>bin\windows\kafka-server-start.bat config\server.properties`
    * Notice the ID `[2020-05-17 01:46:44,435] INFO [KafkaServer id=0] started (kafka.server.KafkaServer)`

On a side note, [future versions of Kafka will not use Zookeeper](https://cwiki.apache.org/confluence/display/KAFKA/KIP-500%3A+Replace+ZooKeeper+with+a+Self-Managed+Metadata+Quorum)

What you have now is a Kafka Broker.

----------------
### Creating Topics

* Create a topic with name - stockPriceTopic, partitions - 3, replication-factor - 2
    ```
    C:\Users\vino\Downloads\kafka>bin\windows\kafka-topics.bat --zookeeper 127.0.0.1:2181 --topic stockPriceTopic --create --partitions 3 --replication-factor 2
    Error while executing topic command : Replication factor: 2 larger than available brokers: 1.
    [2020-05-17 01:41:55,770] ERROR org.apache.kafka.common.errors.InvalidReplicationFactorException: Replication factor: 2 larger than available brokers: 1.
     (kafka.admin.TopicCommand$)
    ```
* Notice the error message. In previous exercise you have only created 1 broker but you are asking the partitions to be replicated at 2. That's why the error. For now let's keep replication-factor=1. Note that since you are running kafka locally, increasing broker for this exercise does not make any sense. There is no point increasing brokers for resilience - if your system crashes, you will lose all brokers. 
    ```
   C:\Users\vino\Downloads\kafka>bin\windows\kafka-topics.bat --zookeeper 127.0.0.1:2181 --topic stockPriceTopic --create --partitions 3 --replication-factor 1
  Created topic stockPriceTopic.
    ```
* List the topics
    ```
     C:\Users\vino\Downloads\kafka>bin\windows\kafka-topics.bat --zookeeper 127.0.0.1:2181 --list
    stockPriceTopic
    ```
* Describe the topic. You will see buzzwords : Partition, replicationFactor, Leader, ISR (In-Sync Replicas) - we will get to it soon. Note that the leader : 0 is nothing but the `kafka-server` we ran (go back check the logs at kafka-server cmd window)
    ```
    C:\Users\vino\Downloads\kafka>bin\windows\kafka-topics.bat --zookeeper 127.0.0.1:2181 --topic stockPriceTopic --describe
    ```
* If you are curious, figure out what it takes to increase replication factor of this topic and do that as an exercise. Hint : [Multi broker cluster](https://kafka.apache.org/quickstart#quickstart_multibroker)
* In windows, never delete a topic. This crashes Kafka Broker and [this is a known issue.](https://issues.apache.org/jira/browse/KAFKA-1194)

This concludes the installation.

### Checkpoint - Broker and Topics

With installation, we have seen 2 major components of Kafka - Broker and Topics

* A number of Brokers makes a Kafka cluster. 
    * Each broker is identified by an id - we saw ID : 0 in our setup.
    * They can discover each other because of Zookeeper.
* Topic holds the data and it can have many partitions.
    * Broker houses Topic partitions. Eg :
        * you have 2 brokers - B1 & B2 
        * A topic of 3 partitions - P1, P2 & P3. 
        * Broker B1 has [P1, P2]. 
        * Broker B2 has [P3]
    * Each Partition can have only one Leader and *only that leader* can receive and serve data for that partition. 
    * If you set replication factor as n for topic, it means each partition gets replicated n times.
        * Topic with P1, P2
        * Broker - B1, B2
        * Replication Factor = 2
        * B1 has [P1, P2'] and B2 has [P2, P1']
        * B1 is leader for P1 and B2 is syncing data from B1 for partition 1 and therefore it is an ISR - In Sync Replica
        * B2 is leader for P2 and B1 is ISR for P2
        * If B1 is down, then B2 can serve data. i.e B2 becomes leader for both P1 and P2

_You should understand broker, topic, partition, replication factor, Leader, ISR before going to the next step._

### Exercise

Using the CLI Commands,
* Create a topic - test with 3 partitions
* Use `kafka-console-producer` to produce some messages into this topic - 1,2,3,4,...20
    ```
  C:\Users\vino\Downloads\kafka>bin\windows\kafka-console-producer.bat --broker-list 127.0.0.1:9092 --topic test
    ``` 
    * How are you able to discover broker at 9092. Hint : Look into `config/server.properties`
    * Can you produce to a topic that does not exist? Hint : Look for `num.partitions` in server.properties file.
* Kill the kafka-console-producer command
* Now use `kafka-console-consumer` to consume the messages produced.
    * Can you see the numbers you produced? No?
    * Run the kafka-console-producer again and produce messages : 21,22,23,..25.
    * Why can you only see from 21?
    * Hint: `--from-beginning`
* Why the numbers are not in order?
    * Hint : Number of partitions you set for the test topic

-------------

## Overview

Before we go ahead with further recipes, here is a brief overview of what we are going to build

![Record-and-Replay](/docs/record_and_replay.png)

1. StockPriceApp-Prod is going to read stock prices from Feeds - which is actually a csv [committed to the repo](/record-replay/src/main/resources/feeds.csv)
1. StockPriceAdd-Prod writes these stock prices to prod-DB
1. It also writes to stockPriceTopic at Kafka
1. StockPriceApp-Uat is reading the stock prices from stockPriceTopic at Kafka
1. StockPriceApp-Uat writes the stock prices read from kafka to uat-DB

-------------

## Recipe 3 - Producing messages into Kafka

### Exercise
* Make sure stockPriceTopic is created with 3 partitions.
* Add an implementation for `StockPriceWriter` interface

    ```
    public interface StockPriceWriter {
        void writeStockPrice(StockPrice stockPrice);
    }
    ```
* This implementation should write to DB and then to Kafka using Kafka Producer API
* Also, it should only write to Kafka if `writeStockPriceToKafka` property is set to true
* At prod profile, set `writeStockPriceToKafka` to true and at uat profile, set this to false
* Add an implementation for `StockPriceReader` interface. This implementation should read stockPrice from [feeds.csv](/record-replay/src/main/resources/feeds.csv) file. 

    ```
    public interface StockPriceReader {
        List<StockPrice> read();
    }
    ```
    * Do not waste too much time on this implementation. You need opencsv - version 5 or above - in order to parse dates to LocalDate. Copy the implementation from [FeedStockPriceReaderImpl](/record-replay/src/main/java/com/arcexl/reader/FeedStockPriceReaderImpl.java). You will also need few annotations at the [StockPrice](/record-replay/src/main/kotlin/com/arcexl/domain/StockPrice.kt) model.
* Write a small program that reads from CSV using the `StockPriceReader` and writes to both DB and Kafka using `StockPriceWriter`
* Use CLI commands to verify the kafka producer
    * `kafka-console-consumer` CLI to verify that the messages are produced
    
        `bin\windows\kafka-console-consumer.bat --topic stockPriceTopic --bootstrap-server 127.0.0.1:9092 --from-beginning`
    
    * While the console-consumer is still active - i.e. don't kill it yet. Run consumer-groups
    
        `bin\windows\kafka-consumer-groups.bat --bootstrap-server 127.0.0.1:9092 --list`
    
    * In the list above, pick the one with groupId like "console-consumer-%"
    
        `bin\windows\kafka-consumer-groups.bat --bootstrap-server 127.0.0.1:9092 --group console-consumer-18941 --describe`
    
    * Observe that consumer group is "aware" of partitions within a topic. That is the reason why, in consumer, 
    IBM gets displayed in order - i.e. starting from 2010 to 2020. i.e. within partition, the data is guaranteed to be in inserted order 
### Focus Points

* Understand synchronous and asynchronous Kafka producer API.
* You would need Serializer and Deserializer for StockPrice. You would definitely run into issues with deserializing LocalDate. Register `com.fasterxml.jackson.datatype.jsr310.JavaTimeModule` to `ObjectMapper`
* Add a callback and log the metadata - The message that you produced, which partition did it go to? and what is the offset?
* First produce the messages without key.
* Produce messages with key. Observe that messages with the same key goes to the same partition.
* Observe that offset is increasing within the partition.
* Are you wondering what happens when a price written to DB did not make it to Kafka? Then you are going in the right direction. Put that thought on hold for a little while. We will get there soon.

### Checkpoint - Topic, Partition and Offset

* Offset is meaningful only within a partition.
* Order is guaranteed only within a partition - not across partitions.
* Once a data is written to a partition, it cannot be changed - Immutability.
* Data is assigned randomly to a partition, unless a key is provided.

--------------