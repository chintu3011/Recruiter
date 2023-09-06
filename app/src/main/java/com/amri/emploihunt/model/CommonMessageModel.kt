package com.amri.emploihunt.model

data class CommonMessageModel(
    val data: DataCommonMessage,
    val message: String,
    val status: Int
)

data class DataCommonMessage(
    val message: String
)