package ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TabsClient implements TabsInterface {
	private final Logger log = LoggerFactory.getLogger(TabsClient.class);

	private RestTemplate restTemplate;
	private String tabServiceHost;
	private long tabServicePort;
	private boolean useRibbon;

	private LoadBalancerClient loadBalancer;

	@Autowired
	public TabsClient(@Value("${tab.service.host:tab}") String tabServiceHost,
			@Value("${tab.service.port:8080}") long tabServicePort,
			@Value("${ribbon.eureka.enabled:false}") boolean useRibbon) {
		this.restTemplate = getRestTemplate();
		this.tabServiceHost = tabServiceHost;
		this.tabServicePort = tabServicePort;
		this.useRibbon = useRibbon;
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
		converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, MediaType.APPLICATION_JSON));
		converter.setObjectMapper(mapper);

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(converter);
		converters.add(new FormHttpMessageConverter());

		return new RestTemplate(converters);
	}

	private String tabURL() {
		String url;
		if (useRibbon) {
			ServiceInstance instance = loadBalancer.choose("TAB");
			url = "http://" + instance.getHost() + ":" + instance.getPort() + "/";
		} else {
			url = "http://" + tabServiceHost + ":" + tabServicePort + "/";
		}
		log.trace("Catalog: URL {} ", url);
		return url;
	}

	private HttpEntity<Map<String, Object>> makeJsonRequest(Map<String, Object> map) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = null;
		request = new HttpEntity<>(map, headers);
		log.info(request.toString());
		return request;
	}

	@Override
	public void openTab(int tableNumber, String waiter) {
		Map<String, Object> map = new HashMap<>();
		map.put("table_number", tableNumber);
		map.put("waiter", waiter);

		HttpEntity<Map<String, Object>> request = makeJsonRequest(map);

		ResponseEntity<Void> response = null;
		response = restTemplate.postForEntity(tabURL() + "opentab", request, Void.class);
		log.info(response.getStatusCode().toString());
	}

	@Override
	public void closeTab(long id, double amount_paid) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("amount_paid", amount_paid);

		HttpEntity<Map<String, Object>> request = makeJsonRequest(map);

		ResponseEntity<Void> response = null;
		response = restTemplate.postForEntity(tabURL() + "closetab", request, Void.class);
		log.info(response.getStatusCode().toString());
	}

	@Override
	public void placeOrder(long id, int[] orderedItem) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("ordered_item[]", orderedItem);

		HttpEntity<Map<String, Object>> request = makeJsonRequest(map);

		ResponseEntity<Void> response = null;
		response = restTemplate.postForEntity(tabURL() + "placeorder", request, Void.class);
		log.info(response.getStatusCode().toString());
	}

	@Override
	public void markDrinksServed(long id, int[] drinksServed) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("drinks_served[]", drinksServed);

		HttpEntity<Map<String, Object>> request = makeJsonRequest(map);

		ResponseEntity<Void> response = null;
		response = restTemplate.postForEntity(tabURL() + "markdrinksserved", request, Void.class);
		log.info(response.getStatusCode().toString());
	}

	@Override
	public void markFoodPrepared(long id, int[] foodPrepared) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("food_prepared[]", foodPrepared);

		HttpEntity<Map<String, Object>> request = makeJsonRequest(map);

		ResponseEntity<Void> response = null;
		response = restTemplate.postForEntity(tabURL() + "markfoodprepared", request, Void.class);
		log.info(response.getStatusCode().toString());
	}

	@Override
	public void markFoodServed(long id, int[] foodServed) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("food_served[]", foodServed);

		HttpEntity<Map<String, Object>> request = makeJsonRequest(map);

		ResponseEntity<Void> response = null;
		response = restTemplate.postForEntity(tabURL() + "markfoodserved", request, Void.class);
		log.info(response.getStatusCode().toString());
	}

}
