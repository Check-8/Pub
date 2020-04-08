package tab.queue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import tab.events.Event;

import javax.annotation.PostConstruct;

public class ProduttoreCodaKafka implements ProduttoreCoda {
	private static final Logger logger = LoggerFactory.getLogger(ProduttoreCodaKafka.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Value("classpath:producer.properties")
	private Resource producerResource;

	private KafkaProducer<String, String> producer;

	public ProduttoreCodaKafka() {

	}

	@PostConstruct
	public void init() {
		Properties prop = new Properties();
		try (InputStream is = producerResource.getInputStream()) {
			prop.load(is);
		} catch (IOException e) {
			logger.error("Could not load configuration", e);
		}
		producer = new KafkaProducer<>(prop);
	}

	public void aggiungi(Event event) {
		try {
			String jsonInString = objectMapper.writeValueAsString(event);
			ProducerRecord<String, String> pr = new ProducerRecord<>("TAB_EVENT", jsonInString);
			logger.info("{} {}", pr.topic(), pr.value());
			producer.send(pr);
		} catch (JsonProcessingException e) {
			logger.error("Could not convert event to json", e);
		}
	}

	public void post(Collection<Event> events) {
		for (Event event : events) {
			aggiungi(event);
		}
	}

}
