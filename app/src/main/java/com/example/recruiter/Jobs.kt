package com.example.recruiter

import android.os.Parcel
import android.os.Parcelable

data class Jobs(
    var jobTile : String ?= null,
    var aboutPost : String ?=null,
    var companyName : String ?=null,
    var education : String ?=null,
    var email : String ?=null,
    var employeeNeed : String ?=null,
    var experienceDuration: String ?=null,
    var jobApplications : Int ?=null,
    var jobLocation : String ?=null,
    var jobRoll : String ?= null,
    var phone : Long ?=null,
    var postDuration : String ?=null,
    var salary : String?= null,
    var softSkills : String?= null,
    var technicalSkills : String?= null,
    var workingmode : String?= null,
    var companyLogo : String?= null,
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
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

