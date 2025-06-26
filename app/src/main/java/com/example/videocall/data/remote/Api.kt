package com.example.videocall.data.remote

import com.example.videocall.data.remote.dto.CreateRoomRequest
import com.example.videocall.data.remote.dto.CreateRoomResponse
import com.example.videocall.data.remote.dto.DeleteRoomRequest
import com.example.videocall.data.remote.dto.DeleteRoomResponse
import com.example.videocall.data.remote.dto.GetRoomResponse
import com.example.videocall.data.remote.dto.GetUserRoomsResponse
import com.example.videocall.data.remote.dto.LoginRequest
import com.example.videocall.data.remote.dto.RegisterRequest
import com.example.videocall.data.remote.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<TokenResponse>

    @POST("room/createRoom")
    suspend fun createRoom(
        @Header("Authorization") auth: String,
        @Body request: CreateRoomRequest,
    ): Response<CreateRoomResponse>

    @GET("room/getRoom")
    suspend fun getRoom(@Query("roomId") roomId: String): Response<GetRoomResponse>

    @GET("room/getUserRooms")
    suspend fun getUserRooms(@Header("Authorization") auth: String): Response<GetUserRoomsResponse>

    @POST("room/deleteRoom")
    suspend fun deleteRoom(
        @Header("Authorization") auth: String,
        @Body request: DeleteRoomRequest,
    ): Response<DeleteRoomResponse>
}