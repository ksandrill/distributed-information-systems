package ru.shshelokov.database_util.inserters.relation_inserter

import ru.shshelokov.database_util.inserters.base.Iinserter

abstract class RelationInserter : Iinserter {
    protected val NODE_TABLE = "osm.node"
    protected  val TAG_TABLE = "osm.tag"
    protected  val USER_TABLE = "osm.user"



    override val tables: Iterable<String>
        get() = listOf(TAG_TABLE, NODE_TABLE, USER_TABLE)

    companion object {
         const val NODE_TABLE = "osm.node"
         const val TAG_TABLE = "osm.tag"
         const val USER_TABLE = "osm.user"
    }
}