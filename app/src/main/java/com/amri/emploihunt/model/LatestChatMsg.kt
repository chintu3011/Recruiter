package com.amri.emploihunt.model

import java.io.Serializable

data class LatestChatMsg(
    val latestChatMsg: MessageData,
    val user: User
):Serializable
