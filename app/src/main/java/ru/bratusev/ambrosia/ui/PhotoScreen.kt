package ru.bratusev.ambrosia.ui

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import ru.bratusev.ambrosia.R
import ru.bratusev.ambrosia.data.model.SendModel
import ru.bratusev.ambrosia.data.remote.RetrofitInstance
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoScreen() {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var convertedImage by remember { mutableStateOf("") }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setImageCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri?.let { uri ->
                convertedImage = convertImageToBase64(context, uri)
                showDialog = true
            }
        }
    }

    if (showDialog) {
        MarkDescriptionDialog(
            onDismiss = { showDialog = false },
            requestType = 1,
            image = convertedImage
        )
    }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    Box(
        Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                painter = painterResource(R.drawable.ic_photo_img),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        controller = cameraController
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
            )

            Button(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        val photoFile = createImageFile(context)
                        val photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            photoFile
                        )
                        capturedImageUri = photoUri
                        takePictureLauncher.launch(photoUri)
                    } else cameraPermissionState.launchPermissionRequest()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green
                )
            ) {
                Text(
                    if (capturedImageUri == null) "Сделать фото" else "Сделать новое фото",
                    modifier = Modifier
                        .padding(vertical = 5.dp, horizontal = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir("Pictures")
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

private fun convertImageToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes() ?: ByteArray(0)
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

@Composable
fun MarkDescriptionDialog(
    onDismiss: () -> Unit,
    requestType: Int,
    image: String
) {
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val context = LocalContext.current
    val uploadScope = rememberCoroutineScope()

    LocationApp(context, LocationServices.getFusedLocationProviderClient(context)) {
        location = it
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Опишите метку",
                    fontSize = 19.sp,
                    color = Color(0xFF1F1F1F),
                    fontFamily = FontFamily.Default,
                    modifier = Modifier.padding(start = 30.dp, top = 25.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 18.dp),
                    placeholder = {
                        Text(
                            text = "Введите описание",
                            color = Color(0xFF989898)
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF989898),
                        unfocusedBorderColor = Color(0xFF989898),
                        textColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                )

                Button(
                    onClick = {
                        val info = description.ifEmpty { "Описание отсутствует" }
                        if (image.isEmpty()) {
                            onDismiss()
                            return@Button
                        }

                        uploadScope.launch {
                            try {
                                SendModel(
                                    "Без имени",
                                    info,
                                    "${location.latitude}, ${location.longitude}",
                                    requestType,
                                    image
                                ).let {
                                    RetrofitInstance.api.uploadMarker(it)
                                }
                            } catch (e: RuntimeException) {
                                val message = e.message.toString()
                                if (!context.isNetworkConnected()) {
                                    Toast.makeText(
                                        context,
                                        "Проверьте интернет соединение",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    /*Toast.makeText(
                                        context,
                                        "Ошибка отправки $message",
                                        Toast.LENGTH_SHORT
                                    ).show()*/
                                }
                            }
                        }

                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        backgroundColor = Color.Green
                    )
                ) {
                    Text("Отправить", fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationApp(
    context: Context,
    locationClient: FusedLocationProviderClient,
    onLocationChange: (LatLng) -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val locationUtils = remember { LocationUtils(context, locationClient) }
    val coarseLocationPermission =
        rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION)
    val fineLocationPermission =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(key1 = Unit) {
        val permissions = listOf(coarseLocationPermission.status, fineLocationPermission.status)

        if (permissions.all { it is PermissionStatus.Granted }) {
            locationUtils.getLastLocation(
                onSuccess = { loc ->
                    onLocationChange(LatLng(loc.latitude, loc.longitude))
                },
                onFailure = { exception ->
                    errorMessage = exception.message
                }
            )
        } else {
            coarseLocationPermission.launchPermissionRequest()
            fineLocationPermission.launchPermissionRequest()
        }
    }
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return when {
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}

@Preview(showSystemUi = true)
@Composable
fun PhotoScreenPreview(modifier: Modifier = Modifier) {
    PhotoScreen()
}
