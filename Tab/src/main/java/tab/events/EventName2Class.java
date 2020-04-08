package tab.events;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Qualifier("eventname2class")
public class EventName2Class {
	private static final Logger logger = LoggerFactory.getLogger(EventName2Class.class);

	@Value("classpath:eventname.properties")
	private Resource eventnamesResource;

	private Properties properties;

	public EventName2Class() {
	}

	@PostConstruct
	public void init() {
		try (InputStream inStream = eventnamesResource.getInputStream()) {
			properties = new Properties();
			properties.load(inStream);
		} catch (IOException e) {
			logger.error("Could not load configuration", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Event> name2class(String name) throws ClassNotFoundException {
		String classname = properties.getProperty(name);
		return (Class<? extends Event>) Class.forName(classname);
	}

}
