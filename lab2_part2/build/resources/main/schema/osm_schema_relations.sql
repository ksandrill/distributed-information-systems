create schema if not exists osm;
create table if not exists osm.user(
    uid         bigint primary key,
    username    text
);
create table if not exists osm.node(
    id          bigint primary key,
    version     int,
    cdate       timestamptz,
    uid         bigint,
    changeset   bigint,
    lat         double precision,
    lon         double precision,

    constraint fk_user
        foreign key(uid) references osm.user(uid)
);
create table if not exists osm.tag(
    node_id     bigint,
    k           text,
    v           text,

    constraint fk_node
        foreign key(node_id) references osm.node(id)
);