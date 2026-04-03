// SOURCE 1 USED: ChatGPT
// Usage: Help figuring out how to store data for config changes so rotations wouldn't lose data
// Namely figuring out how to store selections in view model
// Also help figuring out how to sync data (get new updates if there are changes, but if not don't update) & pull from API in beginning only
package edu.nd.pmcburne.hello

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val dao: LocationDao) : ViewModel() {

    private val _selectedTag = mutableStateOf("core")
    val selectedTag: String get() = _selectedTag.value

    fun setSelectedTag(tag: String) {
        _selectedTag.value = tag
    }

    private val _locations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val locations: StateFlow<List<LocationEntity>> = _locations

    private val _selectedLocation = mutableStateOf<LocationEntity?>(null)
    val selectedLocation: LocationEntity? get() = _selectedLocation.value
    fun selectLocation(loc: LocationEntity?) {
        _selectedLocation.value = loc
    }

    private val api = RetrofitInstance.api

    init {
        viewModelScope.launch {
            syncData()
            _locations.value = dao.getAll()
        }
    }

    private suspend fun syncData() {
        val existing = dao.getAll()

        val apiData = api.getLocations()
        val apiEntities = apiData.map {
            LocationEntity(
                it.id,
                it.name,
                it.description,
                it.tag_list.joinToString(","),
                it.visual_center.latitude,
                it.visual_center.longitude
            )
        }

        val toInsertOrUpdate = apiEntities.filter { apiLoc ->
            val existing = existing.find { it.id == apiLoc.id }
            existing == null || existing != apiLoc
        }

        if (toInsertOrUpdate.isNotEmpty()) {
            dao.insertAll(toInsertOrUpdate)
        }
    }
}