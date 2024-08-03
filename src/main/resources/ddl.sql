create TABLE IF NOT EXISTS temperature_records
(
uuid VARCHAR,
createdtime  TIMESTAMP WITH TIME ZONE,
temperature Double PRECISION,
primary key(uuid, createdtime)
);