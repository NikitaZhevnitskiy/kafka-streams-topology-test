## kafka scripts

### Topics
`kafka-topics --create --zookeeper zoo1:2181 --replication-factor 1 --partitions 1 --topic bank-transaction`
`kafka-topics --create --zookeeper zoo1:2181 --replication-factor 1 --partitions 1 --topic bank-balance-exactly-once --config cleanup.policy=compact`

### Console consumers
```
kafka-console-consumer --bootstrap-server kafka1:9092 \
    --topic bank-balance-exactly-once \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --consumer-property print.key=true \
    --consumer-property print.value=true \
    --consumer-property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --consumer-property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```
```
kafka-console-consumer --from-beginning --bootstrap-server kafka1:9092 --topic bank-transactions
```
