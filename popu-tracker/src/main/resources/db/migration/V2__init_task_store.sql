create table if not exists task_store(
entity_id uuid primary key,
entity_version integer default 1,
created_at timestamp,
title varchar,
assigned_on uuid not null,
status boolean default false
);