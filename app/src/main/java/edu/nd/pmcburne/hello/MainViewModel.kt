package edu.nd.pmcburne.hello

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class MainViewModel(private val dao: LocationDao) : ViewModel() {

    // ------------------- Tag State -------------------
    private val _selectedTag = mutableStateOf("core")
    val selectedTag: String get() = _selectedTag.value

    fun setSelectedTag(tag: String) {
        _selectedTag.value = tag
    }

    // ------------------- Locations -------------------
    private val _locations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val locations: StateFlow<List<LocationEntity>> = _locations

    private val api = RetrofitInstance.api

    init {
        viewModelScope.launch {
            syncData()
            _locations.value = dao.getAll()
        }
    }

    private suspend fun syncData() {
        val existing = dao.getAll()
        if (existing.isEmpty()) {
            val apiData = api.getLocations()
            val entities = apiData.map {
                LocationEntity(
                    it.id,
                    it.name,
                    it.description,
                    it.tag_list.joinToString(","),
                    it.visual_center.latitude,
                    it.visual_center.longitude
                )
            }
            dao.insertAll(entities)
        }
    }
}