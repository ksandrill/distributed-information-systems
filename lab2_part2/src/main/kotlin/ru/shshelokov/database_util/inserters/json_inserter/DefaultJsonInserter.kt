package ru.shshelokov.database_util.inserters.json_inserter

import org.json.JSONArray
import org.openstreetmap.osm.Node
import java.sql.Connection
import java.sql.Statement
import java.text.MessageFormat

class DefaultJsonInserter : JsonInserter() {
    override fun insert(
        nodes: Iterable<Node?>,
        connection: Connection,
        shouldContinue: (Long)->Boolean
    ): Long {
        var insertedCount = 0L
        connection.createStatement().use { statement ->
            for (node in nodes) {
                if (!shouldContinue(insertedCount)) {
                    break
                }
                if (node != null) {
                    insertNode(node, statement)
                }
                insertedCount += 1
            }
        }
        return insertedCount
    }


    companion object {
        private fun makeInsert(
            node: Node,
            tagsJson: JSONArray,
            table: String
        ): String {
            return MessageFormat.format(
                "insert into {0} values ({1,number,#}, {2,number,#}, " +
                        "''{3}'', {4,number,#}, {5,number,#}, {6,number,#}, {7,number,#}, ''{8}''::jsonb)",
                table,
                node.id, node.version, node.timestamp,
                node.uid, node.changeset,
                node.lat, node.lon,
                tagsJson.toString()
            )
        }

        private fun makeInsert(
            userId: Long,
            userName: String,
            table: String
        ): String {
            return MessageFormat.format(
                "insert into {0} values ({1,number,#}, ''{2}'') on conflict do nothing",
                table,
                userId, userName
            )
        }
        private fun insertNode(
            node: Node,
            statement: Statement
        ) {
            var query = makeInsert(node.uid.toLong(), node.user, USER_TABLE)
            statement.execute(query)
            val tagsJson = serialize(node.tag)
            query = makeInsert(node, tagsJson, NODE_TABLE)
            statement.execute(query)
        }
    }
}