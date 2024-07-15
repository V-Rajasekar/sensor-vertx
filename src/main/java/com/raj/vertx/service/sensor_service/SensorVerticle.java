package com.raj.vertx.service.sensor_service;

import java.util.Random;
import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorVerticle extends AbstractVerticle {

  Logger LOG = LoggerFactory.getLogger(SensorVerticle.class);
  private double temperature = 20;
  private final Random random = new Random();

    @Override
  public void start(Promise<Void> startPromise) throws Exception {
        LOG.info("Sensor Verticle started");
      String uniqueId = UUID.randomUUID().toString();
      vertx.setPeriodic(2000, this::updateTemperature);
      startPromise.complete();
  }

  private void updateTemperature(Long aLong) {
      temperature = temperature + (random.nextGaussian()/ 2.0);
      LOG.info("New temperature:{}", temperature);
  }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
       // vertx.deployVerticle(new SensorVerticle());
        vertx.deployVerticle("SensorVehicle", new DeploymentOptions().setInstances(4));
    }
}
