package ru.shshelokov.database_util

import ru.shshelokov.logger
import java.io.IOException
import java.sql.Connection
import java.sql.SQLException
import java.util.*


object DataBaseUtil {
    private val log = logger<DataBaseUtil>()

    private val SCHEMA_FILES = arrayOf(
        "/db/schema/osm_schema_relations.sql",
        "/db/schema/osm_schema_json.sql",
        "/db/schema/osm_schema_custom_types.sql"
    )

    fun createDatabaseStructure(connection: Connection) {
        val autocommit = connection.autoCommit
        connection.autoCommit = false
        try {
            for (schemaFile in SCHEMA_FILES) {
                createSchema(connection, schemaFile)
            }
        } catch (e: IOException) {
            connection.rollback()
            throw e
        } catch (e: SQLException) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = autocommit
        }
    }

    private fun createSchema(
        connection: Connection,
        schemaFile: String
    ) {
        DataBaseUtil::class.java.getResourceAsStream(schemaFile).use { input ->
            connection.createStatement().use { statement ->
                log.debug(String.format("Creating schema \"%s\"", schemaFile))
                if (null == input) {
                    log.error("Schema file not found")
                    throw IOException(String.format("Schema file \"%s\" not found", schemaFile))
                }
                Scanner(input).use { scanner ->
                    scanner.useDelimiter(";\r?\n?")
                    while (scanner.hasNext()) {
                        val query = scanner.next().replace("\r?\n", "\n") /// was replaceAll&
                        log.debug(String.format("Executing \"%s\"", repr(query)))
                        statement.execute(query)
                    }
                }
                log.debug("Created schema")
            }
        }
    }

    fun escape(s: String): String {
        return s
            .replace("\\\\".toRegex(), "\\\\\\\\")
            .replace("\b".toRegex(), "\\b")
            .replace("\n".toRegex(), "\\n")
            .replace("\r".toRegex(), "\\r")
            .replace("\t".toRegex(), "\\t")
            .replace("\\x1A".toRegex(), "\\Z")
            .replace("\\x00".toRegex(), "\\0") // evil stuff ahead
            .replace("'".toRegex(), "")
            .replace("\"".toRegex(), "")
            .replace(",".toRegex(), "")
            .replace("\\)".toRegex(), "")
            .replace("\\(".toRegex(), "")
    }

    private fun repr(query: String): String {
        val end = query.indexOf('\n')
        val result = query.substring(0, if (end > 0) end else query.length)
        return if (end > 0) {
            "$result..."
        } else result
    }
}