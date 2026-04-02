package edu.nd.pmcburne.hello

data class Location(
    val id: Int,
    val name: String,
    val tags: List<String>,
    val description: String,
    val latitude: Double,
    val longitude: Double
)
