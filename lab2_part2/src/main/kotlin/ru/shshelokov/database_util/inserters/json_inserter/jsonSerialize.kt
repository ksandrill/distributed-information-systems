package ru.shshelokov.database_util.inserters.json_inserter

import org.json.JSONArray
import org.json.JSONObject
import org.openstreetmap.osm.Tag


fun serialize(tags: Iterable<Tag>): JSONArray {
    return JSONArray()
        .putAll(
           tags.map{serialize(it)}
        )
}

fun serialize(tag: Tag): JSONObject {
    return JSONObject()
        .put("k", tag.k)
        .put("v", tag.v)
}