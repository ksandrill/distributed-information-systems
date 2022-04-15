package ru.shshelokov.database_util.inserters.base

import ru.shshelokov.database_util.inserters.enums.InsertMethod
import ru.shshelokov.database_util.inserters.enums.SchemaType
import ru.shshelokov.database_util.inserters.custom_type_inserter.DefaultCustomTypeInserter
import ru.shshelokov.database_util.inserters.custom_type_inserter.PreparedStatementCustomTypeInserter
import ru.shshelokov.database_util.inserters.json_inserter.DefaultJsonInserter
import ru.shshelokov.database_util.inserters.json_inserter.PreparedStatementJsonInserter
import ru.shshelokov.database_util.inserters.relation_inserter.DefaultRelationInserter
import ru.shshelokov.database_util.inserters.relation_inserter.PreparedStatementRelationInserter

class InserterFactory(batchSize: Long) {
    private val inserters: Map<Pair<InsertMethod, SchemaType>, () -> Iinserter> = mapOf(
        Pair(InsertMethod.DEFAULT, SchemaType.RELATIONS) to { DefaultRelationInserter() },
        Pair(InsertMethod.BATCH, SchemaType.RELATIONS) to { PreparedStatementRelationInserter(batchSize) },
        Pair(InsertMethod.PREPARED_STATEMENT, SchemaType.RELATIONS) to { PreparedStatementRelationInserter() },
        Pair(InsertMethod.DEFAULT, SchemaType.JSON) to { DefaultJsonInserter() },
        Pair(InsertMethod.BATCH, SchemaType.JSON) to { PreparedStatementJsonInserter(batchSize) },
        Pair(InsertMethod.PREPARED_STATEMENT, SchemaType.JSON) to { PreparedStatementJsonInserter() },
        Pair(InsertMethod.DEFAULT, SchemaType.CUSTOM_TYPES) to { DefaultCustomTypeInserter() },
        Pair(InsertMethod.BATCH, SchemaType.CUSTOM_TYPES) to { PreparedStatementCustomTypeInserter(batchSize) },
        Pair(InsertMethod.PREPARED_STATEMENT, SchemaType.CUSTOM_TYPES) to { PreparedStatementCustomTypeInserter() },
    )


    fun getInserter(
        method: InsertMethod,
        schemaType: SchemaType
    ): Iinserter {
        val inserterKey = Pair(method, schemaType)
        if (!inserters.containsKey(inserterKey)) {
            throw InserterNotImplementedException(method, schemaType)
        }
        return inserters[inserterKey]!!()
    }

    class InserterNotImplementedException(
        method: InsertMethod,
        schemaType: SchemaType
    ) : Exception("No inserter for type  $schemaType,  method $method")
}