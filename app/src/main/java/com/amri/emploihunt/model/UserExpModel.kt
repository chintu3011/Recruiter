package com.amri.emploihunt.model

import java.io.Serializable
import java.time.Duration

data class UserExpModel(
    val data: ArrayList<Experience>,
    val message: String,
    val status: Int
):Serializable

data class Experience(
    val vDesignation: String,
    val vCompanyName: String,
    val vJobLocation: String,
    val bIsCurrentCompany:Int,
    val vDuration: String
)
