package com.amri.emploihunt.model

import java.io.Serializable

data class MessageData(
    var msgId:String ?= null,
    var msgType:Int ?= null,
    var docUri:String ?= null,
    var message:String ?= null,
    var toId:String ?= null,
    var fromId:String ?= null,
    val dateStamp:String ?= null,
    var timeStamp:String ?= null
): Serializable