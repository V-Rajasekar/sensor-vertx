package com.raj.vertx.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //vertx.deployVerticle(new SensorVerticle());
        vertx.deployVerticle("com.raj.vertx.service.sensor.SensorVerticle", new DeploymentOptions().setInstances(2));
    }
}
