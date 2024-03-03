create table if not exists entity_logs(
    "id" serial primary key,
    "message" varchar,
    "uuid" uuid,
    "date" timestamp,
    "level" int,
    "class" varchar
);