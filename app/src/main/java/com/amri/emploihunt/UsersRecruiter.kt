package com.amri.emploihunt
import java.io.Serializable

data class UsersRecruiter(

    var userId :String = "",
    
    var userFName :String = "",
    var userLName :String = "",
    var userPhoneNumber :String = "",
    var userEmailId :String = "",
    var userProfileImg :String = "",
    var userProfileBannerImg :String = "",
    var userTagLine :String = "",
    var userCurrentCompany :String = "",

    var userDesignation :String = "",
    var userJobTitle :String = "",
    var userBio :String = "",
    var userSalary :String = "",
    var userJobLocation:String = "",
    var userWorkingMode :String = "",
    var termsConditionsAcceptance :String = ""

) : Serializable
