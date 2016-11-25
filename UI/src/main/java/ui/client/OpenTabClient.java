package ui.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ui.ItemTodo;
import ui.TableTodo;

public class OpenTabClient implements OpenTabInteface {
	private final Logger log = LoggerFactory.getLogger(OpenTabClient.class);

	private RestTemplate restTemplate;
	private String openServiceHost;
	private long openServicePort;
	private boolean useRibbon;

	private LoadBalancerClient loadBalancer;

	@Autowired
	public OpenTabClient(@Value("${open.service.host:opentab}") String openServiceHost,
			@Value("${open.service.port:8080}") long openServicePort,
			@Value("${ribbon.eureka.enabled:false}") boolean useRibbon) {
		this.restTemplate = getRestTemplate();
		this.openServiceHost = openServiceHost;
		this.openServicePort = openServicePort;
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
		converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));
		converter.setObjectMapper(mapper);

		RestTemplate rest = new RestTemplate(Collections.<HttpMessageConverter<?>>singletonList(converter));
		return rest;
	}

	private String openURL() {
		String url;
		if (useRibbon) {
			ServiceInstance instance = loadBalancer.choose("OPENTAB");
			url = "http://" + instance.getHost() + ":" + instance.getPort() + "/";
		} else {
			url = "http://" + openServiceHost + ":" + openServicePort + "/";
		}
		log.trace("Catalog: URL {} ", url);
		return url;
	}

	@Override
	public TableTodo getByTab(long id) {
		return restTemplate.getForObject(openURL() + "tab/id/" + id, TableTodo.class);
	}

	@Override
	public TableTodo getByTableNumber(int tableNumber) {
		return restTemplate.getForObject(openURL() + "tab/table/" + tableNumber, TableTodo.class);
	}

	@Override
	public Map<Integer, List<ItemTodo>> getTodoListByWaiter(String waiter) {
		ParameterizedTypeReference<Map<Integer, List<ItemTodo>>> type = null;
		type = new ParameterizedTypeReference<Map<Integer, List<ItemTodo>>>() {
		};
		ResponseEntity<Map<Integer, List<ItemTodo>>> resp = null;
		resp = restTemplate.exchange(openURL() + "tab/" + waiter, HttpMethod.GET, null, type);
		return resp.getBody();
	}

	@Override
	public Double getToPayByTableNumber(int tableNumber) {
		return restTemplate.getForObject(openURL() + "tab/topay/" + tableNumber, Double.class);
	}
}
