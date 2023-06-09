package com.example.recruiter

import androidx.datastore.preferences.core.stringPreferencesKey

data class UsersJobSeeker(

    var userFName :String ?= null,
    var userLName :String ?= null,
    var userPhoneNumber :String ?= null,
    var userEmailId :String ?= null,
    var userProfileImg :String ?= null,
    var userProfileBannerImg :String ?= null,
    var userTagLine :String ?= null,
    var userCurrentCompany :String ?= null,

    var userBio :String ?= null,
    var userQualification :String ?= null,
    var userExperienceState :String ?= null,
    var userDesignation :String ?= null,
    var userPrevCompany :String ?= null,
    var userPrevJobDuration :String ?= null,
    var userResumeUri :String ?= null,
    var userResumeFileName :String ?= null,
    var userPerfJobTitle :String ?= null,
    var userExpectedSalary :String ?= null,
    var userPrefJobLocation :String ?= null,
    var userWorkingMode :String ?= null
)
