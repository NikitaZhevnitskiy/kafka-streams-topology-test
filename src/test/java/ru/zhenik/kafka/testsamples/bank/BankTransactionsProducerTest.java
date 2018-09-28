package ru.zhenik.kafka.testsamples.bank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class BankTransactionsProducerTest {

    @Test
    public void newRandomTransactionsTest(){
        ProducerRecord<String, String> record = BankTransactionsProducer.newRandomTransaction("john");
        String key = record.key();
        String value = record.value();

        assertEquals(key, "john");

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(value);
            assertEquals(node.get("name").asText(), "john");
            assertTrue("Amount should be less than 100", node.get("amount").asInt() < 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(value);

    }


}