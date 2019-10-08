package com.techshard.graphql.fetcher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.techshard.graphql.reponse.Website;

import graphql.schema.DataFetcher;

@Component
public class GraphQLDataFetcher {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphQLDataFetcher.class);

	@Autowired
	Environment env;

	@Bean
	private RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public DataFetcher<List<Website>> getWebsiteList() {
		logger.info("Entering GraphQLDataFetcher :: getWebsiteList() method");
		return dataFetchingEnv -> {
			return new RestTemplate().exchange(env.getProperty("rest.url") + "/getWebSiteList", HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Website>>() {

					}).getBody();
		};
	}

	public DataFetcher<Website> getWebsiteById() {
		logger.info("Entering GraphQLDataFetcher :: getWebsiteById() method");
		return dataFetchingEnv -> {
			String id = dataFetchingEnv.getArgument("id");
			return new RestTemplate().getForObject(env.getProperty("rest.url") + "/getWebSiteBy"+ "/" + id, Website.class);
		};
	}

	public DataFetcher<String> addWebsite() {
		logger.info("Entering GraphQLDataFetcher :: addWebsite() method");
		return dataFetchingEnv -> {
			String name = dataFetchingEnv.getArgument("name");
			logger.info("name :" + name);
			String url = dataFetchingEnv.getArgument("url");
			logger.info("url :" + url);
			Website website = new Website();
			website.setName(name);
			website.setUrl(url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Website> entity = new HttpEntity<>(website, headers);
			return new RestTemplate().postForObject(env.getProperty("rest.url") + "/saveWeb", entity, String.class);

		};
	}

	public DataFetcher<String> updateWebsite() {
		logger.info("Entering GraphQLDataFetcher :: updateWebsite() method");
		return dataFetchingEnv -> {
			String id = dataFetchingEnv.getArgument("id");
			logger.info("id :" + id);
			String name = dataFetchingEnv.getArgument("name");
			logger.info("name :" + name);
			String url = dataFetchingEnv.getArgument("url");
			logger.info("url :" + url);
			Website website = new Website(Integer.parseInt(id), name, url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Website> entity = new HttpEntity<>(website, headers);
			return new RestTemplate().exchange(env.getProperty("rest.url") + "/updateWebsite", HttpMethod.PUT, entity,
					new ParameterizedTypeReference<String>() {
					}).getBody();

		};

	}

	public DataFetcher<String> deleteWebsite() {
		return dataFetchingEnv -> {
			String id = dataFetchingEnv.getArgument("id");
			Website website = new Website();
			website.setId(Integer.parseInt(id));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Website> entity = new HttpEntity<>(website, headers);
			return new RestTemplate().exchange(env.getProperty("rest.url") + "/deleteWebsite", HttpMethod.DELETE, entity,
					new ParameterizedTypeReference<String>() {
					}).getBody();

		};

	}

}
