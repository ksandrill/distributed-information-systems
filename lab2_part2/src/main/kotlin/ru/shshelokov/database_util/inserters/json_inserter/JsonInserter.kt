package ru.shshelokov.database_util.inserters.json_inserter

import ru.shshelokov.database_util.inserters.base.Iinserter

abstract class JsonInserter : Iinserter {
    override val tables: Iterable<String>
        get() = listOf(NODE_TABLE, USER_TABLE)

    companion object {
         const val NODE_TABLE = "osm_json.node"
         const val USER_TABLE = "osm_json.user"
    }
}