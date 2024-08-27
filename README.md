# sensor-vertx
A sample eclipse vertx application to demonstrate how to use vertx to build an application to store the temperature in Postgres Database. Create a RESTful API to 
retrieve all the temperatures, temperature by UUID.
- Every 30 secs the temperature are generated and published in the eventbus
- The temperature records are consumed and saved to the postgres table `temperature_records`
- Restful API's are written to expose these stored value 


## PreRequests.
- Postgres database with a table to store the temperature. Please note you can run Docker postgres image to setup this Postgres DB
```sql
-- DROP TABLE public.temperature_records;

    CREATE TABLE public.temperature_records (
        "uuid" varchar NOT NULL,
        createdtime timestamptz NOT NULL,
        temperature float8 NULL,
        CONSTRAINT temperature_records_pkey PRIMARY KEY (uuid, createdtime)
    );
```

## Running the application

- Build application using `gradle clean build`
- Use the Run config file `.run/com.raj.vertx.service.Main.run.xml` to configure the run step and run the application

## Test

- Every 30 secs messages are published like below 

```json
    {
      "uuid" : "43a0ca8c-cfc2-4c58-bf00-7752c3de16cd",
      "temperature" : 19.57477221733607,
      "createdtime" : "2024-08-27T11:03:20.189090800+05:30"
    }
```
- REST API's endpoints
  - `[`Show All Temperatures`]`http://127.0.0.1:8080/temperatures/
  - `[`Show Temperature by uuid`]`http://127.0.0.1:8080/temperatures/uuid/1d2cae5c-a7e8-46e2-b01d-60129f30dd26