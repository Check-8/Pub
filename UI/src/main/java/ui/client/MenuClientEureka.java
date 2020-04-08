package ui.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ui.ItemTodo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MenuClientEureka implements MenuClient {
	private final Logger log = LoggerFactory.getLogger(MenuClient.class);

	private RestTemplate restTemplate;
	private String menuServiceHost;
	private long menuServicePort;
	private boolean useRibbon;

	private LoadBalancerClient loadBalancer;

	private List<ItemTodo> oldmenu;
	private Lock menuLock;

	@Autowired
	public MenuClientEureka(@Value("${menu.service.host:menu}") String menuServiceHost,
			@Value("${menu.service.port:8080}") long menuServicePort,
			@Value("${ribbon.eureka.enabled:false}") boolean useRibbon) {
		this.restTemplate = getRestTemplate();
		this.menuServiceHost = menuServiceHost;
		this.menuServicePort = menuServicePort;
		this.useRibbon = useRibbon;
		oldmenu = Collections.emptyList();
		menuLock = new ReentrantLock();
	}

	@Autowired(required = false)
	public void setLoadBalancer(LoadBalancerClient loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	protected RestTemplate getRestTemplate() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));
		converter.setObjectMapper(mapper);

		return new RestTemplate(Collections.<HttpMessageConverter<?>>singletonList(converter));
	}

	private String MenuURL() {
		String url;
		if (useRibbon) {
			ServiceInstance instance = loadBalancer.choose("MENU");
			url = "http://" + instance.getHost() + ":" + instance.getPort() + "/";
		} else {
			url = "http://" + menuServiceHost + ":" + menuServicePort + "/";
		}
		log.trace("Catalog: URL {} ", url);
		return url;
	}

	@Scheduled(fixedDelay = 30000)
	public void loadMenu() {
		ParameterizedTypeReference<List<ItemTodo>> type = null;
		type = new ParameterizedTypeReference<List<ItemTodo>>() {
		};
		ResponseEntity<List<ItemTodo>> resp = null;
		try {
			resp = restTemplate.exchange(MenuURL() + "menu", HttpMethod.GET, null, type);
			menuLock.lock();
			oldmenu = resp.getBody();
		} catch (RestClientException e) {
			log.error("Unable to connect to menu", e);
		} finally {
			menuLock.unlock();
		}
	}

	@Override
	public List<ItemTodo> getMenu() {
		try {
			menuLock.lock();
			if (oldmenu.isEmpty())
				loadMenu();
		} finally {
			menuLock.unlock();
		}
		return new ArrayList<>(oldmenu);

	}
}
