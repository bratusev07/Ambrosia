package ru.bratusev.ambrosia.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.bratusev.ambrosia.R
import ru.bratusev.ambrosia.data.model.Markers
import ru.bratusev.ambrosia.data.remote.RetrofitInstance
import ru.bratusev.ambrosia.ui.MarkerIconManager.getMarkerIcon


private val mapStyle = MapStyleOptions(
    """
        [
            {
                "featureType": "all",
                "elementType": "labels.icon",
                "stylers": [
                    {
                        "visibility": "off"
                    }
                ]
            }
        ]
        """.trimIndent()
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(1.35, 103.87), 10f)
    }
    var selectedMarker by remember { mutableStateOf<Markers?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    var markers by remember { mutableStateOf(emptyList<Markers>()) }
    LaunchedEffect(Unit) {
        while (true) {
            try {
                markers = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getAllMarks() ?: emptyList()
                }
            } catch (_: Exception) {
            }
            delay(30000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings().copy(
                compassEnabled = true,
                myLocationButtonEnabled = true,
            ),
            properties = MapProperties().copy(
                mapStyleOptions = mapStyle
            )
        ) {
            markers.forEach { marker ->
                Marker(
                    state = rememberMarkerState(
                        key = marker.id.toString(),
                        position = marker.let { LatLng(it.lat, it.lon) }
                    ),
                    onClick = {
                        selectedMarker = marker
                        true
                    },
                    icon = getMarkerIcon(LocalContext.current, R.drawable.ic_marker)
                )
            }
        }

        Image(
            modifier = Modifier
                .clickable { showDialog = true }
                .padding(end = 20.dp, top = 50.dp)
                .align(Alignment.TopEnd),
            painter = painterResource(R.drawable.ic_question),
            contentDescription = null
        )

        if (selectedMarker != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedMarker = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                content = {
                    selectedMarker?.let {
                        MarkerBottomSheet(marker = it, modifier = Modifier)
                    }
                }
            )
        }

        if (showDialog) {
            AlertDialog(
                modifier = Modifier.padding(40.dp),
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                text = { Text("Вы можете помогать администрации бороться с сорной растительностью, фотографируя кусты амброзии и создавая метки её произростания на карте города") },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}

@Composable
fun MarkerBottomSheet(
    marker: Markers,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = rememberAsyncImagePainter(
                model = marker.getImage,
                error = painterResource(R.drawable.ic_launcher_foreground)
            ),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp, 400.dp)
                .padding(start = 35.dp, end = 35.dp, top = 10.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = marker.street.toString(),
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 25.dp)
                .fillMaxWidth(),
            fontSize = 18.sp,
            color = Color(0xFF1F1F1F),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(34.dp))
    }
}


object MarkerIconManager {
    private var markerIcon: BitmapDescriptor? = null

    fun getMarkerIcon(context: Context, drawableResId: Int): BitmapDescriptor? {
        if (markerIcon == null) {
            val drawable: Drawable =
                ContextCompat.getDrawable(context, drawableResId) ?: return null
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            markerIcon = BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        return markerIcon
    }
}
