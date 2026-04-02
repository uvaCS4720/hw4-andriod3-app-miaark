package edu.nd.pmcburne.hello

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.android.gms.maps.model.LatLng
import edu.nd.pmcburne.hello.ui.theme.MyApplicationTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "locations_db"
        ).build()

        setContent {
            val dao = db.locationDao()
            val vm: MainViewModel = viewModel(
                factory = MainViewModelFactory(dao)
            )
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(vm, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val locations by vm.locations.collectAsState()
    var selectedTag by remember { mutableStateOf("core") }
    val tags = extractTags(locations)

    Column(modifier = modifier) {
        TagDropdown(tags, selectedTag){
            selectedTag = it
        }

        MapView(locations, selectedTag)
    }
}

@Composable
fun TagDropdown(tags: List<String>, selected: String, onSelect: (String) -> Unit){
    var expanded by remember { mutableStateOf(false) }

    Box{
        Text(selected, modifier = Modifier.clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false})  {
            tags.forEach{
                tag -> DropdownMenuItem(
                    text = { Text(tag) },
                    onClick = {
                        onSelect(tag)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MapView(locations: List<LocationEntity>, selectedTag: String){
    val filtered = locations.filter{
        it.tags.split(",").contains(selectedTag)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(38.03567, -78.50365),
                15f
            )
        }
    ){
        filtered.forEach{ loc ->
            Marker(
                state = MarkerState(
                    position = LatLng(loc.latitude, loc.longitude)
                ),
                title = loc.name,
                snippet = loc.description
            )
        }
    }
}

suspend fun syncData(api: ApiService, dao: LocationDao){
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

fun extractTags(locations: List<LocationEntity>): List<String>{
    return locations
        .flatMap{ it.tags.split(",") }
        .map{ it.trim() }
        .distinct()
        .sorted()
}
