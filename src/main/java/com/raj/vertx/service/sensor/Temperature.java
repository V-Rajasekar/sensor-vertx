package com.raj.vertx.service.sensor;

import java.time.OffsetDateTime;

public class Temperature {

    private String uuid;

    private OffsetDateTime createdtime;
    private Double temperature;

    public String getUuid() {
        return uuid;
    }

    public OffsetDateTime getCreatedtime() {
        return createdtime;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCreatedtime(OffsetDateTime createdtime) {
        this.createdtime = createdtime;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "uuid='" + uuid + '\'' +
                ", createdtime=" + createdtime +
                ", temperature=" + temperature +
                '}';
    }
}
