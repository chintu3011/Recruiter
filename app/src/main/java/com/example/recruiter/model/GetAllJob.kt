package com.example.recruiter.model

import com.example.recruiter.Jobs

data class GetAllJob(
    val data: List<Jobs>,
    val message: String,
    val status: Int,
    val current_page: Int,
    val limit: Int,
    val total_records: Int,
    val total_pages: Int
)
