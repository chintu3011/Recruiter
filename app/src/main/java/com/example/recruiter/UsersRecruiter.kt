package com.example.recruiter

data class UsersRecruiter(
    var firstName: String ?= null,
    var lastName: String ?= null,
    var phoneNo : String ?= null,
    var email: String ?= null,

    var prevCompanyName :String ?= null,
    var designation :String ?= null,
    var job :String ?= null,
    var jobDescription :String ?= null,
    var expectedSalary :String ?= null,
    var cityPreferences :String ?= null,
    var workingMode :String ?= null,
    var termsConditionsAcceptance :String ?= null

)
