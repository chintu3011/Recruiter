package com.example.recruiter

import android.os.Parcel
import android.os.Parcelable

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
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Jobs> {
        override fun createFromParcel(parcel: Parcel): Jobs {
            return Jobs(parcel)
        }

        override fun newArray(size: Int): Array<Jobs?> {
            return arrayOfNulls(size)
        }
    }
}

