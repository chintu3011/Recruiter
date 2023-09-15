package com.amri.emploihunt.jobSeekerSide

interface JobListUpdateListener {
    fun updateJobList(query: String)
    fun backToSearchView()
}