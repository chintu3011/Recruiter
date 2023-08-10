package com.example.recruiter

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Jobs(
    var id: Int ?=null,
    var iHrUserId: Int ?=null,
    var vJobTitle : String ?= null,
    var tDes : String ?=null,
    var vCompanyName : String ?=null,
    var vJobLevel : String ?=null,
    var vEducation : String ?=null,
    var iNumberOfVacancy : Int ?=null,
    var vExperience: String ?=null,
    var iNumberOfApplied : Int ?=null,
    var vAddress : String ?=null,
    var vJobRoleResponsbility : String ?= null,
    var vSalaryPackage : String?= null,
    var tSoftSkill : String?= null,
    var tTechnicalSkill : String?= null,
    var vWrokingMode : String?= null,
    var tCompanyLogoUrl : String?= null,
    var tCreatedAt : String?= null,
    var tUpdatedAt : String?= null,
    ) :Serializable

