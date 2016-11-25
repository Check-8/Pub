package ui.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

public class MenuClientEureka implements MenuClient {
	private final Logger log = LoggerFactory.getLogger(MenuClient.class);

	private RestTemplate restTemplate;
	private String menuServiceHost;
	private long menuServicePort;
	private boolean useRibbon;

	private LoadBalancerClient loadBalancer;

	@Autowired
	public MenuClientEureka(@Value("${menu.service.host:menu}") String menuServiceHost,
			@Value("${menu.service.port:8080}") long menuServicePort,
			@Value("${ribbon.eureka.enabled:false}") boolean useRibbon) {
		this.restTemplate = getRestTemplate();
		this.menuServiceHost = menuServiceHost;
		this.menuServicePort = menuServicePort;
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

	@Override
	public List<ItemTodo> getMenu() {
		ParameterizedTypeReference<List<ItemTodo>> type = null;
		type = new ParameterizedTypeReference<List<ItemTodo>>() {
		};
		ResponseEntity<List<ItemTodo>> resp = null;
		resp = restTemplate.exchange(MenuURL() + "menu", HttpMethod.GET, null, type);
		return resp.getBody();
	}
}
