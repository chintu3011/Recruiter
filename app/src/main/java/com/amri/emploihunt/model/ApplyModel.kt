package com.amri.emploihunt.model

data class ApplyModel(
    val data: DataPost,
    val message: String,
    val status: Int
)
data class DataPost(
    val tAuthToken: String,
    val iUserId: String,
    val vReferralCode: String,
    val tDeviceToken: String,
    val user: User
)