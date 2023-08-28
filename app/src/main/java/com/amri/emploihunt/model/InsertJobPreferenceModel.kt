package com.amri.emploihunt.model

data class InsertJobPreferenceModel(
    val data: DataJobPreference,
    val message: String,
    val status: Int
)
data class DataJobPreference(
    val message: String
)