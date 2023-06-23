package com.example.recruiter

import java.io.Serializable

data class MessageData(
    var msgId:String ?= null,
    var message:String ?= null,
    var toId:String ?= null,
    var fromId:String ?= null,
    val dateStamp:String ?= null,
    var timeStamp:String ?= null
):Serializable

