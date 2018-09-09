package com.ykb.tdp.testsuite.security;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public final class SecuredPathProvider {

	private static SecuredPathHolder holder;

	private SecuredPathProvider() {

		// Hide the public constructor
	}

	private static String patternify(String path) {
		if (path == null)
			return null;
		return path + (path.endsWith("/") ? "**" : "/**");
	}

	private static SecuredPathHolder readPaths(Class<?> beanClass) {
		SecuredPathHolder result;

		AnnotationAttributes classLevelRequestmappingAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(beanClass, RequestMapping.class);

		final String[] basePaths = classLevelRequestmappingAttributes != null ? classLevelRequestmappingAttributes.getStringArray("path") : null;
		final RequestMethod[] baseMethods = classLevelRequestmappingAttributes != null
				? (RequestMethod[]) classLevelRequestmappingAttributes.get("method")
				: null;

		boolean securedClass = beanClass.isAnnotationPresent(Secured.class);

		// If class is annotated with Secured and RequestMapping everything under that path will be secured
		if (securedClass && basePaths != null && basePaths.length > 0) {
			result = new SecuredPathHolder();
			result.addSingle(null, Arrays.stream(basePaths).map(path -> patternify(path)).collect(toSet()));
		} else {
			result = Arrays.stream(beanClass.getMethods())
					.filter(method -> securedClass
							|| method.isAnnotationPresent(Secured.class) && AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class))
					.map(method -> AnnotatedElementUtils.getMergedAnnotationAttributes(method, RequestMapping.class))
					.reduce(new SecuredPathHolder(), (hld, attr) -> hld.add(attr, basePaths, baseMethods), SecuredPathHolder::merge);
		}

		return result;
	}

	private static SecuredPathHolder init() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
		Set<BeanDefinition> candidateComponents = provider.findCandidateComponents("com.ykb.tdp.testsuite");

		return candidateComponents.stream()
				// map bean definitions to classes
				.map(beanDef -> {
					try {
						return Class.forName(beanDef.getBeanClassName());
					} catch (ClassNotFoundException e2) {
						return SecuredPathProvider.class;
					}
				})
				// then filter out non-secure ones
				.filter(beanClass -> beanClass.isAnnotationPresent(Secured.class)
						|| Arrays.stream(beanClass.getMethods()).anyMatch(method -> method.isAnnotationPresent(Secured.class)))
				// then read paths in the class and its secured methods
				.map(beanClass -> readPaths(beanClass))
				// finally merge the resulting path holders into a single one
				.reduce(SecuredPathHolder::merge)
				// if no secured paths exists, return an empty path holder
				.orElse(new SecuredPathHolder());
	}

	public static synchronized String[] securedPathsForUnspecified() {
		return holder.get(null);
	}

	public static synchronized String[] securedPathsFor(HttpMethod method) {
		if (holder == null) {
			holder = init();
		}

		return holder.get(method);
	}

	private static class SecuredPathHolder {
		private Map<String, Set<String>> innerMap;

		public SecuredPathHolder() {
			innerMap = new LinkedHashMap<>();
		}

		public String[] get(HttpMethod method) {
			Set<String> result = Optional.ofNullable(innerMap.get("")).orElseGet(HashSet::new);
			if (method != null) {
				result.addAll(Optional.ofNullable(innerMap.get(method.name())).orElse(Collections.emptySet()));
			}

			return result.stream()
					.sorted(Comparator.comparingLong(path -> ((String) path).chars().filter(c -> '/' == c).count())
							.reversed()
							.thenComparingLong(path -> ((String) path).chars().filter(c -> '*' == c).count())
							.reversed())
					.toArray(String[]::new);
		}

		public SecuredPathHolder add(AnnotationAttributes attr, String[] basePaths, RequestMethod[] basemethods) {
			if (attr == null) {
				return this;
			}
			RequestMethod[] methods = Stream.of(Optional.ofNullable(basemethods), Optional.ofNullable((RequestMethod[]) attr.get("method")))
					.flatMap(opt -> Stream.of(opt.orElse(new RequestMethod[] {})))
					.toArray(RequestMethod[]::new);

			if (methods.length == 0) {
				methods = new RequestMethod[] { null };
			}

			Stream<String> paths = Stream.of(attr.getStringArray("path"));
			Set<String> pathsToAdd = basePaths == null || basePaths.length == 0
					? paths.collect(toSet())
					: paths.collect(LinkedHashSet::new,
									(set, subPath) -> set.addAll(Stream.of(basePaths).map(basePath -> concatPath(basePath, subPath)).collect(toSet())),
									(s1, s2) -> s1.addAll(s2));

			Stream.of(methods).map(m -> HttpMethod.resolve(m == null ? null : m.name())).forEach(hm -> addSingle(hm, pathsToAdd));

			return this;
		}

		public SecuredPathHolder addSingle(HttpMethod method, Set<String> paths) {
			String key = method == null ? "" : method.name();
			if (innerMap.containsKey(key)) {
				innerMap.get(key).addAll(paths);
			} else {
				innerMap.put(key, paths);
			}

			return this;
		}

		public SecuredPathHolder merge(SecuredPathHolder h2) {
			if (h2 != null && h2.innerMap != null) {
				h2.innerMap.forEach((k, v) -> this.addSingle(StringUtils.isEmpty(k) ? null : HttpMethod.resolve(k), v));
			}
			return this;
		}

		private static String concatPath(String basePath, String subPath) {
			return (basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath)
					+ (subPath.startsWith("/") ? subPath : "/".concat(subPath));
		}
	}

}
