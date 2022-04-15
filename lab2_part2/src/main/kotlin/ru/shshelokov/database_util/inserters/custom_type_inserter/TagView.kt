package ru.shshelokov.database_util.inserters.custom_type_inserter

import org.openstreetmap.osm.Tag
import ru.shshelokov.database_util.DataBaseUtil

class TagView(private val tag: Tag) {
    override fun toString(): String {
        return String.format("('%s','%s')", DataBaseUtil.escape(tag.k), DataBaseUtil.escape(tag.v))
    }

}