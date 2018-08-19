package com.ykb.tdp.testsuite.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

	private static final String OAUTH_SCHEME_NAME = "spring_oauth";

	@Value("${client-oauth.user}")
	private String clientId;

	@Value("${client-oauth.secret}")
	private String clientSecret;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	private AuthorizationScope[] scopes = {
			new AuthorizationScope("read", "for reading stuff"),
			new AuthorizationScope("write", "for writing stuff"),
			new AuthorizationScope("trust", "for trusting stuff") };

	/**
	 * Exposes Rest API documentation on <i>"/v2/api-docs"</i> and GUI on <i>"/swagger-ui.html"</i>
	 * 
	 * @return
	 */
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.ykb.tdp.testsuite.controller"))
				.paths(PathSelectors.any())
				.build()
				.securitySchemes(securitySchemes())
				.securityContexts(securityContext())
				.apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("TDP Test Suite")
				.description("Time Deposit Test Automation Suite")
				.termsOfServiceUrl("https://www.todo.com/api")// TODO complete when repo is moved
				.contact(new Contact("todo", "http://www.todo.com", "todo@todo.com"))// TODO complete when repo is moved
				.license("Proprietary License")
				.licenseUrl("https://www.todo.com")// TODO complete when repo is moved
				.version("1.0.0")
				.build();
	}

	private List<SecurityContext> securityContext() {
		SecurityContext restApiSecureContext = SecurityContext
				.builder()
				.securityReferences(Arrays.asList(new SecurityReference(OAUTH_SCHEME_NAME, scopes)))
				.forPaths(PathSelectors.ant("/RestApiSecure/**"))
				.build();

		return Collections.singletonList(restApiSecureContext);
	}

	private List<? extends SecurityScheme> securitySchemes() {
		SecurityScheme oauth = new OAuthBuilder()
				.name(OAUTH_SCHEME_NAME)
				.grantTypes(Arrays.asList(new ResourceOwnerPasswordCredentialsGrant(contextPath + "/oauth/token")))
				.scopes(Arrays.asList(scopes))
				.build();

		return Collections.singletonList(oauth);
	}

	@Bean
	public SecurityConfiguration security() {
		return SecurityConfigurationBuilder.builder().clientId(clientId).clientSecret(clientSecret).build();
	}
}
