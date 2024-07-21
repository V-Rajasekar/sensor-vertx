package com.raj.vertx.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

   static Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //vertx.deployVerticle(new SensorVerticle());
        vertx.deployVerticle("com.raj.vertx.service.sensor.SensorVerticle", new DeploymentOptions().setInstances(2));

        vertx.eventBus().<JsonObject>consumer("temperature.updates", msg -> {
            log.info("msg>>>>{}", msg.body().encodePrettily());
        });
    }
}
