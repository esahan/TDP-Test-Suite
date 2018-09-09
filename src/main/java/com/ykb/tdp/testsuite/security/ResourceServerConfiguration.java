package com.ykb.tdp.testsuite.security;

import static com.ykb.tdp.testsuite.security.SecuredPathProvider.securedPathsFor;
import static com.ykb.tdp.testsuite.security.SecuredPathProvider.securedPathsForUnspecified;

import java.util.stream.Stream;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

	private static final String RESOURCE_ID = "tdp_resource_id";

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		resources.resourceId(RESOURCE_ID).stateless(false);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizationConfigurer = http.anonymous()
				.disable()
				.exceptionHandling()
				.accessDeniedHandler(new OAuth2AccessDeniedHandler())
				.and()
				.authorizeRequests();

		Stream.of(HttpMethod.values()).forEach(method -> authenticateForMethod(authorizationConfigurer, method));
		authenticateForUnspecified(authorizationConfigurer);
	}

	private void authenticateForUnspecified(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizationConfigurer) {
		String[] securedPaths = securedPathsForUnspecified();
		if (securedPaths != null && securedPaths.length > 0) {
			authorizationConfigurer.antMatchers(securedPaths).authenticated();
		}
	}

	private void authenticateForMethod(	ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizationConfigurer,
										HttpMethod method) {
		String[] securedPaths = securedPathsFor(method);
		if (securedPaths != null && securedPaths.length > 0) {
			authorizationConfigurer.antMatchers(method, securedPaths).authenticated();
		}
	}

}
