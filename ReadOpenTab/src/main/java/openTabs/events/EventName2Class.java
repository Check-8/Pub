package openTabs.events;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.io.Resources;

@Component
@Qualifier("eventname2class")
public class EventName2Class {
	private Properties properties;

	private String basePackage;

	public EventName2Class() {
		try (InputStream inStream = Resources.getResource("eventname.properties").openStream();) {
			properties = new Properties();
			properties.load(inStream);
			basePackage = properties.getProperty("base_package");
			if (basePackage == null)
				basePackage = "";
			else
				basePackage += ".";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Event> name2class(String name) throws ClassNotFoundException {
		String classname = basePackage + properties.getProperty(name);
		return (Class<? extends Event>) Class.forName(classname);
	}

}
