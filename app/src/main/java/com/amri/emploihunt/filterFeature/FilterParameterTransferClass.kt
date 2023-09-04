package com.amri.emploihunt.filterFeature

class FilterParameterTransferClass {

    /*For Job Filter for job seekers*/
    interface FilterJobListListener {
        /*fun onDataReceivedFilterJobList(
            domainList: MutableList<String>,
            locationList: MutableList<String>,
            workingModeList: MutableList<String>,
            packageList: MutableList<String>
        )*/
        fun onDataReceivedFilterJobList(
            domainList: String,
            locationList: String,
            workingModeList: String,
            packageList: String
        )
    }

    private var jobListener: FilterJobListListener? = null

    fun setJobListener(listener: FilterJobListListener){
        this.jobListener = listener
    }

    /*fun setJobData(domainList: MutableList<String>, locationList: MutableList<String>, workingModeList: MutableList<String>, packageList: MutableList<String>){
        if(jobListener != null){
            jobListener!!.onDataReceivedFilterJobList(domainList,locationList,workingModeList,packageList)
        }
    }*/
    fun setJobData(domainList: String, locationList: String, workingModeList: String, packageList: String){
        if(jobListener != null){
            jobListener!!.onDataReceivedFilterJobList(domainList,locationList,workingModeList,packageList)
        }
    }


    /*For Application Filter for recruiters*/
    interface FilterApplicationListener{
        /*fun onDataReceivedFilterApplicationList(
            domainList: MutableList<String>,
            locationList: MutableList<String>,
            workingModeList: MutableList<String>,
            packageList: MutableList<String>
        )*/
        fun onDataReceivedFilterApplicationList(
            domainList: String,
            locationList: String,
            workingModeList: String,
            packageList: String
        )
    }

    private var applicationListener: FilterApplicationListener? = null

    fun setApplicationListener(listener: FilterApplicationListener){
        this.applicationListener = listener
    }

    /*fun setApplicationData(domainList: MutableList<String>, locationList: MutableList<String>, workingModeList: MutableList<String>, packageList: MutableList<String>){
        if(applicationListener != null){
            applicationListener!!.onDataReceivedFilterApplicationList(domainList,locationList,workingModeList,packageList)
        }
    }*/
    fun setApplicationData(domain: String, location: String, workingMode: String, packageRange: String){
        if(applicationListener != null){
            applicationListener!!.onDataReceivedFilterApplicationList(domain,location,workingMode,packageRange)
        }
    }

    companion object{
        private var mInstance: FilterParameterTransferClass?= null
        val instance: FilterParameterTransferClass?
            get() {
                 if(mInstance == null){
                     mInstance = FilterParameterTransferClass()
                 }
                return mInstance
            }
        
    }
}