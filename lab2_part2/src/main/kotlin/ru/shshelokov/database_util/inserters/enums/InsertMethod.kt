package ru.shshelokov.database_util.inserters.enums

enum class InsertMethod {
    DEFAULT, PREPARED_STATEMENT, BATCH;

    override fun toString(): String {
        return when (this) {
            DEFAULT -> "default"
            PREPARED_STATEMENT -> "prepared-statement"
            BATCH -> "batch"
        }
    }
}