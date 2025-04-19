package ru.bratusev.ambrosia

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import ru.bratusev.ambrosia.ui.HelpScreen
import ru.bratusev.ambrosia.ui.MapScreen
import ru.bratusev.ambrosia.ui.PhotoScreen
import ru.bratusev.ambrosia.ui.theme.AmbrosiaTheme

const val PHOTO_SCREEN = "photoScreen"
const val MAP_SCREEN = "mapScreen"
const val HELP_SCREEN = "helpScreen"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmbrosiaTheme {
                val navController = rememberNavController()
                val locationPermissionState = rememberPermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION
                )
                LaunchedEffect(Unit) {
                    locationPermissionState.launchPermissionRequest()
                }
                Scaffold(
                    bottomBar = {
                        BottomBar(navController)
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = "mapScreen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("photoScreen") { PhotoScreen() }
                        composable("mapScreen") { MapScreen() }
                        composable("helpScreen") { HelpScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Gray
    ) {
        val currentRoute = navController.currentDestination?.route

        BottomNavigationItem(
            selected = currentRoute == PHOTO_SCREEN,
            onClick = {
                if (currentRoute != PHOTO_SCREEN) {
                    navController.navigate(PHOTO_SCREEN)
                }
            },
            icon = {
                val tintColor = if (currentRoute == PHOTO_SCREEN) Color.Green else Color.Gray
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                    tint = tintColor
                )
            }
        )
        BottomNavigationItem(
            selected = currentRoute == MAP_SCREEN,
            onClick = {
                if (currentRoute != MAP_SCREEN) {
                    navController.navigate(MAP_SCREEN)
                }
            },
            icon = {
                val tintColor = if (currentRoute ==MAP_SCREEN) Color.Green else Color.Gray
                Icon(
                    painter = painterResource(id = R.drawable.ic_map),
                    contentDescription = null,
                    tint = tintColor
                )
            },
        )
        BottomNavigationItem(
            selected = currentRoute == HELP_SCREEN,
            onClick = {
                if (currentRoute != HELP_SCREEN) {
                    navController.navigate(HELP_SCREEN)
                }
            },
            icon = {
                val tintColor = if (currentRoute == HELP_SCREEN) Color.Green else Color.Gray
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = null,
                    tint = tintColor
                )
            }
        )
    }
}
