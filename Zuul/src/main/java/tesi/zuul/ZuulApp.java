package tesi.zuul;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

import tesi.zuul.prefilter.LogRequest;

@SpringBootApplication
@EnableZuulProxy
public class ZuulApp {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ZuulApp.class).web(true).run(args);
	}

	public LogRequest logRequestFilter() {
		return new LogRequest();
	}

}