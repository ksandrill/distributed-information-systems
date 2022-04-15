package ru.shshelokov.database_util.inserters.base

import org.openstreetmap.osm.Node
import java.sql.Connection

interface Iinserter {
    fun clean(connection: Connection) {
        connection.createStatement().use { statement ->
            for (table in tables) {
                statement.execute("delete from $table")
            }
        }
    }

    val tables: Iterable<String?>

    fun insert(
        nodes: Iterable<Node?>,
        connection: Connection,
        shouldContinue: (Long) -> Boolean
    ): Long
}