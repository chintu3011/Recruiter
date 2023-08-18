package com.amri.emploihunt.model

data class RegisterUserModel(
    val data: DataRegisterUser,
    val message: String,
    val status: Int
)

data class DataRegisterUser(
    val tAuthToken: String,
    val tDeviceToken: String,
    val user: User,
)