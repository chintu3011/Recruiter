package com.example.recruiter.model


data class GetAllJobSeeker(
    val data: List<User>,
    val message: String,
    val status: Int,
    val current_page: Int,
    val limit: Int,
    val total_records: Int,
    val total_pages: Int
)
