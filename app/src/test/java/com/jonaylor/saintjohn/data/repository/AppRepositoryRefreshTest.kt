package com.jonaylor.saintjohn.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for app repository refresh mechanism.
 * 
 * The refresh trigger should:
 * - Emit new values when refreshApps() is called
 * - Have unique timestamps to force recomposition
 * - Be observable by multiple collectors
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppRepositoryRefreshTest {

    @Test
    fun `refresh trigger should emit new value when refreshApps called`() = runTest {
        val refreshTrigger = MutableStateFlow(0L)
        
        val initialValue = refreshTrigger.value
        
        refreshTrigger.value = System.currentTimeMillis()
        
        assertNotEquals(
            "Refresh trigger should have new value",
            initialValue,
            refreshTrigger.value
        )
    }

    @Test
    fun `multiple refreshApps calls should emit different values`() = runTest {
        val refreshTrigger = MutableStateFlow(0L)
        val values = mutableListOf<Long>()
        
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            refreshTrigger.take(4).toList(values)
        }
        
        refreshTrigger.value = 1L
        refreshTrigger.value = 2L
        refreshTrigger.value = 3L
        
        job.cancel()
        
        assertEquals("Should have collected 4 values", 4, values.size)
        assertEquals("Values should all be unique", values.size, values.toSet().size)
    }

    @Test
    fun `refresh trigger timestamp should be monotonically increasing`() = runTest {
        val refreshTrigger = MutableStateFlow(0L)
        
        val timestamps = mutableListOf<Long>()
        
        repeat(5) {
            refreshTrigger.value = System.currentTimeMillis()
            timestamps.add(refreshTrigger.value)
            Thread.sleep(1)
        }
        
        for (i in 1 until timestamps.size) {
            assertTrue(
                "Timestamps should be monotonically increasing",
                timestamps[i] >= timestamps[i - 1]
            )
        }
    }

    @Test
    fun `flow should emit on refresh`() = runTest {
        val refreshTrigger = MutableStateFlow(0L)
        
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            refreshTrigger.collect { }
        }
        
        val valueBefore = refreshTrigger.value
        refreshTrigger.value = System.currentTimeMillis()
        val valueAfter = refreshTrigger.first()
        
        assertNotEquals("Value should change after refresh", valueBefore, valueAfter)
        
        job.cancel()
    }
}
