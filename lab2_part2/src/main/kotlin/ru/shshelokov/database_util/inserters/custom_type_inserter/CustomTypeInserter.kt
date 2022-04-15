package ru.shshelokov.database_util.inserters.custom_type_inserter

import ru.shshelokov.database_util.inserters.base.Iinserter

interface CustomTypeInserter : Iinserter {
    override val tables: Iterable<String>
        get() = listOf(NODE_TABLE, USER_TABLE)

    companion object {
        const val NODE_TABLE = "osm_custom_types.node"
        const val USER_TABLE = "osm_custom_types.user"
    }
}