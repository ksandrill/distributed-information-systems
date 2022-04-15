package ru.shshelokov.database_util.inserters.custom_type_inserter

import org.openstreetmap.osm.Tag

fun serialize(tags: Iterable<Tag>): String {
    val tagsArrayString = tags.joinToString(",") { obj: Tag -> TagView(obj).toString() }
    return if (tagsArrayString.isEmpty()) {
        "ARRAY[]::tag[]"
    } else "ARRAY[$tagsArrayString]::tag[]"
}