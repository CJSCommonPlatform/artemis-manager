# artemis-manager

* Browse DLQ

_java -jar artemis-manager.jar browse -host localhost -port 3000 -brokerName 0.0.0.0_

* Remove message by id

_java -jar artemis-manager.jar remove -host localhost -port 3000 -brokerName 0.0.0.0 -msgId 12d8e63e-c842-11e6-986d-00e1000074d2_

* Remove multiple messages (provide list of message ids on input)

_echo msgId1 msgId2 | java -jar artemis-manager.jar remove -host localhost -port 3000 -brokerName 0.0.0.0_

* Chaining commands

This will remove all messages from DLQ that have been originally sent to the queue abracadabra

_java -jar target/artemis-manager.jar browse -host localhost -port 3000 -brokerName 0.0.0.0 | jgrep originalDestination=jms.queue.abracadabra -s msgId | java -jar target/artemis-manager.jar remove -host localhost -port 3000 -brokerName 0.0.0.0_