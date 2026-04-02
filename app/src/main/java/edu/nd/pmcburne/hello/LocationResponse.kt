package edu.nd.pmcburne.hello

data class LocationResponse(
    val id: Int,
    val name: String,
    val description: String,
    val tag_list: List<String>,
    val visual_center: VisualCenter
)

data class VisualCenter(
    val latitude: Double,
    val longitude: Double
)

fun LocationResponse.toLocation(): Location{
    return Location(
        id = id,
        name = name,
        description = description,
        tags = tag_list,
        latitude = visual_center.latitude,
        longitude = visual_center.longitude
    )
}
