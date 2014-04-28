.. _jms:

###
JMS
###

JMS support was initially contributed by Jason Koch.

Prerequisites
=============

Gatling JMS DSL is not available by default.

One has to manually add the following imports::

  import io.gatling.jms.Predef._
  import javax.jms._

JMS Protocol
============

.. _jms-protocol:

Use the ``jms`` object in order to create a JMS protocol.

* ``connectionFactoryName``: mandatory
* ``url``: mandatory
* ``contextFactory``: mandatory
* ``credentials``: optional
* ``listenerCount``: the number of ReplyConsumers. mandatory (> 0)
* ``useNonPersistentDeliveryMode``/``usePersistentDeliveryMode``: optional, default to non persistent

JMS Request API
===============

.. _jms-request:

Use the ``jms("requestName")`` method in order to create a JMS request.

Request Type
------------

Currently, only ``reqreply`` request type is supported.

Destination
-----------

Define the target destination with ``queue("queueName")`` or alternatively with ``destination(JmsDestination)``

Optionally define reply destination with ``replyQueue("responseQueue")`` or ``replyDestination(JmsDestination)`` if not defined dynamic queue will be used.

Additionally for reply destination JMS selector can be defined with ``selector("selector")``


Message Matching
----------------

Request/Reply messages are matched using JMS pattern (request JMSMessageID should be return in response as JMSCorrelationID).

If different logic is required, it can be specified using ``messageMatcher(JmsMessageMatcher)``.

Message
-------

* ``textMessage(Expression[String])``
* ``bytesMessage(Expression[Array[Byte]])``
* ``mapMessage(Expression[Map[String, Any]])``
* ``objectMessage(Expression[java.io.Serializable])``

Properties
----------

One can send additional properties with ``property(Expression[String], Expression[Any])``.

JMS Check API
=============

.. _jms-api:

JMS checks are very basic for now.

There is ``simpleCheck`` that accepts just ``javax.jms.Message => Boolean`` functions.

There is also ``xpath`` check for ``javax.jms.TextMessage`` that carries XML content.

Additionally you can define your custom check that implements ``Check[javax.jms.Message]``

Example
=======

Short example, assuming FFMQ on localhost, using a reqreply query, to the queue named "jmstestq"::

  import net.timewalker.ffmq3.FFMQConstants
  import io.gatling.core.Predef._
  import io.gatling.jms.Predef._
  import javax.jms._
  import scala.concurrent.duration._

  class TestJmsDsl extends Simulation {

    val jmsConfig = JmsProtocolBuilder.default
      .connectionFactoryName(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME)
      .url("tcp://localhost:10002")
      .credentials("user", "secret")
      .contextFactory(FFMQConstants.JNDI_CONTEXT_FACTORY)
      .listenerCount(1)
      .usePersistentDeliveryMode

    val scn = scenario("JMS DSL test").repeat(1) {
      exec(jms("req reply testing").reqreply
      .queue("jmstestq")
      .textMessage("hello from gatling jms dsl")
      .property("test_header", "test_value")
      .check(simpleCheck(checkBodyTextCorrect))
      )
    }

    setUp(scn.inject(rampUsersPerSec(10) to (1000) during (2 minutes)))
      .protocols(jmsConfig)

    def checkBodyTextCorrect(m: Message) = {
      // this assumes that the service just does an "uppercase" transform on the text
      m match {
      case tm: TextMessage => tm.getText.toString == "HELLO FROM GATLING JMS DSL"
      case _ => false
      }
    }
  }

