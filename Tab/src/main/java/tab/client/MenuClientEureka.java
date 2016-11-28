package tab.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import tab.events.OrderedItem;

public class MenuClientEureka implements MenuClient {
	private final Logger log = LoggerFactory.getLogger(MenuClient.class);

	private RestTemplate restTemplate;
	private String menuServiceHost;
	private long menuServicePort;
	private boolean useRibbon;

	private LoadBalancerClient loadBalancer;

	private Map<Integer, OrderedItem> menuNumber2item;
	private Lock lock;

	@Autowired
	public MenuClientEureka(@Value("${menu.service.host:menu}") String menuServiceHost,
			@Value("${menu.service.port:8080}") long menuServicePort,
			@Value("${ribbon.eureka.enabled:false}") boolean useRibbon) {
		this.restTemplate = getRestTemplate();
		this.menuServiceHost = menuServiceHost;
		this.menuServicePort = menuServicePort;
		this.useRibbon = useRibbon;
		menuNumber2item = new HashMap<>();
		lock = new ReentrantLock();
	}

	@Autowired(required = false)
	public void setLoadBalancer(LoadBalancerClient loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	protected RestTemplate getRestTemplate() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new Jackson2HalModule());

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
	public void getMenu() {
		ParameterizedTypeReference<List<OrderedItem>> type = null;
		type = new ParameterizedTypeReference<List<OrderedItem>>() {
		};
		ResponseEntity<List<OrderedItem>> resp = null;
		try {
			resp = restTemplate.exchange(MenuURL() + "menu", HttpMethod.GET, null, type);
			List<OrderedItem> menu = resp.getBody();
			lock.lock();
			menuNumber2item = menu.stream().collect(Collectors.toMap(OrderedItem::getMenuNumber, Function.identity()));
		} catch (RestClientException e) {
			log.error("Failed to load menu", e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	@HystrixCommand(fallbackMethod = "defaultMenuItem")
	public OrderedItem getMenuItem(Integer menuNumber) {
		OrderedItem oi = restTemplate.getForObject(MenuURL() + menuNumber, OrderedItem.class);
		try {
			lock.lock();
			menuNumber2item.put(menuNumber, oi);
		} finally {
			lock.unlock();
		}
		return oi;
	}

	public OrderedItem defaultMenuItem(Integer menuNumber) {
		try {
			lock.lock();
			return menuNumber2item.get(menuNumber);
		} finally {
			lock.unlock();
		}
	}
}
