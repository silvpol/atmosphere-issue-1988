package com.ibid_events.atmosphere;

import com.sun.jersey.spi.resource.Singleton;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.NginxInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

/**
 * Example of issue 1988
 * Created by Swav Swiac on 02/06/2015.
 */
@Singleton
@AtmosphereHandlerService(path = "/ws/*",
		interceptors = {
				TrackMessageSizeInterceptor.class, NginxInterceptor.class
		})
public class ExampleAtmosphereHandler implements AtmosphereHandler, AtmosphereServletProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ExampleAtmosphereHandler.class);
	private final BroadcasterCache broadcasterCache;
	private BroadcasterFactory broadcasterFactory;

	public static final String UUID_STRING = "11111111-a4b4-11e3-a769-00231832fa86";
	public static final UUID UUID_OBJECT = UUID.fromString("22222222-a4b4-11e3-a769-00231832fa86");

	public ExampleAtmosphereHandler() {
		this.broadcasterCache = new UUIDBroadcasterCache();
	}

	@Override
	public void init(AtmosphereConfig config) throws ServletException {
		this.broadcasterFactory = config.getBroadcasterFactory();
		broadcasterCache.configure(config);

	}

	@Override
	public void onRequest(AtmosphereResource resource) throws IOException {
		Broadcaster broadcaster;
		AtmosphereRequest request = resource.getRequest();
		if (request.getMethod().equalsIgnoreCase("GET")) {
			// if using streaming transport disable compression
			if (resource.transport() == AtmosphereResource.TRANSPORT.STREAMING)
				resource.getResponse().setHeader(HttpHeaders.CONTENT_ENCODING, "identity");
			resource.suspend();
			if ("/pass".equals(request.getPathInfo())) {
				logger.debug("Client connects to /ws/pass");
				broadcaster = broadcasterFactory.lookup(UUID_STRING);
				if (broadcaster == null) {
					broadcaster = broadcasterFactory.get(UUID_STRING);
					broadcaster.setBroadcasterLifeCyclePolicy(BroadcasterLifeCyclePolicy.EMPTY_DESTROY);
					broadcaster.getBroadcasterConfig().setBroadcasterCache(broadcasterCache);
				}
				broadcaster.addAtmosphereResource(resource);
			} else if ("/fail".equals((request.getPathInfo()))) {
				logger.debug("Client connects to /ws/fail");
				broadcaster = broadcasterFactory.lookup(UUID_OBJECT);
				if (broadcaster == null) {
					broadcaster = broadcasterFactory.get(UUID_OBJECT);
					broadcaster.setBroadcasterLifeCyclePolicy(BroadcasterLifeCyclePolicy.EMPTY_DESTROY);
					broadcaster.getBroadcasterConfig().setBroadcasterCache(broadcasterCache);
				}
				broadcaster.addAtmosphereResource(resource);
			}
		} else if (request.getMethod().equalsIgnoreCase("POST")) {
			logger.info("POST received");
			broadcaster = broadcasterFactory.lookup(UUID_STRING);
			if (broadcaster != null)
				broadcaster.broadcast("pass");
			broadcaster = broadcasterFactory.lookup(UUID_OBJECT);
			if (broadcaster != null)
				broadcaster.broadcast("fail");
		}
	}

	@Override
	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
		Object message = event.getMessage();
		if (event.isSuspended() && message != null) {
			PrintWriter writer = event.getResource().getResponse().getWriter();
			if (message instanceof String) {
				writer.write((String) message);
				writer.flush();
			} else if (message instanceof List) {
				for (Object msg : (List) message)
					if (msg instanceof String)
						writer.write((String) msg);
				writer.flush();
			} else
				logger.debug("Unknown message type {}", event.getMessage().getClass());
		} else if (event.isCancelled() || event.isClosedByClient()) {
			logger.debug("Client {} disconnects", event.getResource().uuid());
		}
		if (event.isClosedByApplication())
			logger.debug("Resource {} isClosedByApplication", event.getResource());

	}

	@Override
	public void destroy() {

	}
}
