package ru.shshelokov.database_util.inserters.custom_type_inserter

import org.openstreetmap.osm.Node
import java.sql.Connection
import java.sql.Statement
import java.text.MessageFormat

class DefaultCustomTypeInserter : CustomTypeInserter {
    override fun insert(
        nodes: Iterable<Node?>,
        connection: Connection,
        shouldContinue: (Long)-> Boolean
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
            tagsCustomType: String,
            table: String
        ): String {
            return MessageFormat.format(
                "insert into {0} values ({1,number,#}, {2,number,#}, " +
                        "''{3}'', {4,number,#}, {5,number,#}, {6,number,#}, {7,number,#}, {8})",
                table,
                node.id, node.version, node.timestamp,
                node.uid, node.changeset,
                node.lat, node.lon,
                tagsCustomType
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
            var query = makeInsert(node.uid.toLong(), node.user, CustomTypeInserter.USER_TABLE)
            statement.execute(query)
            val tagsCustomType = serialize(node.tag)
            query = makeInsert(node, tagsCustomType, CustomTypeInserter.NODE_TABLE)
            statement.execute(query)
        }
    }
}