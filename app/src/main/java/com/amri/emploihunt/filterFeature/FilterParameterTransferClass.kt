package com.amri.emploihunt.filterFeature

class FilterParameterTransferClass {

    /*For Job Filter for job seekers*/
    interface FilterJobListListener {
        fun onDataReceivedFilterJobList(
            domainList: MutableList<String>,
            locationList: MutableList<String>,
            workingModeList: MutableList<String>,
            packageList: MutableList<String>
        )
    }

    private var jobListener: FilterJobListListener? = null

    fun setJobListener(listener: FilterJobListListener){
        this.jobListener = listener
    }

    fun setJobData(domainList: MutableList<String>, locationList: MutableList<String>, workingModeList: MutableList<String>, packageList: MutableList<String>){
        if(jobListener != null){
            jobListener!!.onDataReceivedFilterJobList(domainList,locationList,workingModeList,packageList)
        }
    }


    /*For Application Filter for recruiters*/
    interface FilterApplicationListener{
        fun onDataReceivedFilterApplicationList(
            domainList: MutableList<String>,
            locationList: MutableList<String>,
            workingModeList: MutableList<String>,
            packageList: MutableList<String>
        )
    }

    private var applicationListener: FilterApplicationListener? = null

    fun setApplicationListener(listener: FilterApplicationListener){
        this.applicationListener = listener
    }

    fun setApplicationData(domainList: MutableList<String>, locationList: MutableList<String>, workingModeList: MutableList<String>, packageList: MutableList<String>){
        if(applicationListener != null){
            applicationListener!!.onDataReceivedFilterApplicationList(domainList,locationList,workingModeList,packageList)
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