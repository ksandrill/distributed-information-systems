package ru.shshelokov.database_util.inserters.custom_type_inserter

import org.openstreetmap.osm.Node
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp

class PreparedStatementCustomTypeInserter(private val batchSize: Long = -1) :
    CustomTypeInserter {
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
                    insertNode(connection, node, insertNode, insertUser)
                    insertedCount += 1
                    handlePrepared(insertNode, insertUser)
                }
                insertUser.executeBatch()
                insertNode.executeBatch()
            }
        }
        return insertedCount
    }

    private fun insertNode(
        connection: Connection,
        node: Node?,
        insertNode: PreparedStatement,
        insertUser: PreparedStatement
    ) {
        StatementFormatter.setArguments(insertUser, node!!.uid.toLong(), node.user)
        handlePrepared(insertUser)
        StatementFormatter.setArguments(connection, insertNode, node)
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
                    "insert into %s values(?, ?, ?, ?, ?, ?, ?, ?)",
                    CustomTypeInserter.NODE_TABLE
                )
            )
        }

        fun setArguments(
            connection: Connection,
            statement: PreparedStatement,
            node: Node?
        ) = statement.run {
            var parameterIndex = 1
            setLong(parameterIndex++, node!!.id.toLong())
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
            setArray(
                parameterIndex,
                connection.createArrayOf(
                    TAG_CUSTOM_TYPE_NAME,
                    node.tag.map { TagView(it) }.toTypedArray()
                )
            )
        }


        fun makeInsertUser(
            connection: Connection
        ): PreparedStatement = connection.prepareStatement(
            String.format(
                "insert into %s values(?, ?) on conflict do nothing",
                CustomTypeInserter.USER_TABLE
            )
        )


        fun setArguments(
            statement: PreparedStatement,
            uid: Long,
            userName: String?
        ) {
            var parameterIndex = 1
            statement.setLong(parameterIndex++, uid)
            statement.setString(parameterIndex, userName)
        }
    }

    companion object {
        private const val TAG_CUSTOM_TYPE_NAME = "tag"
    }
}