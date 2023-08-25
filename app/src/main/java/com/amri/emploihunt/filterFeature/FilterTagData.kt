package com.amri.emploihunt.filterFeature

import java.io.Serializable

data class FilterTagData(
    var tagName:String ?= "",
    var attribute:Int ?= -1
)  : Serializable
