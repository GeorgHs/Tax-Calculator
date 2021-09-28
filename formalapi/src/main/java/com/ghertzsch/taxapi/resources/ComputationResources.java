package com.ghertzsch.taxapi.resources;

import java.util.ArrayList;
import java.util.List;

import com.ghertzsch.taxapi.entity.Computation;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ComputationResources {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComputationResources.class);

	
	public Router getAPISubRouter(Vertx vertx) {
		
		Router apiSubRouter = Router.router(vertx);
		
		// API routing
		apiSubRouter.route("/*").handler(this::defaultProcessorForAllAPI);
		
		apiSubRouter.route("/v1/calculator*").handler(BodyHandler.create());
		apiSubRouter.get("/v1/calculator").handler(this::getAllComputations);
		apiSubRouter.get("/v1/calculator/:id").handler(this::getComputationById);
		apiSubRouter.post("/v1/calculator").handler(this::addComputation);
		apiSubRouter.put("/v1/calculator/:id").handler(this::updateComputationById);
		apiSubRouter.delete("/v1/calculator/:id").handler(this::deleteComputationById);
		
		return apiSubRouter;
	}
	
	
	public void defaultProcessorForAllAPI(RoutingContext routingContext) {
		
		String authToken = routingContext.request().getHeader("AuthToken");
				
		if (authToken == null || !authToken.equals("123")) {
			LOGGER.info("Failed basic auth check");

			routingContext.response().setStatusCode(401).putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encodePrettily(new JsonObject().put("error", "Not Authorized to use these API's")));
		}
		else {
			LOGGER.info("Passed basic auth check");
			
			// Allowing CORS - Cross Domain API calls
			routingContext.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			routingContext.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE");
			
			// Call next matching route
			routingContext.next();			
		}
		
	}
	

	public void getAllComputations(RoutingContext routingContext) {
		
		JsonObject responseJson = new JsonObject();
	
		Computation firstItem = new Computation("112233", "123", "My item 123");
		Computation secondItem = new Computation("11334455", "321", "My item 321");
		
		List<Computation> products = new ArrayList<Computation>();
		
		products.add(firstItem);
		products.add(secondItem);
		
		responseJson.put("products", products);
		
		routingContext.response()
			.setStatusCode(200)
			.putHeader("content-type", "application/json")
			.end(Json.encodePrettily(responseJson));

	}
	

	public void getComputationById(RoutingContext routingContext) {
		
		final String productId = routingContext.request().getParam("id");
		
		String number = "123";
		
		Computation firstItem = new Computation(productId, number, "My item " + number);
		
		routingContext.response()
		.setStatusCode(200)
		.putHeader("content-type", "application/json")
		.end(Json.encodePrettily(firstItem));


		
	}
	
	public void addComputation(RoutingContext routingContext) {
		
		JsonObject jsonBody = routingContext.getBodyAsJson();
		
		System.out.println(jsonBody);
		
		String number = jsonBody.getString("number");
		String description = jsonBody.getString("description");
		
		Computation newItem = new Computation("", number, description);
		
		// Add into database and get unique id
		newItem.setId("556677");
		
		routingContext.response()
		.setStatusCode(201)
		.putHeader("content-type", "application/json")
		.end(Json.encodePrettily(newItem));

		
	}
	
	public void updateComputationById(RoutingContext routingContext) {
		
		final String productId = routingContext.request().getParam("id");
		
		JsonObject jsonBody = routingContext.getBodyAsJson();


		String number = jsonBody.getString("number");
		String description = jsonBody.getString("description");
		
		Computation updatedItem = new Computation(productId, number, description);

		routingContext.response()
		.setStatusCode(200)
		.putHeader("content-type", "application/json")
		.end(Json.encodePrettily(updatedItem));

		
	}
	
	public void deleteComputationById(RoutingContext routingContext) {
		
		final String productId = routingContext.request().getParam("id");

		routingContext.response()
		.setStatusCode(200)
		.putHeader("content-type", "application/json")
		.end();

		
	}

}
