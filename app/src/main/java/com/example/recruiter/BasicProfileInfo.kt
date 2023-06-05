package com.example.recruiter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class BasicProfileInfo(val context:Context) {

    private val Context.datastore : DataStore<Preferences>  by preferencesDataStore("BASIC_USER_INFO")

    companion object{
       /** val userType = stringPreferencesKey("USER_TYPE")**/
        val userName = stringPreferencesKey("USER_NAME")
        val userPhoneNumber = stringPreferencesKey("PHONE_NUMBER")
        val userEmailId = stringPreferencesKey("EMAIL_ID")
        val userProfileImg = stringPreferencesKey("PROFILE_IMG")
        val userProfileBannerImg = stringPreferencesKey("PROFILE_BANNER_IMG")
        val userTagLine = stringPreferencesKey("TAG_LINE")
        val userCurrentCompany = stringPreferencesKey("USER_CURRENT_COMPANY")
    }
    

    suspend fun storeProfileData(
        /**type:String,**/
        name:String,
        phoneNumber:String,
        emailId:String,
        profileImg:String,
        profileBannerImg:String,
        tageLine:String,
        currentCompany:String){

        context.datastore.edit {
            it[userName] = name
            it[userPhoneNumber] = phoneNumber
            it[userEmailId] = emailId
            it[userProfileImg] = profileImg
            it[userProfileBannerImg] = profileBannerImg
            it[userTagLine] = tageLine
            it[userCurrentCompany] = currentCompany
        }
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



}