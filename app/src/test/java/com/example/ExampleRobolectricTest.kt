package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.AlarmScheduler
import com.example.missions.MissionGenerators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WakeMaster", appName)
    }

    @Test
    fun `test math mission generator`() {
        val mathMission = MissionGenerators.generateMath("EASY")
        assertNotNull(mathMission.question)
        assertTrue(mathMission.options.contains(mathMission.correctAnswer))
        assertEquals(4, mathMission.options.size)
    }

    @Test
    fun `test alarm scheduler calculation`() {
        val nextMillis = AlarmScheduler.calculateNextTriggerMillis(7, 30, emptyList())
        assertTrue(nextMillis > System.currentTimeMillis())
    }
}
