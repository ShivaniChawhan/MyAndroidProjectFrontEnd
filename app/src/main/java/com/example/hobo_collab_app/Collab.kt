package com.example.hobo_collab_app

data class Collab (
    val id: String?,
    val title: String,
    val description: String,
    val requiredFollowers: String?,
    val platforms: List<String>
)