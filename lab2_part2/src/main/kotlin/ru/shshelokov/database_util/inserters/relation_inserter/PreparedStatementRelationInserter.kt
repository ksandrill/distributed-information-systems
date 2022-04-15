package ru.shshelokov.database_util.inserters.relation_inserter

import org.openstreetmap.osm.Node
import org.openstreetmap.osm.Tag
import ru.shshelokov.database_util.inserters.relation_inserter.PreparedStatementRelationInserter.StatementFormatter.setArguments
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp

class PreparedStatementRelationInserter(private val batchSize: Long = -1) : RelationInserter() {
    private var currentBatchSize: Long = 0

    override fun insert(
        nodes: Iterable<Node?>,
        connection: Connection,
        shouldContinue: (Long) -> Boolean
    ): Long {
        var insertedCount = 0L
        StatementFormatter.makeInsertNode(connection).use { insertNode ->
            StatementFormatter.makeInsertTag(connection).use { insertTag ->
                StatementFormatter.makeInsertUser(connection).use { insertUser ->
                    for (node in nodes) {
                        if (!shouldContinue(insertedCount)) {
                            break
                        }
                        if (node != null) {
                            insertNode(node, insertNode, insertTag, insertUser)
                        }
                        insertedCount += 1
                        handlePrepared(insertNode, insertTag, insertUser)
                    }
                    insertUser.executeBatch()
                    insertNode.executeBatch()
                    insertTag.executeBatch()
                }
            }
        }
        return insertedCount
    }

    private fun insertNode(
        node: Node,
        insertNode: PreparedStatement,
        insertTag: PreparedStatement,
        insertUser: PreparedStatement
    ) {
        setArguments(insertUser, node.uid.toLong(), node.user)
        handlePrepared(insertUser)
        setArguments(insertNode, node)
        handlePrepared(insertNode)
        for (tag in node.tag) {
            setArguments(insertTag, tag, node.id.toLong())
            handlePrepared(insertTag)
        }
    }

    private fun handlePrepared(statement: PreparedStatement) {
        if (batchSize <= 0) {
            statement.execute()
            return
        }
        statement.addBatch()
        currentBatchSize += 1
    }

    private fun handlePrepared(
        insertNode: PreparedStatement,
        insertTag: PreparedStatement,
        insertUser: PreparedStatement
    ) {
        if (batchSize <= 0 || currentBatchSize < batchSize) {
            return
        }
        insertUser.executeBatch()
        insertNode.executeBatch()
        insertTag.executeBatch()
        currentBatchSize = 0
    }

    private object StatementFormatter {
        fun makeInsertNode(
            connection: Connection
        ): PreparedStatement {
            return connection.prepareStatement(String.format("insert into %s values(?, ?, ?, ?, ?, ?, ?)", NODE_TABLE))
        }

        fun setArguments(
            statement: PreparedStatement,
            node: Node
        ) {
            var parameterIndex = 1
            statement.setLong(parameterIndex++, node.id.toLong())
            statement.setInt(parameterIndex++, node.version.toInt())
            statement.setTimestamp(
                parameterIndex++,
                Timestamp.from(
                    node
                        .timestamp
                        .toGregorianCalendar()
                        .toZonedDateTime()
                        .toInstant()
                )
            )
            statement.setLong(parameterIndex++, node.uid.toLong())
            statement.setLong(parameterIndex++, node.changeset.toLong())
            statement.setDouble(parameterIndex++, node.lat)
            statement.setDouble(parameterIndex, node.lon)
        }

        fun makeInsertTag(
            connection: Connection
        ): PreparedStatement {
            return connection.prepareStatement(String.format("insert into %s values(?, ?, ?)", TAG_TABLE))
        }

        fun setArguments(
            statement: PreparedStatement,
            tag: Tag,
            nodeId: Long
        ) = statement.run {
            var parameterIndex = 1
            setLong(parameterIndex++, nodeId)
            setString(parameterIndex++, tag.k)
            setString(parameterIndex, tag.v)
        }

        fun makeInsertUser(
            connection: Connection
        ): PreparedStatement {
            return connection.prepareStatement(
                String.format(
                    "insert into %s values(?, ?) on conflict do nothing",
                    USER_TABLE
                )
            )
        }

        fun setArguments(
            statement: PreparedStatement,
            uid: Long,
            userName: String
        ) {
            var parameterIndex = 1
            statement.setLong(parameterIndex++, uid)
            statement.setString(parameterIndex, userName)
        }
    }
}