package com.example.observabilitydemo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.example.observabilitydemo.post.JsonPlaceholderService;
import com.example.observabilitydemo.post.Post;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;

@SpringBootApplication
public class ObservabilityDemoApplication {

	private static final Logger log = LoggerFactory.getLogger(ObservabilityDemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityDemoApplication.class, args);
	}

	@Bean
	JsonPlaceholderService jsonPlaceholderService() {
		RestClient restClient = RestClient.create("https://jsonplaceholder.typicode.com");
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
		return factory.createClient(JsonPlaceholderService.class);
	}
	
	@Bean
	CommandLineRunner commandLineRunner(JsonPlaceholderService jsonPlaceholderService, ObservationRegistry observationRegistry) {
		return args -> {
			Observation.createNotStarted("posts.load-all-posts", observationRegistry)
			.lowCardinalityKeyValue("author", "Anna")
			.contextualName("post-service.find-all")
			.observe(() -> {
				List<Post> posts = jsonPlaceholderService.findAll();
				log.info("Posts: {}", posts.size());
			});
		};
	}

	/*
	 * The following bean is same as the above commandLineRunner bean
	 * The only difference here is all the code for observability is replaced with @Observed annotation
	 * AOP dependency needs to be added to the project in order to make this work
	 * Unfortunately, due to some unknown reasons, this method didn't work and zipkin did not observe this call
	 */

	// @Bean
	// @Observed(name = "posts.load-all-posts", contextualName = "post.find-all")
	// CommandLineRunner commandLineRunner(JsonPlaceholderService jsonPlaceholderService) {
	// 	return args -> {
	// 		jsonPlaceholderService.findAll();
	// 	};
	// }
}
