package com.ghertzsch.taxapi;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.ghertzsch.taxapi.entity.Computation;
import com.ghertzsch.taxapi.resources.ComputationResources;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Hello world!
 *
 */
public class FormalAPIVerticle extends AbstractVerticle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FormalAPIVerticle.class);
	
    public static void main( String[] args ) {
    	
//    	DeploymentOptions options = new DeploymentOptions();
//    	
//    	options.setConfig(new JsonObject().put("http.port", 8080));
    			
    	Vertx vertx = Vertx.vertx();
    	
        // Use config/config.json from resources/classpath
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
 
        configRetriever.getConfig(config -> {
        	
       	if (config.succeeded()) {
        		
        		JsonObject configJson = config.result();
        		
        		System.out.println(configJson.encodePrettily());
         		
        		DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
        		
            	vertx.deployVerticle(new FormalAPIVerticle(), options);

       		}
       	
       	
        });
        
    	
    }
    
	@Override
	public void start() {
		LOGGER.info("Verticle FormalAPIVerticle Started");
		
		Router router = Router.router(vertx);
		
		router.route().handler(CookieHandler.create());
		
		// Create ProductResource object
		ComputationResources productResources = new ComputationResources();
		
		// Map subrouter for Products
		router.mountSubRouter("/api/", productResources.getAPISubRouter(vertx));
		
		
		router.get("/yo.html").handler(routingContext -> {
			
			Cookie nameCookie = routingContext.getCookie("name");
			
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("webroot/yo.html").getFile());

			String mappedHTML = "";

			try {
				StringBuilder result = new StringBuilder("");

				Scanner scanner = new Scanner(file);

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}

				scanner.close();

				mappedHTML = result.toString();
				
				
				String name = "Unknown";
						
				if (nameCookie != null) {
					name = nameCookie.getValue();
				}
				else {
					nameCookie = Cookie.cookie("name", "Tom-Jay");
					nameCookie.setPath("/");
					nameCookie.setMaxAge(365 * 24 * 60 * 60); // 1 year in seconds
					
					routingContext.addCookie(nameCookie);
				}
				
				mappedHTML = replaceAllTokens(mappedHTML, "{name}", name);

			} catch (IOException e) {
				e.printStackTrace();
			}

			routingContext.response().putHeader("content-type", "text/html").end(mappedHTML);
			
		});
		
		// Default if no routes are matched
		router.route().handler(StaticHandler.create().setCachingEnabled(false));

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port"), asyncResult -> {
			
			if (asyncResult.succeeded()) { 
				LOGGER.info("HTTP server running on port " + config().getInteger("http.port"));
			}
			else {
				LOGGER.error("Could not start a HTTP server", asyncResult.cause());
			}
			
			
		});

		

	}

	
	public String replaceAllTokens(String input, String token, String newValue) {

		String output = input;

		while (output.indexOf(token) != -1) {

			output = output.replace(token, newValue);

		}

		return output;

	}

    
	@Override
	public void stop() {
		LOGGER.info("Verticle FormalAPIVerticle Stopped");
	}

	
}
