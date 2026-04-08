--liquibase formatted sql

--changeset Daniil:2
--validCheckSum 9:1bd55da74fd13b4d3030cfadb8f20ae4

alter table orders
    add column order_id UUID;

update orders
set order_id = event_id
where order_id is null;

alter table orders
    alter column order_id set not null;

alter table orders
    add constraint uk_orders_order_id unique (order_id);

alter table orders
    add column kafka_offset VARCHAR(255);

update orders
set kafka_offset = concat('legacy-', id::text)
where kafka_offset is null;

alter table orders
    alter column kafka_offset set not null;

alter table orders
    add constraint uk_orders_kafka_offset unique (kafka_offset);

alter table orders
    add column status VARCHAR(50) not null default 'NEW';

alter table orders
    alter column created_at set not null;

--rollback alter table orders drop constraint if exists uk_orders_order_id;
--rollback alter table orders drop constraint if exists uk_orders_kafka_offset;
--rollback alter table orders drop column if exists order_id;
--rollback alter table orders drop column if exists kafka_offset;
--rollback alter table orders drop column if exists status;
