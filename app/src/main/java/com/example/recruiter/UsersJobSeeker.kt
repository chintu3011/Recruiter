package com.example.recruiter

import java.io.Serializable

data class UsersJobSeeker(

    var userId :String = "",
    
    var userFName :String = "",
    var userLName :String = "",
    var userPhoneNumber :String = "",
    var userEmailId :String = "",
    var userProfileImg :String = "",
    var userProfileBannerImg :String = "",
    var userTagLine :String = "",
    var userCurrentCompany :String = "",

    var userBio :String = "",
    var userQualification :String = "",
    var userExperienceState :String = "",
    var userDesignation :String = "",
    var userPrevCompany :String = "",
    var userPrevJobDuration :String = "",
    var userResumeUri :String = "",
    var userResumeFileName :String = "",
    var userPerfJobTitle :String = "",
    var userExpectedSalary :String = "",
    var userPrefJobLocation :String = "",
    var userWorkingMode :String = ""
): Serializable
