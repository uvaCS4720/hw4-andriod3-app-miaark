// SOURCE 1 USED: ChatGPT
// Usage: Clickable markers with text (createSimpleMarker()), dropdown menu item formatting, viewmodel interactions (how to subscribe to values),
// setting up Google Maps composable and how to use, DB usage setup, UI neatening

package edu.nd.pmcburne.hello

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.maps.model.BitmapDescriptor


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
                Scaffold(modifier = Modifier.fillMaxSize().padding(16.dp)) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                                text = "UVA Places \uD83D\uDCCD",
                                color = Color(0xffE57200),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center) {
                            Text(
                                text = "Explore UVA's placemarks by selecting a place type from the dropdown below",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        MainScreen(vm, modifier = Modifier.fillMaxSize())
                    }
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
    val selectedTag by remember { derivedStateOf { vm.selectedTag } }
    val tags = extractTags(locations)

    Column(modifier = modifier.fillMaxSize()) {
        TagDropdown(tags, selectedTag) { vm.setSelectedTag(it) }
        MapView(locations, selectedTag, vm)
    }
}

@Composable
fun TagDropdown(tags: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0) }

    val selectedDisplay = selected.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    Box(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = selectedDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select a Place Type to View") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown arrow",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .onGloballyPositioned { coordinates ->
                    textFieldWidth = coordinates.size.width
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldWidth.toDp() })
                .heightIn(max=400.dp)
                .background(Color.White)
                .padding(top = 10.dp),
            shadowElevation = 8.dp
        ) {
            tags.forEach { tag ->
                DropdownMenuItem(
                    text = {
                        Text(
                            tag.replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { w ->
                                    w.replaceFirstChar { c -> c.uppercase() }
                                }
                        )
                    },
                    onClick = {
                        onSelect(tag)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun MapView(locations: List<LocationEntity>, selectedTag: String, vm: MainViewModel) {
    val originalLatLng = LatLng(38.0336, -78.5080)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(originalLatLng, 17f)
    }

    val filtered = locations.filter { it.tags.split(",").contains(selectedTag) }
    val selectedLocation = vm.selectedLocation

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, color = Color(0xFF232D4B), shape = RoundedCornerShape(8.dp)),
            cameraPositionState = cameraPositionState
        ) {
            filtered.forEach { loc ->
                Marker(
                    state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                    title = loc.name,
                    onClick = {
                        vm.selectLocation(loc)
                        true
                    },
                    icon = if (selectedLocation?.id == loc.id) {
                        createSimpleMarker(loc.name, loc.description, Color(0xFFffb369))
                    } else {
                        createSimpleMarker(loc.name, loc.description, Color(0xFFF7922D))
                    }
                )
            }
        }

        Button(
            onClick = {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(originalLatLng, 15f)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Reset Position")
        }

        selectedLocation?.let { loc ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        onClick = { vm.selectLocation(null) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .widthIn(max = 300.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(loc.name, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(loc.description)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = { vm.selectLocation(null) }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

fun createSimpleMarker(title: String, description: String,  backgroundColor: Color): BitmapDescriptor {
    val textWidthApprox = 18 * maxOf(title.length, 20) // make width big enough for title or snippet
    val width = textWidthApprox + 50
    val height = 135
    val tipHeight = 30

    val bitmap = Bitmap.createBitmap(width, height + tipHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint().apply {
        color = backgroundColor.toArgb()
        isAntiAlias = true
    }

    val borderPaint = Paint().apply {
        color = 0xFFbd5f02.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    val path = android.graphics.Path().apply {
        moveTo(0f, 0f)
        lineTo(width.toFloat(), 0f)
        lineTo(width.toFloat(), height.toFloat())
        lineTo(width / 2f + tipHeight / 2f, height.toFloat())
        lineTo(width / 2f, height + tipHeight.toFloat())
        lineTo(width / 2f - tipHeight / 2f, height.toFloat())
        lineTo(0f, height.toFloat())
        close()
    }

    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, borderPaint)

    val titlePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    val descPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
    }

    val titleX = width / 2f
    val titleY = height / 2f - 10f
    canvas.drawText(title, titleX, titleY, titlePaint)

    val maxChars = 66
    var snippetText = description.take(maxChars)
    if (description.length > maxChars) {
        snippetText = snippetText.dropLast(3) + "..."
    }

    val lines = snippetText.chunked(33)
    var yOffset = titleY + 30f
    for (line in lines) {
        canvas.drawText(line, width / 2f, yOffset, descPaint)
        yOffset += descPaint.textSize + 4f
    }

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
fun extractTags(locations: List<LocationEntity>): List<String>{
    return locations
        .flatMap{ it.tags.split(",") }
        .map{ it.trim() }
        .distinct()
        .sorted()
}
