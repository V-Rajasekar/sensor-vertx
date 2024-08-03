package com.raj.vertx.service;

import com.raj.vertx.service.sensor.SensorVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
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

      /*  Vertx.clusteredVertx(new VertxOptions())
                .onSuccess(ok -> {
                    vertx.deployVerticle(new SensorVerticle());
                })
                .onFailure(fail -> {
                    log.error("woop, {}", fail);
                });*/
    }


}
