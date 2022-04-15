package ru.shshelokov.database_util.inserters.json_inserter

import org.openstreetmap.osm.Node
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp

class PreparedStatementJsonInserter(private val batchSize: Long = -1) : JsonInserter() {
    private var currentBatchSize: Long = 0

    override fun insert(
        nodes: Iterable<Node?>,
        connection: Connection,
        shouldContinue: (Long) -> Boolean
    ): Long {
        var insertedCount = 0L
        StatementFormatter.makeInsertNode(connection).use { insertNode ->
            StatementFormatter.makeInsertUser(connection).use { insertUser ->
                for (node in nodes) {
                    if (!shouldContinue(insertedCount)) {
                        break
                    }
                    if (node != null) {
                        insertNode(node, insertNode, insertUser)
                    }
                    insertedCount += 1
                    handlePrepared(insertNode, insertUser)
                }
                insertNode.executeBatch()
                insertUser.executeBatch()
            }
        }
        return insertedCount
    }

    private fun insertNode(
        node: Node,
        insertNode: PreparedStatement,
        insertUser: PreparedStatement
    ) {
        StatementFormatter.setArguments(insertUser, node.uid.toLong(), node.user)
        handlePrepared(insertUser)
        StatementFormatter.setArguments(insertNode, node)
        handlePrepared(insertNode)
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
        insertUser: PreparedStatement
    ) {
        if (batchSize <= 0 || currentBatchSize < batchSize) {
            return
        }
        insertUser.executeBatch()
        insertNode.executeBatch()
        currentBatchSize = 0
    }

    private object StatementFormatter {
        fun makeInsertNode(
            connection: Connection
        ): PreparedStatement {
            return connection.prepareStatement(
                String.format(
                    "insert into %s values(?, ?, ?, ?, ?, ?, ?, ?::jsonb)",
                    NODE_TABLE
                )
            )
        }

        fun setArguments(
            statement: PreparedStatement,
            node: Node
        ) = statement.run {
            var parameterIndex = 1
            setLong(parameterIndex++, node.id.toLong())
            setInt(parameterIndex++, node.version.toInt())
            setTimestamp(
                parameterIndex++,
                Timestamp.from(
                    node
                        .timestamp
                        .toGregorianCalendar()
                        .toZonedDateTime()
                        .toInstant()
                )
            )
            setLong(parameterIndex++, node.uid.toLong())
            setLong(parameterIndex++, node.changeset.toLong())
            setDouble(parameterIndex++, node.lat)
            setDouble(parameterIndex++, node.lon)
            val tagsJson = serialize(node.tag)
            setString(parameterIndex, tagsJson.toString())
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
        ) = statement.run {
            var parameterIndex = 1
            setLong(parameterIndex++, uid)
            setString(parameterIndex, userName)
        }
    }
}