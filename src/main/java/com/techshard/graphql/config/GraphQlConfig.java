package com.techshard.graphql.config;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.common.io.Resources;
import com.techshard.graphql.fetcher.GraphQLDataFetcher;

import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.GraphQLServlet;
import graphql.servlet.SimpleGraphQLServlet;

@Configuration
public class GraphQlConfig {
	
	@Autowired
	private GraphQLDataFetcher graphQLDataFetcher;
	
	private GraphQL graphQL;
	
	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
        
		URL url = Resources.getResource("schema.graphqls");
		String sdl = "";
		try {
			sdl = Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		GraphQLSchema graphQLSchema = buildSchema(sdl);
		ExecutionStrategy executionStrategy = new AsyncExecutionStrategy();
		GraphQL.newGraphQL(graphQLSchema).build();
		GraphQLServlet servlet = new SimpleGraphQLServlet(graphQLSchema, executionStrategy);
		ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/graphql");
		return bean;
	}
	private GraphQLSchema buildSchema(String sdl) {
		TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(sdl);
		RuntimeWiring runtimeWiring = buildWiring();
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
	}
    
	private RuntimeWiring buildWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.type(newTypeWiring("Query").dataFetcher("websites",
						graphQLDataFetcher.getWebsiteList()))
				.type(newTypeWiring("Query").dataFetcher("website",
						graphQLDataFetcher.getWebsiteById()))
				.type(newTypeWiring("Mutation").dataFetcher("addWebsite",
						graphQLDataFetcher.addWebsite()))
				.type(newTypeWiring("Mutation").dataFetcher("updateWebsite",
						graphQLDataFetcher.updateWebsite()))
				.type(newTypeWiring("Mutation").dataFetcher("deleteWebsite",
						graphQLDataFetcher.deleteWebsite()))
				.build();

	}

	
	@SuppressWarnings("deprecation")
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("*").allowedOrigins("http://localhost:*");
			}
		};
	}
	
	@Bean
	public GraphQL graphQL() {
		return graphQL;
	}

}
