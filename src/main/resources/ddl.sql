create TABLE IF NOT EXISTS temperature_records
(
uuid VARCHAR,
createdTime  TIMESTAMP WITH TIME ZONE,
temperature Double PRECISION,
primary key(uuid, createdTime)
);