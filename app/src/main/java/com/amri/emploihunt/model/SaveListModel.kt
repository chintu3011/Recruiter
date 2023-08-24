package com.amri.emploihunt.model

data class SaveListModel(
    val data: List<DataSaveList>,
    val message: String,
    val status: Int,
    val current_page: Int,
    val limit: Int,
    val total_records: Int,
    val total_pages: Int
)
data class DataSaveList(
    val id: Int,
    val iUserId: Int,
    val iJobId: Int,
    val tCreatedAt: String,
    val tUpadatedAt: String,
    val job: Jobs,
    val user: User
)