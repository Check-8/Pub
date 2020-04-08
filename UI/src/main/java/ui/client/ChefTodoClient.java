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
import org.springframework.web.client.RestTemplate;
import ui.TodoGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChefTodoClient implements ChefTodoInterface {
	private static final Logger log = LoggerFactory.getLogger(ChefTodoClient.class);

	private RestTemplate restTemplate;
	private String chefServiceHost;
	private long chefServicePort;
	private boolean useRibbon;

	private LoadBalancerClient loadBalancer;

	@Autowired
	public ChefTodoClient(@Value("${chef.service.host:chef}") String chefServiceHost,
			@Value("${chef.service.port:8080}") long chefServicePort,
			@Value("${ribbon.eureka.enabled:false}") boolean useRibbon) {
		this.restTemplate = getRestTemplate();
		this.chefServiceHost = chefServiceHost;
		this.chefServicePort = chefServicePort;
		this.useRibbon = useRibbon;
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

	private String chefURL() {
		String url;
		if (useRibbon) {
			ServiceInstance instance = loadBalancer.choose("CHEFTODO");
			url = "http://" + instance.getHost() + ":" + instance.getPort() + "/";
		} else {
			url = "http://" + chefServiceHost + ":" + chefServicePort + "/";
		}
		log.trace("Catalog: URL {} ", url);
		return url;
	}

	@Override
	public List<TodoGroup> getTodoList() {
		ParameterizedTypeReference<List<TodoGroup>> type = null;
		type = new ParameterizedTypeReference<List<TodoGroup>>() {
		};
		ResponseEntity<List<TodoGroup>> resp = null;
		resp = restTemplate.exchange(chefURL() + "chef", HttpMethod.GET, null, type);
		return resp.getBody();
	}
}
