package com.amri.emploihunt.model

import java.io.Serializable

data class AppliedCandidateModel(
    val data: List<DataAppliedCandidate>,
    val message: String,
    val status: Int,
    val current_page: Int,
    val limit: Int,
    val total_records: Int,
    val total_pages: Int
)
data class DataAppliedCandidate(
    val id: Int,
    val iUserId: Int,
    val iJobId: Int,
    val tUpadatedAt: String,
    val tCreatedAt: String,
    val userJobPref: UserJobPref
):Serializable


data class UserJobPref(
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
    val vPreferCity: String,
    val isLogin: String,
    val isBlock: String,
    val tCreatedAt: String,
    val tUpadatedAt: String,
    val jobPreference: ArrayList<DataJobPreferenceList>? = null
):Serializable