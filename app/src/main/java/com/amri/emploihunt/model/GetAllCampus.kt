package com.amri.emploihunt.model

data class GetAllCampus(
    val data: List<DataCampus>,
    val message: String,
    val status: Int,
    val current_page: Int,
    val limit: Int,
    val total_records: Int,
    val total_pages: Int
)
data class DataCampus(
    val id: Int,
    val vCampusName: String,
    val tCampusAddress: String,
    val vQulification: String,
    val tVacancy: String,
    val iNumberOfApplied: Int,
    val iStatus: Int,
    var iIsApplied: Int,
    val tRegistrationEndDate: String,
    val tCreatedAt: String,
    val tUpadatedAt: String,

    )
