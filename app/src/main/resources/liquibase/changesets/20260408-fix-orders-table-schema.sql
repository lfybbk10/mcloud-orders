--liquibase formatted sql

--changeset Daniil:3

alter table orders
    add column if not exists order_id UUID;

update orders
set order_id = event_id
where order_id is null;

alter table orders
    alter column order_id set not null;

create unique index if not exists uk_orders_order_id
    on orders(order_id);

alter table orders
    add column if not exists kafka_offset VARCHAR(255);

update orders
set kafka_offset = concat('legacy-', id::text)
where kafka_offset is null;

alter table orders
    alter column kafka_offset set not null;

create unique index if not exists uk_orders_kafka_offset
    on orders(kafka_offset);

alter table orders
    add column if not exists status VARCHAR(50);

update orders
set status = 'NEW'
where status is null;

alter table orders
    alter column status set default 'NEW';

alter table orders
    alter column status set not null;

update orders
set created_at = now()
where created_at is null;

alter table orders
    alter column created_at set not null;

--rollback alter table orders drop constraint if exists uk_orders_order_id;
--rollback alter table orders drop constraint if exists uk_orders_kafka_offset;
--rollback alter table orders drop column if exists order_id;
--rollback alter table orders drop column if exists kafka_offset;
--rollback alter table orders drop column if exists status;
