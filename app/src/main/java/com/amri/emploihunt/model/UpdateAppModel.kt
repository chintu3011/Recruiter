package com.amri.emploihunt.model

data class UpdateAppModel(
    val data: DataUpdateApp,
    val message: String,
    val status: Int
)

data class DataUpdateApp(
    val isForceUpdate: Int,
    val latestAppVersionCode: Int,
    val tMessage: String,
    val isBlock: Int

)