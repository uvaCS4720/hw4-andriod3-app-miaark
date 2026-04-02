package edu.nd.pmcburne.hello

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit

data class MainUIState(
    val counterValue: Int
)

class MainViewModel(private val dao: LocationDao) : ViewModel() {
    private val api = RetrofitInstance.api
    private val _locations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val locations: StateFlow<List<LocationEntity>> = _locations

    init{
        viewModelScope.launch{
            syncData()
            _locations.value = dao.getAll()
        }
    }

    private suspend fun syncData(){
        val existing = dao.getAll()

        if(existing.isEmpty()){
            val apiData = api.getLocations()
            val entities = apiData.map{
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