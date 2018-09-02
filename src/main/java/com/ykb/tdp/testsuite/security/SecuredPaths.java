package com.ykb.tdp.testsuite.security;

import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

import java.util.Arrays;
import java.util.stream.Stream;

public final class SecuredPaths {
	private static final int CONST = PUBLIC | STATIC | FINAL;

	public static final String RestApiSecure = "/RestApiSecure";

	// TODO define secured paths here

	private SecuredPaths() {
		// Hide the public constructor
	}

	public static Stream<String> all() {
		return Arrays
				.stream(SecuredPaths.class.getDeclaredFields())
				.filter(field -> field.getType() == String.class && (CONST & field.getModifiers()) == CONST)
				.map(field -> {
					try {
						return (String) field.get(null);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						return null;
					}
				});
	}

	public static String patternify(String path) {
		if (path == null)
			return null;
		return path + (path.endsWith("/") ? "**" : "/**");
	}
}
