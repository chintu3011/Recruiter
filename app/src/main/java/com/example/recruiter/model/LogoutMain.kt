package com.example.recruiter.model

import com.google.gson.annotations.SerializedName

data class LogoutMain(

    @SerializedName("message")
    var message: String? = null,

    @SerializedName("data")
    var data: DataMsg
)
data class DataMsg(
    @SerializedName("msg")
    var msg: String? = null,
)