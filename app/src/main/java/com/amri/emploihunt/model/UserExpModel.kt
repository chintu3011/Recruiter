package com.amri.emploihunt.model

import java.io.Serializable
import java.time.Duration

data class UserExpModel(
    val data: ArrayList<Experience>,
    val message: String,
    val status: Int
):Serializable

data class ExperienceModel(
    val data: Experience,
    val message: String,
    val status: Int
)

data class Experience(
    val id:Int,
    val vDesignation: String,
    val vCompanyName: String,
    val vJobLocation: String,
    val bIsCurrentCompany:Int,
    val vDuration: String?,
    val iUserId: Int,
    val tCreatedAt: String,
    val tUpadatedAt:String
)
