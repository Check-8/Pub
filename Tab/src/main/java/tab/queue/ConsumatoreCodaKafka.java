package tab.queue;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import tab.events.Event;
import tab.events.EventName2Class;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumatoreCodaKafka extends ConsumatoreCoda {
	private static Logger logger = LoggerFactory.getLogger(ConsumatoreCodaKafka.class);

	private static ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Value("classpath:consumer.properties")
	private Resource consumerResource;

	private KafkaConsumer<String, String> consumer;
	private ExecutorService exec;

	@Autowired
	@Qualifier("eventname2class")
	private EventName2Class en2c;

	public ConsumatoreCodaKafka() {

	}

	@PostConstruct
	public void init() {
		Properties prop = new Properties();
		try (InputStream is = consumerResource.getInputStream()) {
			prop.load(is);
		} catch (IOException e) {
			logger.error("Could not load consumer configuration", e);
		}
		consumer = new KafkaConsumer<>(prop);
		consumer.subscribe(List.of("TAB_EVENT"));
		exec = Executors.newSingleThreadExecutor();
		exec.execute(new Consumatore());
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

						try {
							map = objectMapper.readValue(json, Map.class);
							Class<? extends Event> eventClass = null;
							eventClass = en2c.name2class((String) map.get("eventName"));
							event = objectMapper.convertValue(map, eventClass);
						} catch (IOException e) {
							logger.error("Error while parsing message", e);
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
}
