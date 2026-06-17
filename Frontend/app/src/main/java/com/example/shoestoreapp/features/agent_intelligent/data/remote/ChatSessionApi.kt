package com.example.shoestoreapp.features.agent_intelligent.data.remote

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatSessionApi {
    @POST("api/v1/session")
    suspend fun createSession(): Response<CreateSessionResponseDto>

    @GET("api/v1/session")
    suspend fun getSession(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize : Int = 10,
    ) : Response<PageResult<ChatSessionResponseDto>>

    @GET("api/v1/chat-message")
    suspend fun getChatMessages(
        @Query("publicSessionId") sessionId: String,
        @Query("cursor") cursor: String? = null
    ): Response<ChatHistoryResponseDto>

    @DELETE("api/v1/session")
    suspend fun deleteAllSessions(): Response<Unit>
}
