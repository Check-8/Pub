package openTabs.events;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
@Qualifier("eventname2class")
public class EventName2Class {
    @Value("classpath:eventname.properties")
    private Resource eventnamesResource;

    private Properties properties;

    private String basePackage;

    public EventName2Class() {

    }

    @PostConstruct
    public void init() {
        try (InputStream inStream = eventnamesResource.getInputStream()) {
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
