package com.raj.vertx.service.sensor;

import java.util.Random;
import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorVerticle extends AbstractVerticle {

    Logger LOG = LoggerFactory.getLogger(SensorVerticle.class);
    private double temperature = 20;
    private final Random random = new Random();

    String uniqueId = UUID.randomUUID().toString();
    private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT",
            "8080"));

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOG.info("Sensor Verticle started");

        vertx.setPeriodic(2000, this::updateTemperature);


        //Creating HTTP Request handler using Router
        Router router = Router.router(vertx);
        router.get("/data") //HTTP GET call
              .handler(this::getData);

        //Creating an HTTP server whos request are handled by router
        vertx.createHttpServer()
             .requestHandler(router)
             .listen(HTTP_PORT)
             .onSuccess(ok -> {
                 LOG.info("HTTP server running:  http://127.0.0.1:{}", HTTP_PORT);
                 startPromise.complete();
             })
             .onFailure(startPromise::fail); //propagate the failure to caller

    }

    private void getData(RoutingContext context) {
        LOG.info("Process http request for {}", context.request().remoteAddress().toString());
        JsonObject payload = new JsonObject();
        payload.put("uuid", uniqueId);
        payload.put("temperature", temperature);
        payload.put("timeStamp", System.currentTimeMillis());

        context.response()
               .putHeader("Content-Type", "application/json")
               .setStatusCode(200)
               .end(payload.encode());
    }

    private void updateTemperature(Long aLong) {
        temperature = temperature + (random.nextGaussian() / 2.0);
        LOG.info("New temperature:{}", temperature);
    }
}
