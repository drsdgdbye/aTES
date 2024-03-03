create table if not exists account_store(
entity_id uuid primary key,
entity_version integer default 1,
full_name varchar,
role varchar not null
);