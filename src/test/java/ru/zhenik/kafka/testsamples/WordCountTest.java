package ru.zhenik.kafka.testsamples;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.protocol.types.Field.Str;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WordCountTest {
  private final String SERVER_MOCK = "lol:11111";
  private TopologyTestDriver testDriver;
  private KeyValueStore<String, Long> store;
  private StringDeserializer stringDeserializer = new StringDeserializer();
  private LongDeserializer longDeserializer = new LongDeserializer();
  private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());


  @Before
  public void setup() {
    final StreamsBuilder builder = new StreamsBuilder();
    Topology topology = WordCount.getTopology(builder);
    Properties config = new Properties();
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_MOCK);
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, WordCount.APP_ID);
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    testDriver = new TopologyTestDriver(topology, config);
    store=testDriver.getKeyValueStore(WordCount.STORE_NAME);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }

  @Test
  public void testTopology() {
    // Arrange
    List<String> values = Arrays.asList(
        "streams is amazing",
        "kafka streams",
        "kafka");
    writeToTopic(WordCount.INPUT_TOPIC, values);

    // Assert
    assertEquals(2, store.get("streams").longValue());
  }

  private void writeToTopic(String topic, List<String> values) {
    for (String value : values) {
      testDriver.pipeInput(recordFactory.create(topic, "", value, 9999L));
    }
  }


}