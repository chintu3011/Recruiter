package com.example.recruiter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.lang.reflect.TypeVariable

private val Context.datastore : DataStore<Preferences> by preferencesDataStore("JOB_SEEKER_PROFILE_INFO")

data class JobSeekerProfileInfo(val context: Context) {

    companion object{
        val userType = stringPreferencesKey("userType")
        val userId = stringPreferencesKey("userId")

        val userFName = stringPreferencesKey("userFName")
        val userLName = stringPreferencesKey("userLName")
        val userPhoneNumber = stringPreferencesKey("userPhoneNumber")
        val userEmailId = stringPreferencesKey("userEmailId")
        val userProfileImg = stringPreferencesKey("userProfileImg")
        val userProfileBannerImg = stringPreferencesKey("userProfileBannerImg")
        val userTagLine = stringPreferencesKey("userTagLine")
        val userCurrentCompany = stringPreferencesKey("userCurrentCompany")

        val userBio = stringPreferencesKey("userBio")
        val userQualification = stringPreferencesKey("userQualification")
        val userExperienceState = stringPreferencesKey("userExperienceState")
        val userDesignation = stringPreferencesKey("userDesignation")
        val userPrevCompany = stringPreferencesKey("userPrevCompany")
        val userPrevJobDuration = stringPreferencesKey("userPrevJobDuration")
        val userResumeUri = stringPreferencesKey("userResumeUri")
        val userResumeFileName = stringPreferencesKey("userResumeFileName")
        val userPerfJobTitle = stringPreferencesKey("userPerfJobTitle")
        val userExpectedSalary = stringPreferencesKey("userExpectedSalary")
        val userPrefJobLocation = stringPreferencesKey("userPrefJobLocation")
        val userWorkingMode = stringPreferencesKey("userWorkingMode")
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
        fName:String,
        lName:String,
        phoneNumber:String,
        emailId:String,
        tageLine:String,
        currentCompany:String,

    ){
        context.datastore.edit {
            it[userFName] = fName
            it[userLName] = lName
            it[userPhoneNumber] = phoneNumber
            it[userEmailId] = emailId
            it[userTagLine] = tageLine
            it[userCurrentCompany] = currentCompany
        }
    }
    suspend fun storeUserType(
        type:String,
        id:String
    ){
        context.datastore.edit {
            it[userType] = type
            it[userId] = id
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
    fun getData(variable:Preferences.Key<String>) = context.datastore.data.map{
        it[variable]?:""
    }
    suspend fun readAllKeys(): Set<Preferences.Key<*>>? {
        val keys = context.datastore.data
            .map {
                it.asMap().keys
            }
        return keys.firstOrNull()
    }
    suspend fun getValueByKey(key: Preferences.Key<*>): Any? {
        val value = context.datastore.data
            .map {
                it[key]
            }
        return value.firstOrNull()
    }
    fun getUserType() = context.datastore.data.map{
        it[userType]?:""
    }
    fun getUserId() = context.datastore.data.map{
        it[userId]?:""
    }
    fun getUserFName() = context.datastore.data.map {
        it[userFName]?:""
    }
    fun getUserLName() = context.datastore.data.map {
        it[userLName]?:""
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
