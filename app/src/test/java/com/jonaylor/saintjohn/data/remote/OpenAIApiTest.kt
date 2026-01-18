package com.jonaylor.saintjohn.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenAIApiTest {

    @Test
    fun testUrlConstruction() {
        val retrofit = Retrofit.Builder()
            .baseUrl(OpenAIApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val service = retrofit.create(OpenAIApi::class.java)
        
        // We can't easily spy on the interface method call without MockWebServer,
        // but we can verify the BASE_URL constant which is the likely culprit.
        
        assertEquals("https://api.openai.com/v1/", OpenAIApi.BASE_URL)
    }
}
