package openTabs.queue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import openTabs.events.Event;
import openTabs.events.EventName2Class;


public class ConsumatoreCodaKafka extends ConsumatoreCoda {
	private static Logger logger = LoggerFactory.getLogger(ConsumatoreCodaKafka.class);

	private KafkaConsumer<String, String> consumer;
	private ObjectMapperHolder omh;
	private ExecutorService exec;

	@Autowired
	@Qualifier("eventname2class")
	private EventName2Class en2c;

	public ConsumatoreCodaKafka() {
		Properties prop = new Properties();
		try (InputStream is = Resources.getResource("consumer.properties").openStream()) {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		consumer = new KafkaConsumer<>(prop);
		consumer.subscribe(Arrays.asList("TAB_EVENT"));
		exec = Executors.newSingleThreadExecutor();
		exec.execute(new Consumatore());
		omh = new ObjectMapperHolder();
	}

	private class Consumatore implements Runnable {

		private static final long TIMEOUT = 1000;

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			while (true) {
				try {
					ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
					for (ConsumerRecord<String, String> record : records) {
						logger.info(record.topic() + " " + record.value());
						String json = record.value();

						Event event = null;
						Map<String, Object> map = null;

						ObjectMapper mapper = omh.getMapper();
						try {
							map = mapper.readValue(json, Map.class);
							Class<? extends Event> eventClass = null;
							eventClass = en2c.name2class((String) map.get("eventName"));
							event = mapper.convertValue(map, eventClass);
						} catch (JsonGenerationException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						for (Subscriber sub : getSubscriber()) {
							sub.onEvent(event);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class ObjectMapperHolder {
		private ObjectMapper objectMapper;

		public ObjectMapperHolder() {
			objectMapper = new ObjectMapper();
			objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		public ObjectMapper getMapper() {
			return objectMapper;
		}
	}
}
