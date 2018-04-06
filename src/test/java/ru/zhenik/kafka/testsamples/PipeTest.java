package ru.zhenik.kafka.testsamples;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PipeTest {
  private final String SERVER_MOCK = "lol:11111";
  private TopologyTestDriver testDriver;
  private StringDeserializer stringDeserializer = new StringDeserializer();
  private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());


  @Before
  public void setup() {
    final StreamsBuilder builder = new StreamsBuilder();
    Topology topology = Pipe.getTopology(builder);

    Properties config = new Properties();
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_MOCK);
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, Pipe.APP_ID);
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    testDriver = new TopologyTestDriver(topology, config);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }


  @Test
  public void testTopologyLogic() {
    // Arrange
    String value = "olololo asdasd";
    testDriver.pipeInput(recordFactory.create(Pipe.INPUT_TOPIC, "", value, 9999L));

    // Assert
    OutputVerifier.compareKeyValue(testDriver.readOutput(Pipe.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", value);
  }
}