package com.example.videocall.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.videocall.Constants
import com.example.videocall.ui.screens.call.VideoCallScreen
import com.example.videocall.ui.screens.home.HomeScreen
import com.example.videocall.ui.screens.login.LoginScreen
import com.example.videocall.ui.screens.manageRooms.ManageRoomsScreen
import com.example.videocall.ui.screens.roomInfo.RoomInfoScreen
import com.example.videocall.ui.screens.signup.SignUpScreen

@Composable
fun AppNavGraph(sharedNavViewModel: SharedNavViewModel) {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        sharedNavViewModel.navController = navController
    }
    NavHost(navController = navController, startDestination = Routes.Home) {
        composable<Routes.Home> {
            HomeScreen(
                onUserNotFound = {
                    navController.popBackStack()
                    navController.navigate(Routes.Login)
                },
                gotoRoomInfo = { roomId, roomName ->
                    navController.navigate(Routes.ShowRoomInfo(roomId, roomName))
                },
                gotoLogin = {
                    navController.popBackStack()
                    navController.navigate(Routes.Login)
                },
                gotoManageRooms = {
                    navController.navigate(Routes.ManageRooms)
                }
            )
        }
        composable<Routes.Login> {
            LoginScreen(
                onSignUp = { navController.navigate(Routes.SignUp) },
                onSuccessfulLogin = {
                    navController.popBackStack()
                    navController.navigate(Routes.Home)
                }
            )
        }
        composable<Routes.SignUp> {
            SignUpScreen(
                onSuccessfulSignUp = {
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.navigate(Routes.Home)
                }
            )
        }
        composable<Routes.ShowRoomInfo>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${Constants.ROOM_DEEP_LINK}/{roomId}"
                }
            )
        ) {
            val params: Routes.ShowRoomInfo = it.toRoute()
            RoomInfoScreen(
                params.roomId,
                params.roomName,
                gotoVideoCall = { roomId, roomName ->
                    navController.navigate(Routes.VideoCallHome(roomId, roomName))
                },
                goBack = { navController.popBackStack() }
            )
        }
        composable<Routes.VideoCallHome>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${Constants.CALL_DEEP_LINK}/{roomId}/{roomName}"
                    action = Intent.ACTION_VIEW
                }
            )
        ) {
            val roomId = it.toRoute<Routes.VideoCallHome>().roomId
            val roomName = it.toRoute<Routes.VideoCallHome>().roomName
            VideoCallScreen(roomId, roomName, navController::popBackStack)
        }
        composable<Routes.ManageRooms> {
            ManageRoomsScreen(
                gotoRoomInfo = { roomId, roomName ->
                    navController.navigate(Routes.ShowRoomInfo(roomId, roomName))
                },
                goBack = { navController.popBackStack() }
            )
        }
    }
}