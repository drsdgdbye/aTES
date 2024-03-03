create table if not exists offset_data(
    "id" serial primary key,
    "topic" varchar not null,
    "partition" int not null,
    "offset" int8 not null
);