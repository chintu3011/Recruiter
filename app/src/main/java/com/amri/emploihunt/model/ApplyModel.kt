package com.amri.emploihunt.model

data class ApplyModel(
    val data: DataApply,
    val message: String,
    val status: Int
)
data class DataApply(
    val message: String,
)