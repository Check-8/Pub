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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import ui.ItemTodo;
import ui.TableTodo;

import java.util.List;
import java.util.Map;

public class OpenTabClient implements OpenTabInteface {
    private static final Logger log = LoggerFactory.getLogger(OpenTabClient.class);

    private final RestTemplate restTemplate;
    private final String openServiceHost;
    private final long openServicePort;
    private final boolean useRibbon;

    private LoadBalancerClient loadBalancer;

    @Autowired
    public OpenTabClient(@Value("${open.service.host:opentabs}") String openServiceHost,
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

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(MediaTypes.HAL_JSON));
        converter.setObjectMapper(mapper);

        return new RestTemplate(List.of(converter));
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
        return restTemplate.getForObject(openURL() + "tab/" + id, TableTodo.class);
    }

    @Override
    public TableTodo getByTableNumber(int tableNumber) {
        var type = new ParameterizedTypeReference<List<TableTodo>>() {
        };
        var response = restTemplate.exchange(openURL() + "tab?tableNumber=" + tableNumber, HttpMethod.GET, null, type);
        List<TableTodo> list = response.getBody();
        if (list == null) return null;
        return list.stream()
                   .findAny()
                   .orElse(null);
    }

    @Override
    public Map<Integer, List<ItemTodo>> getTodoListByWaiter(String waiter) {
        var type = new ParameterizedTypeReference<Map<Integer, List<ItemTodo>>>() {
        };
        var resp = restTemplate.exchange(openURL() + "tab/" + waiter, HttpMethod.GET, null, type);
        return resp.getBody();
    }

    @Override
    public Double getToPayByTableNumber(int tableNumber) {
        return restTemplate.getForObject(openURL() + "tab/topay/" + tableNumber, Double.class);
    }
}
