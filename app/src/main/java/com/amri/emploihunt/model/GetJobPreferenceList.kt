package com.amri.emploihunt.model

import java.io.Serializable

data class GetJobPreferenceList(
    val data: List<DataJobPreferenceList>,
    val message: String,
    val status: Int,
    val current_page: Int,
    val limit: Int,
    val total_records: Int,
    val total_pages: Int
)
data class DataJobPreferenceList(
    val id: Int,
    val iUserId: Int,
    val vJobTitle: String,
    val vExpectedSalary: String,
    val vWorkingMode: String,
    val vJobLocation: String,
    val tCreatedAt: String,
    val tUpadatedAt: String,
    val user: User? = null
):Serializable