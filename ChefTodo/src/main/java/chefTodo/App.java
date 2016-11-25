package chefTodo;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@ComponentScan
@EnableAutoConfiguration
@EnableDiscoveryClient
@Component
@ImportResource("context.xml")
public class App {

	@Autowired
	@Qualifier("messageDispatcher")
	private MessageDispatcher md;

	public App() {
	}

	@PostConstruct
	public void init() {
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
