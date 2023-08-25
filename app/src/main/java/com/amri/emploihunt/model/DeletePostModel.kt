package com.amri.emploihunt.model

data class DeletePostModel(
    val data: DataDelete,
    val message: String,
    val status: Int
)
data class DataDelete(
    val message: String
)