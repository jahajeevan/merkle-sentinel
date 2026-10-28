create table datasets (id uuid primary key, name varchar(120) not null unique, version bigint not null default 0, created_at timestamp with time zone not null, updated_at timestamp with time zone not null);
create table dataset_records (id uuid primary key, dataset_id uuid not null references datasets(id) on delete cascade, position bigint not null, payload text not null, updated_at timestamp with time zone not null, unique(dataset_id, position));
create index idx_records_dataset on dataset_records(dataset_id, position);
create table checkpoints (id uuid primary key, dataset_id uuid not null references datasets(id) on delete cascade, dataset_version bigint not null, root_hash varchar(64) not null, manifest text not null, signature varchar(64) not null, created_at timestamp with time zone not null);
create index idx_checkpoints_dataset on checkpoints(dataset_id, created_at desc);
create table verification_events (id uuid primary key, dataset_id uuid not null references datasets(id) on delete cascade, dataset_version bigint not null, status varchar(32) not null, details text not null, created_at timestamp with time zone not null);
create table audit_events (id uuid primary key, dataset_id uuid references datasets(id) on delete cascade, type varchar(64) not null, details text not null, created_at timestamp with time zone not null);
