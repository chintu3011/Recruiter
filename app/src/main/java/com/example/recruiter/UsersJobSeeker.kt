package com.example.recruiter

data class UsersJobSeeker(
    var firstName: String ?= null,
    var lastName: String ?= null,
    var phoneNo : String ?= null,
    var email: String ?= null,

    var qualification: String ?= null,
    var experience :String ?= null,
    var prevCompanyName :String ?= null,
    var designation :String ?= null,
    var duration :String ?= null,
    var bio :String ?= null,
    var job :String ?= null,
    var cityPreferences :String ?= null,
    var expectedSalary :String ?= null,
    var workingMode :String ?= null,
    var termsConditionsAcceptance :String ?= null

)
