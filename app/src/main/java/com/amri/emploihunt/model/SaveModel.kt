package com.amri.emploihunt.model

data class SaveModel(
    val data: DataSave,
    val message: String,
    val status: Int
)
data class DataSave(
    val message: String
    )