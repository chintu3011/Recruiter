package com.amri.emploihunt.model

data class PrivacyPolicyModel(
    val data: Policy,
    val message: String,
    val status: Int
)

data class Policy(
    val id: Int,
    val title: String,
    val content: String

)
