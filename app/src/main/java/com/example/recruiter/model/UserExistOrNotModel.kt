package com.example.recruiter.model

data class UserExistOrNotModel(
    val data: DataUserExistOrNotModel,
    val message: String,
    val status: Int
)

data class DataUserExistOrNotModel(
    val iUserId: Int,

)