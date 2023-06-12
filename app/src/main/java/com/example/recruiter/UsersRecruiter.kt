package com.example.recruiter

data class UsersRecruiter(
    var userFName :String ?= null,
    var userLName :String ?= null,
    var userPhoneNumber :String ?= null,
    var userEmailId :String ?= null,
    var userProfileImg :String ?= null,
    var userProfileBannerImg :String ?= null,
    var userTagLine :String ?= null,
    var userCurrentCompany :String ?= null,

    var userDesignation :String ?= null,
    var userJobTitle :String ?= null,
    var userBio :String ?= null,
    var userSalary :String ?= null,
    var userJobLocation:String ?= null,
    var userWorkingMode :String ?= null,
    var termsConditionsAcceptance :String ?= null

)
