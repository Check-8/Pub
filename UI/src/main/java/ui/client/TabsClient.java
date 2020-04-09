package ui.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabsClient implements TabsInterface {
    private static final Logger log = LoggerFactory.getLogger(TabsClient.class);

    private final RestTemplate restTemplate;
    private final String tabServiceHost;
    private final long tabServicePort;
    private final boolean useRibbon;

    private LoadBalancerClient loadBalancer;

    @Autowired
    public TabsClient(@Value("${tab.service.host:tabs}") String tabServiceHost,
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

        var request = new HttpEntity<>(map, headers);
        log.info(request.toString());
        return request;
    }

    @Override
    public void openTab(int tableNumber, String waiter) {
        Map<String, Object> map = new HashMap<>();
        map.put("tableNumber", tableNumber);
        map.put("waiter", waiter);

        var response = restTemplate.postForEntity(tabURL() + "tabs", makeJsonRequest(map), Void.class);
        log.info(response.getStatusCode()
                         .toString());
    }

    @Override
    public void closeTab(long id, double amount_paid) {
        Map<String, Object> map = Map.of("amountPaid", amount_paid);

        restTemplate.exchange(tabURL() + "tabs/" + id + "?amountPaid=" + amount_paid, HttpMethod.DELETE, makeJsonRequest(map), Void.class);
    }

    @Override
    public void placeOrder(long id, int[] orderedItem) {
        Map<String, Object> map = Map.of("id", id, "orderItems", orderedItem);

        var response = restTemplate.postForEntity(tabURL() + "orders", makeJsonRequest(map), Void.class);
        log.info(response.getStatusCode()
                         .toString());
    }

    @Override
    public void markDrinksServed(long id, int[] drinksServed) {
        Map<String, Object> map = Map.of("id", id, "drinksServed", drinksServed);

        var request = makeJsonRequest(map);
        var response = restTemplate.postForEntity(tabURL() + "drinks", request, Void.class);
        log.info(response.getStatusCode()
                         .toString());
    }

    @Override
    public void markFoodPrepared(long id, int[] foodPrepared) {
        Map<String, Object> map = Map.of("id", id, "foodItems", foodPrepared, "state", "PREPARED");

        var request = makeJsonRequest(map);
        var response = restTemplate.postForEntity(tabURL() + "foods", request, Void.class);
        log.info(response.getStatusCode()
                         .toString());
    }

    @Override
    public void markFoodServed(long id, int[] foodServed) {
        Map<String, Object> map = Map.of("id", id, "foodItems", foodServed, "state", "SERVED");

        var request = makeJsonRequest(map);
        var response = restTemplate.postForEntity(tabURL() + "foods", request, Void.class);
        log.info(response.getStatusCode()
                         .toString());
    }

}
