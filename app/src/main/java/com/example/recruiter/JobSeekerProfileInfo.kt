package com.example.recruiter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.datastore : DataStore<Preferences> by preferencesDataStore("JOB_SEEKER_PROFILE_INFO")

data class JobSeekerProfileInfo(val context: Context) {

    companion object{
        val userType = stringPreferencesKey("USER_TYPE")

        val userName = stringPreferencesKey("USER_NAME")
        val userPhoneNumber = stringPreferencesKey("PHONE_NUMBER")
        val userEmailId = stringPreferencesKey("EMAIL_ID")
        val userProfileImg = stringPreferencesKey("PROFILE_IMG")
        val userProfileBannerImg = stringPreferencesKey("PROFILE_BANNER_IMG")
        val userTagLine = stringPreferencesKey("TAG_LINE")
        val userCurrentCompany = stringPreferencesKey("USER_CURRENT_COMPANY")

        val userBio = stringPreferencesKey("BIO")
        val userQualification = stringPreferencesKey("QUALIFICATION")
        val userExperienceState = stringPreferencesKey("EXPERIENCE_STATE")
        val userDesignation = stringPreferencesKey("DESIGNATION")
        val userPrevCompany = stringPreferencesKey("USER_PREV_COMPANY")
        val userPrevJobDuration = stringPreferencesKey("PREV_JOB_DURATION")
        val userResumeUri = stringPreferencesKey("RESUME_URI")
        val userResumeFileName = stringPreferencesKey("USER_RESUME_FILE_NAME")
        val userPerfJobTitle = stringPreferencesKey("PREF_JOB_TITLE")
        val userExpectedSalary = stringPreferencesKey("EXPECTED_SALARY")
        val userPrefJobLocation = stringPreferencesKey("PREF_JOB_LOCATION")
        val userWorkingMode = stringPreferencesKey("WORKING_MODE")


    }

    suspend fun storeAboutData(
        bio:String,
        qualification:String
    ){
        context.datastore.edit {
            it[userBio] = bio
            it[userQualification] = qualification
        }
    }
    suspend fun storeExperienceData(
        experienceState:String,
        designation: String,
        prevCompany:String,
        prevJobDuration:String
    ){
        context.datastore.edit {
            it[userExperienceState] = experienceState
            it[userDesignation] = designation
            it[userPrevCompany] = prevCompany
            it[userPrevJobDuration] = prevJobDuration
        }
    }
    suspend fun storeResumeData(
        resumeFileName:String,
        resumeUri:String
    ){
        context.datastore.edit {
            it[userResumeFileName] = resumeFileName
            it[userResumeUri] = resumeUri
        }
    }

    suspend fun storeJobPreferenceData(
        prefJobTitle:String,
        expectedSalary:String,
        prefJobLocation:String,
        prefWorkingMode:String
    ) {
        context.datastore.edit {
            it[userPerfJobTitle] = prefJobTitle
            it[userExpectedSalary] = expectedSalary
            it[userPrefJobLocation] = prefJobLocation
            it[userWorkingMode] = prefWorkingMode
        }
    }
    suspend fun storeBasicProfileData(
        name:String,
        phoneNumber:String,
        emailId:String,
        tageLine:String,
        currentCompany:String,

    ){
        context.datastore.edit {
            it[userName] = name
            it[userPhoneNumber] = phoneNumber
            it[userEmailId] = emailId
            it[userTagLine] = tageLine
            it[userCurrentCompany] = currentCompany
        }
    }

    suspend fun storeProfileImg(
        profileImg:String
    ){
        context.datastore.edit {
            it[userProfileImg] = profileImg
        }
    }

    suspend fun storeProfileBannerImg(
        profileBannerImg:String
    ){
        context.datastore.edit {
            it[userProfileBannerImg] = profileBannerImg
        }
    }


    fun getUserType() = context.datastore.data.map{
        it[userType]?:""
    }
    fun getUserName() = context.datastore.data.map {
        it[userName]?:""
    }
    fun getUserPhoneNumber() = context.datastore.data.map {
        it[userPhoneNumber]?:""
    }
    fun getUserEmailId() = context.datastore.data.map {
        it[userEmailId]?:""
    }
    fun getUserProfileImg() = context.datastore.data.map {
        it[userProfileImg]?:""
    }
    fun getUserProfileBannerImg() = context.datastore.data.map {
        it[userProfileBannerImg]?:""
    }
    fun getUserTageLine() = context.datastore.data.map {
        it[userTagLine]?:""
    }
    fun getUserCurrentCompany() = context.datastore.data.map {
        it[userCurrentCompany]?:""
    }
    fun getUserBio() = context.datastore.data.map {
        it[userBio]?:""
    }
    fun getUserQualification() = context.datastore.data.map {
        it[userQualification]?:""
    }
    fun getUserExperienceState() = context.datastore.data.map {
        it[userExperienceState]?:""
    }
    fun getUserDesignation() = context.datastore.data.map {
        it[userDesignation]?:""
    }
    fun getUserPrevCompany() = context.datastore.data.map {
        it[userPrevCompany]?:""
    }
    fun getUserPrevJobDuration() = context.datastore.data.map {
        it[userPrevJobDuration]?:""
    }
    fun getUserResumeFileName() = context.datastore.data.map {
        it[userResumeFileName]?:""
    }
    fun getUserResumeUri() = context.datastore.data.map {
        it[userResumeUri]?:""
    }
    fun getUserPerfJobTitle() = context.datastore.data.map {
        it[userPerfJobTitle]?:""
    }
    fun getUserExpectedSalary() = context.datastore.data.map {
        it[userExpectedSalary]?:""
    }
    fun getUserPrefJobLocation() = context.datastore.data.map {
        it[userPrefJobLocation]?:""
    }
    fun getUserPrefWorkingMode() = context.datastore.data.map {
        it[userWorkingMode]?:""
    }

}
