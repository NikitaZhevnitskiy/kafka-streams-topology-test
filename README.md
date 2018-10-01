# Examples: how to test kafka streams topologies
1. unit // todo add more samples
2. integration // todo
3. end2end (testcontainers, etc ...) // todo 

## Kafka docker
UP from docker folder
```
docker-compose -f kafka.yml up -d
```


## Refs
* [Testing a Streams Application | Kafka 1.0](https://kafka.apache.org/11/documentation/streams/developer-guide/testing.html)
* [John-Michael Reed git project Java/Scala | kafka 0.10](https://github.com/JohnReedLOL/kafka-streams)


## Kafka
kafka-topics --zookeeper zoo1:2181 --topic dummy_topic --create --replication-factor 3 --partitions 12  

kafka-console-producer --broker-list kafka1:9092 --topic dummy_topic

kafka-console-consumer --bootstrap-server kafka1:9092 --topic dummy_topic --from-beginning