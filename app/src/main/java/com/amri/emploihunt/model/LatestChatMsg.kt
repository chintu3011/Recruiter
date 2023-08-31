package com.amri.emploihunt.model

import com.amri.emploihunt.model.MessageData
import java.io.Serializable

data class LatestChatMsg(
    val latestChatMsg: MessageData,
    val user: User
):Serializable
