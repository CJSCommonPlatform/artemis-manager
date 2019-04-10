# Artemis Manager

[![Build Status](https://travis-ci.org/CJSCommonPlatform/artemis-manager.svg?branch=master)](https://travis-ci.org/CJSCommonPlatform/artemis-manager) [![Coverage Status](https://coveralls.io/repos/github/CJSCommonPlatform/artemis-manager/badge.svg?branch=master)](https://coveralls.io/github/CJSCommonPlatform/artemis-manager?branch=master)

## Configuration

Configuration can be supplied in a config file and passed to the application via a '@<config_file_path>' option.

* -jmxUrl: The full JMX url, can be used multiple times for clusters. (default: service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi)
* -brokerName: Name of the broker (default: "default")
* -jmxUsername: User for JMX (default none)
* -jmxPassword: Password for JMX (default none)
* -jmsUrl: The JMS url, you should add the clientID as above. You can also add sslEnabled=true to get SSL capability (default: tcp://localhost:61616?clientID=artemis-manager)
* -jmsUsername: User for JMS (default none)
* -jmsPassword: Password for JMS (default none)

A complicated example configuration file might look like:

Assuming two brokers on 192.168.0.10 and 192.168.0.11, with JMX on 1098 and OpenWire on 61616 (but no security)

```
-jmxUrl
service:jmx:rmi:///jndi/rmi://192.168.0.10:1098/jmxrmi
-jmxUrl
service:jmx:rmi:///jndi/rmi://192.168.0.11:1098/jmxrmi
-jmsUrl
tcp://(192.168.0.10:61616,192.168.0.11:61616)?clientID=artemis-manager&sslEnabled=true
```

In the examples below it is assumed a configuration file of artemis.config has been created like that shown above

## Browse DLQ

**Note: Browse uses JMS to connect to the Artemis broker.**

`java -jar artemis-manager.jar browse @artemis.config`

## List Queues

**Note: ListQueues uses JMS to connect to the Artemis broker.**

`java -jar artemis-manager.jar listqueues @artemis.config`

## List Topics

**Note: ListTopics uses JMS to connect to the Artemis broker.**

`java -jar artemis-manager.jar listtopics @artemis.config`


## Remove Message from DLQ

* Remove message by id

**Note: Remove uses JMX to connect to the Artemis broker.**

`java -jar artemis-manager.jar remove @artemis.config -msgId 12d8e63e-c842-11e6-986d-00e1000074d2`

* Remove multiple messages (provide list of message ids on input)

`echo msgId1 msgId2 | java -jar artemis-manager.jar remove`

## Reprocess Message from DLQ

* Reprocess message by id

**Note: Reprocess uses JMX port to connect to the Artemis broker.**

`java -jar artemis-manager.jar reprocess @artemis.config -msgId 12d8e63e-c842-11e6-986d-00e1000074d2`

* Reprocess multiple messages (provide list of message ids on input)

`echo msgId1 msgId2 | java -jar artemis-manager.jar reprocess @artemis.config`

## Reprocess All Messages from DLQ

* Reprocess all message on DLQ

***This command will reprocess ALL messages on the DLQ***

**Note: ReprocessAll uses JMX port to connect to the Artemis broker.**

`java -jar artemis-manager.jar reprocessall @artemis.config`

## Reports on DLQ

* Generate report on the contents of the DLQ.  There are three different reports available.

**Note: Report uses JMS to connect to the Artemis broker.**

`java -jar artemis-manager.jar browse @artemis.config -report totals-by-name-report`

`java -jar artemis-manager.jar browse @artemis.config -report names-by-original-destination-report`

`java -jar artemis-manager.jar browse @artemis.config -report created-at-name-total-report`

## Chaining Commands

* Chaining commands

This will remove all messages from DLQ that have been originally sent to the queue abracadabra

```
java -jar target/artemis-manager.jar browse @artemis.config |\
 jgrep originalDestination=jms.queue.abracadabra -s msgId |\
 java -jar target/artemis-manager.jar remove @artemis.config
```
