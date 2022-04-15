create schema if not exists osm_custom_types;
create type tag as (
   k           text,
   v           text
);
create table if not exists osm_custom_types.user(
    uid         bigint primary key,
    username    text
);
create table if not exists osm_custom_types.node(
    id          bigint primary key,
    version     int,
    cdate       timestamptz,
    uid         bigint,
    changeset   bigint,
    lat         double precision,
    lon         double precision,
    tags        tag[],

    constraint fk_user
        foreign key(uid) references osm_custom_types.user(uid)
);
