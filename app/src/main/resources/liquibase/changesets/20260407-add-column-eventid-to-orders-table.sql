alter table orders
add column event_id UUID UNIQUE NOT NULL;