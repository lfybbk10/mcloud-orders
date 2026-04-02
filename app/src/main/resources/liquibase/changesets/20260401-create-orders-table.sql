--liquibase formatted sql

--changeset Daniil:1

create table orders(
    id UUID PRIMARY KEY,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE
);

--rollback DROP TABLE orders;