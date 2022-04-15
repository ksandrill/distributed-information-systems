create schema if not exists osm_json;
create table if not exists osm_json.user(
   uid         bigint primary key,
   username    text
);
create table if not exists osm_json.node(
   id          bigint primary key,
   version     int,
   cdate       timestamptz,
   uid         bigint,
   changeset   bigint,
   lat         double precision,
   lon         double precision,
   tags        jsonb,

   constraint fk_user
       foreign key(uid) references osm_json.user(uid)
);