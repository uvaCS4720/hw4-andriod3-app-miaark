package edu.nd.pmcburne.hello

import androidx.room.PrimaryKey
import androidx.room.Entity

data class LocationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val tags: String,
    val latitude: Double,
    val longitude: Double
)
