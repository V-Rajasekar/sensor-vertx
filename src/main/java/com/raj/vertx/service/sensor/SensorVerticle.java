package com.raj.vertx.service.sensor;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorVerticle extends AbstractVerticle {

    Logger LOG = LoggerFactory.getLogger(SensorVerticle.class);
    private double temperature = 20;
    private final Random random = new Random();

    String uniqueId = UUID.randomUUID().toString();
    private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT",
            "8080"));

    private SqlClient sqlClient;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOG.info("Sensor Verticle started");

        vertx.setPeriodic(30000, this::updateTemperature);

        sqlClient = createSqlClient(vertx);

        vertx.eventBus().<JsonObject>consumer("temperature.updates", this::recordTemperature);

         //Creating HTTP Request handler using Router
        Router router = Router.router(vertx);
        router.get("/temperatures/uuid/:uuid").handler(this::getData);
        router.get("/temperatures").handler(this::getAllData);


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

    private void recordTemperature(Message<JsonObject> msg) {
        JsonObject body = msg.body();
        OffsetDateTime createdTime = OffsetDateTime.parse(body.getString("createdtime"));
        Tuple values = Tuple.of(body.getString("uuid"),
                createdTime,
                body.getDouble("temperature"));

        sqlClient.preparedQuery("INSERT INTO temperature_records VALUES($1, $2::timestamptz, $3)")
                .execute(values)
                 .onComplete(ar -> {
                     if (ar.succeeded()) {
                         RowSet<Row> rows = ar.result();
                         System.out.println(rows.rowCount());
                     } else {
                         System.out.println("Failure: " + ar.cause().getMessage());
                     }});
    }

    //Using SqlTemplate need some more dependencies
    private void recordTemperature() {
         /* SqlTemplate.forUpdate(sqlClient, "INSERT INTO temperature_records VALUES(#{uuid}, #{createdtime}, #{temperature})")
                   .mapFrom(TupleMapper.jsonObject())
                   .execute(body)
                   .onSuccess(ar -> {
                               System.out.println("Temperature inserted/"+ ar.rowCount());
                           }
                   ).onFailure(fail -> {
                      fail.printStackTrace();
                      // System.out.println("Failure: " + fail.getCause().getMessage());
                   });*/
    }

    private void getAllData(RoutingContext ctx) {

        LOG.info("Requesting all data from :{}", ctx.request().remoteAddress());
        String query = "SELECT * FROM temperature_records";
        sqlClient.preparedQuery(query)
                .execute()
                .onSuccess(rows -> {
                    JsonArray jsonArray = new JsonArray();
                    for (Row row: rows) {
                       jsonArray.add(new JsonObject()
                               .put("uuid", row.getString("uuid"))
                               .put("temperature", row.getDouble("temperature"))
                               .put("createdtime", row.getTemporal("createdtime").toString())
                       );
                    }
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("data", jsonArray).encode());
                })
                .onFailure(fail -> {
                    ctx.fail(500);

                });
    }

    private void getData(RoutingContext context) {
        LOG.info("Process http request for {}", context.request().remoteAddress().toString());
        String uuid = context.request().getParam("uuid");
        //Added to support Java 8 date/time type `java.time.OffsetDateTime
        DatabindCodec.mapper().registerModule(new JavaTimeModule());
        SqlTemplate.forQuery(sqlClient, "select * from temperature_records where uuid=#{uuid}")
                   /*.mapTo(row -> {
                       Temperature temp = new Temperature();
                       temp.setUuid(row.getString("uuid"));
                       temp.setCreatedtime(row.getOffsetDateTime("createdtime"));
                       temp.setTemperature(row.getDouble("temperature"));
                       return temp;
                   })*/

                   .execute(Collections.singletonMap("uuid", uuid))
                   .onSuccess(tempRowSet -> {
                               LOG.info("Latest temperature returned for UUID:{}", uuid);
                            JsonArray data = new JsonArray();
                               for (Row row : tempRowSet) {
                                   JsonObject jsonObject = new JsonObject();
                                   jsonObject.put("uuid", row.getString("uuid"))
                                             .put("temperature", row.getOffsetDateTime("createdtime"))
                                             .put("createdtime", row.getDouble("temperature"));
                                             data.add(jsonObject);
                               }
                               context.response().setStatusCode(200)
                                      .putHeader("Content-Type", "application/json")

                                      .end(new JsonObject().put("uuid", uuid)
                                              .put("data", data).encode());
                           }
                   )
                   .onFailure(err -> {
                       LOG.error("Failed to retrieve latest temperature update for UUID {}/ {}", uuid, err);
                       throw new RuntimeException("Unable to find the temperature for UUID:" + uuid);
                   });
    }

    private JsonObject createTemperatureObj() {
        JsonObject payload = new JsonObject();
        payload.put("uuid", uniqueId);
        payload.put("temperature", temperature);
        payload.put("createdtime", OffsetDateTime.now().toString());
        return payload;
    }

    private void updateTemperature(Long aLong) {
        temperature = temperature + (random.nextGaussian() / 2.0);
        LOG.info("New temperature:{}", temperature);

        vertx.eventBus().publish("temperature.updates", createTemperatureObj());
    }



    private static SqlClient createSqlClient (Vertx vertx) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setDatabase("postgres")
                .setUser("postgres")
                .setPassword("postgres");

// Pool options
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

// Create the pooled client
        return PgBuilder
               // .pool()
                .client()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
    }
}
