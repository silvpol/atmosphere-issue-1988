package com.ibid_events;

import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy;
import org.atmosphere.interceptor.CorsInterceptor;
import org.atmosphere.interceptor.JSONPAtmosphereInterceptor;
import org.atmosphere.interceptor.SSEAtmosphereInterceptor;

/**
 * Example Dropwizard service to illustrate issue 1988
 * Created by Swav Swiac on 02/06/2015.
 */
public class ExampleService extends Service<Configuration> {

	public static final boolean enableTracing = true;

	public static void main(String[] args) throws Exception {
		new ExampleService().run(args);
	}

	@Override
	public void initialize(Bootstrap<Configuration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle());
	}

	@Override
	public void run(Configuration configuration, Environment environment) throws Exception {
		// add dummy resource so Jersey doesn't complain
		environment.addResource(new DummyResource());

		AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
		Class[] disabledInterceptors = new Class[] {
				CorsInterceptor.class, SSEAtmosphereInterceptor.class, JSONPAtmosphereInterceptor.class
		};
		StringBuilder sb = new StringBuilder(256);
		for (Class cls : disabledInterceptors)
			sb.append(cls.getName()).append(",");
		sb.setLength(sb.length() - 1);
		atmosphereServlet.framework()
				.addInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, "com.ibid_events.atmosphere")
				.addInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTORS, sb.toString())
				.addInitParameter(ApplicationConfig.BROADCASTER_LIFECYCLE_POLICY,
						BroadcasterLifeCyclePolicy.EMPTY_DESTROY.toString());
		if (enableTracing) {
			atmosphereServlet.framework()
					.addInitParameter(ResourceConfig.FEATURE_TRACE, "true")
					.addInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName())
					.addInitParameter(LoggingFilter.FEATURE_LOGGING_DISABLE_ENTITY, "false")
					.addInitParameter(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
							LoggingFilter.class.getName());
		}
		// add Atmosphere servlet for WebSocket communication
		environment.addServlet(atmosphereServlet, "/ws/*");
	}
}
