package com.amri.emploihunt.settings

class UpdateSeverHelperClass {

    interface UpdateProfileDataListener{
        fun updateData()

    }

    private var updateProfileDataListener:UpdateProfileDataListener? = null

    fun setListener(listener: UpdateProfileDataListener){
        this.updateProfileDataListener = listener
    }
    fun updateData(){
        if(updateProfileDataListener != null){
            updateProfileDataListener!!.updateData()
        }
    }

    companion object{
        private var mInstance: UpdateSeverHelperClass?= null
        val instance: UpdateSeverHelperClass?
            get() {
                if(mInstance == null){
                    mInstance = UpdateSeverHelperClass()
                }
                return mInstance
            }

    }
}