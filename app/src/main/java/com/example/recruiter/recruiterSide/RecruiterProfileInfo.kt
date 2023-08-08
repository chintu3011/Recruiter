package com.example.recruiter.recruiterSide

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map


private val Context.datastore : DataStore<Preferences> by preferencesDataStore("RECRUITER_PROFILE_INFO")
data class RecruiterProfileInfo(val context: Context){
    
    companion object {

        val userType = stringPreferencesKey("userType")
        val userId = stringPreferencesKey("userId")
        val userFName = stringPreferencesKey("userFName")
        val userLName = stringPreferencesKey("userLName")
        val userPhoneNumber = stringPreferencesKey("userPhoneNumber")
        val userEmailId = stringPreferencesKey("userEmailId")
        val userProfileImg = stringPreferencesKey("userProfileImg")
        val userProfileImgUri = stringPreferencesKey("userProfileImgUri")
        val userProfileBannerImg = stringPreferencesKey("userProfileBannerImg")
        val userProfileBannerImgUri = stringPreferencesKey("userProfileImgUri")
        val userTagLine = stringPreferencesKey("userTagLine")
        val userCurrentCompany = stringPreferencesKey("userCurrentCompany")

        val userJobTitle = stringPreferencesKey("userJobTitle")
        val userSalary = stringPreferencesKey("userSalary")
        val userJobLocation = stringPreferencesKey("v")
        val userBio  = stringPreferencesKey("userBio")
        val userDesignation = stringPreferencesKey("userDesignation")
        val userWorkingMode = stringPreferencesKey("userWorkingMode")
    }

    suspend fun storeAboutData(
        jobTitle:String,
        salary:String,
        jobLocation:String,
        bio:String,
        designation:String,
        workingMode:String
    ){
        context.datastore.edit {
            it[userJobTitle] = jobTitle
            it[userSalary] = salary
            it[userJobLocation] = jobLocation
            it[userBio] = bio
            it[userDesignation] = designation
            it[userWorkingMode] = workingMode
        }
    }

    suspend fun storeUserType(
        type:String,
        id:String
    ){
        context.datastore.edit {
            it[userType] = type
            it[userId]  = id
        }
    }

    suspend fun storeBasicProfileData(
        fName:String,
        lName:String,
        phoneNumber:String,
        emailId:String,
        tageLine:String,
        currentCompany:String
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
    suspend fun storeProfileImg(
        profileImg:String,
        profileImgUri:String
    ){
        context.datastore.edit {
            it[userProfileImg] = profileImg
            it[userProfileImgUri] = profileImgUri
        }
    }

    suspend fun storeProfileBannerImg(
        profileBannerImg:String,
        profileBannerImgUri: String
    ){
        context.datastore.edit {
            it[userProfileBannerImg] = profileBannerImg
            it[userProfileBannerImgUri] = profileBannerImgUri
        }
    }

    suspend fun emptyDataStore(){
        context.datastore.edit {
            it.clear()
        }
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
    fun getUserProfileImgUri() = context.datastore.data.map {
        it[userProfileImgUri]?:""
    }
    fun getUserProfileBannerImg() = context.datastore.data.map {
        it[userProfileBannerImg]?:""
    }
    fun getUserProfileBannerImgUri() = context.datastore.data.map {
        it[userProfileBannerImgUri]?:""
    }
    fun getUserTageLine() = context.datastore.data.map {
        it[userTagLine]?:""
    }
    fun getUserCurrentCompany() = context.datastore.data.map {
        it[userCurrentCompany]?:""
    }
    fun getUserJobTitle() = context.datastore.data.map {
        it[userJobTitle]?:""
    }
    fun getUserSalary() = context.datastore.data.map {
        it[userSalary]?:""
    }
    fun getUserJobLocation() = context.datastore.data.map {
        it[userJobLocation]?:""
    }
    fun getUserBio() = context.datastore.data.map {
        it[userBio]?:""
    }
    fun getUserDesignation() = context.datastore.data.map {
        it[userDesignation]?:""
    }
    fun getUserWorkingMode() = context.datastore.data.map {
        it[userWorkingMode]?:""
    }
}