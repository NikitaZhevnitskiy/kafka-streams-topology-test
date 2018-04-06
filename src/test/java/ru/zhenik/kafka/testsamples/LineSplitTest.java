package ru.zhenik.kafka.testsamples;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
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


public class LineSplitTest {
  private final String SERVER_MOCK = "lol:11111";
  private TopologyTestDriver testDriver;
  private StringDeserializer stringDeserializer = new StringDeserializer();
  private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());


  @Before
  public void setup() {
    final StreamsBuilder builder = new StreamsBuilder();
    Topology topology = LineSplit.getTopology(builder);

    Properties config = new Properties();
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_MOCK);
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, LineSplit.APP_ID);
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    testDriver = new TopologyTestDriver(topology, config);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }


  @Test
  public void testTopologyDriver() {
    // Arrange
    testDriver.pipeInput(recordFactory.create(LineSplit.INPUT_TOPIC, "", "very long string line   ", 9999L));

    // Act
    List<ProducerRecord<String, String>> outputRecords = getRecords(LineSplit.OUTPUT_TOPIC);

    // Assert
    assertEquals(4, outputRecords.size());
  }

  @Test
  public void testTopologyLogic() {
    // Arrange
    testDriver.pipeInput(recordFactory.create(LineSplit.INPUT_TOPIC, "", "very long string line", 9999L));

    // Assert
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "very");
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "long");
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "string");
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "line");

  }

  private List<ProducerRecord<String,String>> getRecords(String topic){
    ProducerRecord<String, String> record;
    ArrayList<ProducerRecord<String, String>> list = new ArrayList<>();
    do {
      record = testDriver.readOutput(topic, stringDeserializer, stringDeserializer);
      if (record!=null){
        list.add(record);
      }
    }
    while (record!=null);
    return list;
  }

}