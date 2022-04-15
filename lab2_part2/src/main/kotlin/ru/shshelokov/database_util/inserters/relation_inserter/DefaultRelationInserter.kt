package ru.shshelokov.database_util.inserters.relation_inserter

import org.openstreetmap.osm.Node
import org.openstreetmap.osm.Tag
import ru.shshelokov.database_util.DataBaseUtil
import java.sql.Connection
import java.sql.Statement
import java.text.MessageFormat

class DefaultRelationInserter : RelationInserter() {
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

    private object QueryFormatter {
        fun makeInsert(
            tag: Tag,
            nodeId: Long,
            table: String
        ): String {
            return MessageFormat.format(
                "insert into {0} values ({1,number,#}, ''{2}'', ''{3}'')",
                table,
                nodeId, DataBaseUtil.escape(tag.k), DataBaseUtil.escape(tag.v)
            )
        }

        fun makeInsert(
            node: Node,
            table: String
        ): String {
            return MessageFormat.format(
                "insert into {0} values ({1,number,#}, {2,number,#}, " +
                        "''{3}'', {4,number,#}, {5,number,#}, {6,number,#}, {7,number,#})",
                table,
                node.id, node.version, node.timestamp,
                node.uid, node.changeset,
                node.lat, node.lon
            )
        }

        fun makeInsert(
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
    }

    companion object {
        private fun insertTag(
            tag: Tag,
            nodeId: Long,
            statement: Statement
        ) {
            val query= QueryFormatter.makeInsert(tag, nodeId, TAG_TABLE)
            statement.execute(query)
        }

        private fun insertNode(
            node: Node,
            statement: Statement
        ) {
            var query = QueryFormatter.makeInsert(node.uid.toLong(), node.user, USER_TABLE)
            statement.execute(query)
            query = QueryFormatter.makeInsert(node, NODE_TABLE)
            statement.execute(query)
            for (tag in node.tag) {
                insertTag(tag, node.id.toLong(), statement)
            }
        }
    }
}