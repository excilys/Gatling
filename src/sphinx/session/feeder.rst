.. _feeder:

#######
Feeders
#######

Feeder is a type alias for ``Iterator[Map[String, T]]``, meaning that the component created by the feed method will poll ``Map[String, T]`` records and inject its content.

It's very simple to build a custom one. For example, here's how one could build a random email generator::

  val random = new util.Random
  val feeder = Iterator.continually(Map("email" -> random.nextString(20) + "@foo.com"))


The structure DSL provides a ``feed`` method.
::

  .feed(feeder)


This defines a workflow step where **every virtual user** feed on the same Feeder.

Every time a virtual user reaches this step, it will pop a record out of the Feeder, and the Session will be injected the record content, result in a new Session instance.


If the Feeder can't produce enough records, Gatling will complain about it and your simulation will stop.

.. _feeder-builder:

RecordArrayFeederBuilder
========================

An ``Array[Map[String, T]]`` can be implicitly turned into a Feeder.
Moreover, this implicit conversion also provides some additional methods for defining the way the Array is iterated over::

  .queue    // default behavior: use an Iterator on the underlying array
  .random   // randomly pick an enry in the array
  .circular // go back to the top of the array once the end is reached

For example::

  val feeder = Array(Map("foo" -> "foo1", "bar" -> "bar1"),
                     Map("foo" -> "foo2", "bar" -> "bar2"),
                     Map("foo" -> "foo3", "bar" -> "bar3")).random


.. _feeder-fileparser:

File parser feeders
===================

Gatling provides several builtins for reading character-separated values files.

Files are expected to be placed in the `data` directory in Gatling distribution. This location can be overridden, see Configuration chapter.

Our parser respects `RFC4180 <https://www.ietf.org/rfc/rfc4180.txt>`_, so don't expect behaviors that don't honor this specification.

For example, a very classic pitfall is trailing spaces in header names: they don't get trimmed.

Besides escaping features described in the RFC, one can use a ``\`` character and escape characters that would match the separator or the double quotes.
::

  val csvFeeder = csv("foo.csv") // use a comma separator
  val tsvFeeder = tsv("foo.tsv") // use a tabulation separator
  val ssvFeeder = csv("foo.ssv") // use a semicolon separator
  val customSeparatorFeeder = separatedValues("foo.txt", "#") // use your own separator

Those built-ins returns ``RecordArrayFeederBuilder`` instances, meaning that the whole file is loaded in memory and parsed, so the resulting feeders doesn't read on disk during the simulation run.

.. _feeder-jdbc:

JDBC feeder
===========

Gatling also provide a builtin that reads from a JDBC connection.
::

  jdbcFeeder(databaseURL: String, username: String, password: String, sql: String)

Just like File parser built-ins, this return a `RecordArrayFeederBuilder` instance.

    * The databaseURL must be a JDBC URL (e.g. jdbc:postgresql:gatling),
    * the username and password are the credentials to access the database,
    * sql is the request that will get the values needed.

Only JDBC4 drivers are supported, so that they automatically registers to the DriverManager.

.. note::
    Do not forget to add the required JDBC driver jar in the classpath (``lib`` folder in the bundle)

.. _feeder-redis:

Redis feeder
============

This feature was originally contributed by Krishnen Chedambarum.

Gatling can read data from Redis using one of the following Redis commands.

* LPOP - remove and return the first element of the list
* SPOP - remove and return a random element from the set
* SRANDMEMBER - return a random element from the set

By default RedisFeeder uses LPOP command::

  import com.redis._
  import serialization._
  import io.gatling.redis.feeder.RedisFeeder
  
  val redisPool = new RedisClientPool("localhost", 6379)
  
  // use a list, so there's one single value per record, which is here named "foo"
  val feeder = RedisFeeder(redisPool, "foo")

An optional third parameter is used to specify desired Redis command::

  // read data using SPOP command from a set named "foo"
  val feeder = RedisFeeder(clientPool, "foo", RedisFeeder.SPOP)


Note that since v2.1.14, Redis supports mass insertion of data from a `file <http://redis.io/topics/mass-insert>`_.
It is possible to load millions of keys in a few seconds in Redis and Gatling will read them off memory directly.

For example: a simple Scala function to generate a file with 1 million different urls ready to be loaded in a Redis list named *URLS*::

  import io.gatling.core.feeder.redis.util._

  def generateOneMillionUrls() = {
    val writer = new PrintWriter(new File("/tmp/loadtest.txt"))
    try {
      for (i <- 0 to 1000000) {
        val url = "test?id=" + i
        // note the list name "URLS" here
        writer.write(generateRedisProtocol("LPUSH", "URLS", url))
      }
    } finally {
       writer.close
    }
  }

The urls can then be loaded in Redis using the following command::

  `cat /tmp/loadtest.txt | redis-cli --pipe`

.. _feeder-non-shared:

Non Shared Data
===============

Sometimes, Gatling users still want all virtual users to play all the records in a file, and Feeder doesn't match this behavior.

Still, it's quite easy to build, thanks to :ref:`flattenMapIntoAttributes <scenario-exec-function-flatten>`  e.g.::

  val array = csv("foo.csv").array

  foreach(array, "record") {
    exec(flattenMapIntoAttributes("${record}"))
    ...
  }

