package com.amri.emploihunt.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map


private val Context.datastore : DataStore<Preferences> by preferencesDataStore("USER_DATA_REPOSITORY")

data class UserDataRepository(val context: Context) {

    companion object{
        val vFirstName = stringPreferencesKey("userFName")
        val vLastName = stringPreferencesKey("userLName")
        val tTagLIne = stringPreferencesKey("userTagLine")
        val vMobile = stringPreferencesKey("userMobile")
        val vEmail = stringPreferencesKey("userEmail")
        val vCity = stringPreferencesKey("UserResidentialCity")

        val tBio = stringPreferencesKey("userBio")

        val vQualification = stringPreferencesKey("userQualification")

        val vCurrentCompany = stringPreferencesKey("userCurrentCompany")
        val vDesignation = stringPreferencesKey("userDesignation")
        val vJobLocation = stringPreferencesKey("userJobLocation")


        val vPreferCity = stringPreferencesKey("userPrefCity")
        val vPreferJobTitle = stringPreferencesKey("userPrefJobTitle")
        val vPreferWorkingMode = stringPreferencesKey("userPrefWorkingMode")

        val tProfileUrl = stringPreferencesKey("userProfileImgUrl")
        val tProfileBannerUrl = stringPreferencesKey("userProfileBannerImgUrl")

        val tResumeUrl = stringPreferencesKey("userResumeUrl")
    }

    suspend fun storeBasicInfo(
        fName:String,
        lName:String,
        mobile:String,
        email:String,
        tagLine:String,
        residentialCity:String
    ){
        context.datastore.edit {
            it[vFirstName] = fName
            it[vLastName] = lName
            it[vMobile] = mobile
            it[vEmail] = email
            it[tTagLIne] = tagLine
            it[vCity] = residentialCity
        }
    }
    suspend fun storeAboutData(
        bio:String,
    ){
        context.datastore.edit {
            it[tBio] = bio
        }
    }
    suspend fun storeQualificationData(
        qualification:String
    ){
        context.datastore.edit {
            it[vQualification] = qualification
        }
    }

    suspend fun storeCurrentPositionData(
        currentCompany:String,
        designation: String,
        jobLocation:String,
        workingMode:String
    ){
        context.datastore.edit {
            it[vCurrentCompany] = currentCompany
            it[vDesignation] = designation
            it[vJobLocation] = jobLocation
            it[vPreferWorkingMode] = workingMode
        }
    }

    

    suspend fun storeResumeData(
        resumeUrl:String
    ){
        context.datastore.edit {
            it[tResumeUrl] = resumeUrl
        }
    }

    suspend fun storeJobPreferenceData(
        prefJobTitle:String,
        prefCity:String,
        prefWorkingMode:String
    ){
        context.datastore.edit {
            it[vPreferJobTitle] = prefJobTitle
            it[vPreferCity] = prefCity
            it[vPreferWorkingMode] = prefWorkingMode
        }
    }

    suspend fun storeProfileImg(
        profileImgUrl:String,
    ){
        context.datastore.edit {
            it[tProfileUrl] = profileImgUrl
        }
    }
    suspend fun storeProfileBannerImg(
        profileBannerUrl:String,
    ){
        context.datastore.edit {
            it[tProfileBannerUrl] = profileBannerUrl
        }
    }

    fun getData(variable: Preferences.Key<String>) = context.datastore.data.map{
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

    suspend fun emptyDataStore(){
        context.datastore.edit {
            it.clear()
        }
    }

    fun getUserFName() = context.datastore.data.map {
        it[vFirstName]?:""
    }
    fun getUserLName() = context.datastore.data.map {
        it[vLastName]?:""
    }
    fun getUserFullName() = context.datastore.data.map {

        val firstName = it[vFirstName]?.toString() ?: ""
        val lastName = it[vLastName]?.toString() ?: ""
        "$firstName $lastName"
    }
    fun getUserPhoneNumber() = context.datastore.data.map {
        it[vMobile]?:""
    }
    fun getUserEmailId() = context.datastore.data.map {
        it[vEmail]?:""
    }
    fun getUserProfileImgUrl() = context.datastore.data.map {
        it[tProfileUrl]?:""
    }
    fun getUserProfileBannerUrl() = context.datastore.data.map {
        it[tProfileBannerUrl]?:""
    }
    fun getUserTageLine() = context.datastore.data.map {
        it[tTagLIne]?:""
    }

    fun getResidentialCity() = context.datastore.data.map {
        it[vCity]?:""
    }
    fun getUserBio() = context.datastore.data.map {
        it[tBio]?:""
    }
    fun getUserQualification() = context.datastore.data.map {
        it[vQualification]?:""
    }

    fun getUserCurrentCompany() = context.datastore.data.map {
        it[vCurrentCompany]?:""
    }
    fun getUserDesignation() = context.datastore.data.map {
        it[vDesignation]?:""
    }

    fun getUserJobLocation() = context.datastore.data.map {
        it[vJobLocation]?:""
    }
    fun getUserResumeUri() = context.datastore.data.map {
        it[tResumeUrl]?:""
    }
    fun getUserPerfJobTitle() = context.datastore.data.map {
        it[vPreferJobTitle]?:""
    }
    fun getUserPrefJobLocation() = context.datastore.data.map {
        it[vPreferCity]?:""
    }
    fun getUserPrefWorkingMode() = context.datastore.data.map {
        it[vPreferWorkingMode]?:""
    }

}