package com.ykb.tdp.testsuite.configuration;

import static com.ykb.tdp.testsuite.security.SecuredPathProvider.securedPathsFor;
import static com.ykb.tdp.testsuite.security.SecuredPathProvider.securedPathsForUnspecified;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

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

	@Value("${info.app.title}")
	private String applicationTitle;

	@Value("${info.app.description}")
	private String applicationDescription;

	@Value("${info.app.version}")
	private String version;

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
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(Predicates.or(RequestHandlerSelectors.basePackage("com.ykb.tdp.testsuite.controller"),
									RequestHandlerSelectors.basePackage("org.springframework.boot.actuate")))
				.paths(PathSelectors.any())
				.build()
				.securitySchemes(securitySchemes())
				.securityContexts(securityContext())
				.apiInfo(apiInfo())
				.forCodeGeneration(true);
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(applicationTitle)
				.description(applicationDescription)
				.termsOfServiceUrl("https://www.todo.com/api")// TODO complete when repo is moved
				.contact(new Contact("todo", "http://www.todo.com", "todo@todo.com"))// TODO complete when repo is moved
				.license("Proprietary License")
				.licenseUrl("https://www.todo.com")// TODO complete when repo is moved
				.version(version)
				.build();
	}

	private List<SecurityContext> securityContext() {
		List<SecurityContext> contexts = Stream.of(HttpMethod.values()).map(this::securityContextFor).filter(Objects::nonNull).collect(Collectors.toList());
		
		SecurityContext securityContextForUnspecified = securityContextForUnspecified();
		if (securityContextForUnspecified != null) {
			contexts.add(securityContextForUnspecified);
		}

		return contexts;
	}

	@SuppressWarnings("unchecked")
	private SecurityContext securityContextForUnspecified() {
		String[] securedPaths = securedPathsForUnspecified();
		if (securedPaths == null || securedPaths.length == 0) {
			return null;
		}
		return SecurityContext.builder()
				.securityReferences(Arrays.asList(new SecurityReference(OAUTH_SCHEME_NAME, scopes)))
				.forPaths(Predicates.or(Stream.of(securedPaths).map(path -> PathSelectors.ant(path)).toArray(Predicate[]::new)))
				.build();
	}

	@SuppressWarnings("unchecked")
	private SecurityContext securityContextFor(HttpMethod method) {
		String[] securedPaths = securedPathsFor(method);
		if (securedPaths == null || securedPaths.length == 0) {
			return null;
		}
		return SecurityContext.builder()
				.securityReferences(Arrays.asList(new SecurityReference(OAUTH_SCHEME_NAME, scopes)))
				.forPaths(Predicates.or(Stream.of(securedPaths).map(path -> PathSelectors.ant(path)).toArray(Predicate[]::new)))
				.forHttpMethods(Predicates.equalTo(method))
				.build();
	}

	private List<? extends SecurityScheme> securitySchemes() {
		SecurityScheme oauth = new OAuthBuilder().name(OAUTH_SCHEME_NAME)
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
