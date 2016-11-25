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
import com.google.common.io.Resources;

import tab.events.Event;

public class ProduttoreCodaKafka implements ProduttoreCoda {
	private static Logger logger = LoggerFactory.getLogger(ProduttoreCodaKafka.class);

	private KafkaProducer<String, String> producer;
	private ObjectMapperHolder omh;

	public ProduttoreCodaKafka() {
		Properties prop = new Properties();
		try (InputStream is = Resources.getResource("producer.properties").openStream()) {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		producer = new KafkaProducer<>(prop);
		omh = new ObjectMapperHolder();
	}

	public void aggiungi(Event event) {
		ProducerRecord<String, String> pr = null;

		ObjectMapper mapper = omh.getMapper();

		try {
			String jsonInString = mapper.writeValueAsString(event);
			pr = new ProducerRecord<String, String>("TAB_EVENT", jsonInString);
			logger.info(pr.topic() + " " + pr.value());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		producer.send(pr);
	}

	public void post(Collection<Event> events) {
		for (Event event : events) {
			aggiungi(event);
		}
	}

	private class ObjectMapperHolder {
		private ObjectMapper objectMapper;

		public ObjectMapperHolder() {
			objectMapper = new ObjectMapper();
		}

		public ObjectMapper getMapper() {
			return objectMapper;
		}
	}

}
