package ru.shshelokov.database_util.inserters.enums

enum class SchemaType {
    RELATIONS, JSON, CUSTOM_TYPES;

    override fun toString(): String {
        return when (this) {
            RELATIONS -> "relations"
            JSON -> "json"
            CUSTOM_TYPES -> "custom-types"
        }
    }
}