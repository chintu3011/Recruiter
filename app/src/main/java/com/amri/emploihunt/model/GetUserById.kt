package com.amri.emploihunt.model

import java.io.Serializable

data class GetUserById(
    val data: User,
    val message: String,
    val status: Int
)

data class User(
    val id: Int,
    val vFirebaseId:String,
    val iRole: Int,
    val vFirstName: String,
    val vLastName: String,
    val vMobile: String,
    val vEmail: String,
    val vQualification: String,
    val tProfileUrl: String,
    val tResumeUrl: String,
    val vCurrentCompany: String,
    val vDesignation: String,
    val vJobLocation: String,
    val vDuration: String,
    val vWorkingMode: String,
    val tBio: String,
    val tTagLine: String,
    val vCity: String,
    /**/val vPreferCity: String,
    val vExpectedSalary: String,
    val isLogin: String,
    val isBlock: String,
    val tCreatedAt: String,
    val tUpadatedAt: String
):Serializable