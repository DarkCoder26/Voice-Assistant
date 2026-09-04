package com.example

import com.example.ai.IntentParser
import com.example.domain.model.AssistantIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentParserTest {

    @Test
    fun testParseCallIntents() {
        val call1 = IntentParser.parse("Call Rahul")
        assertTrue(call1 is AssistantIntent.MakeCall)
        assertEquals("Rahul", (call1 as AssistantIntent.MakeCall).contactName)

        val call2 = IntentParser.parse("Rahul ko call karo")
        assertTrue(call2 is AssistantIntent.MakeCall)
        assertEquals("Rahul", (call2 as AssistantIntent.MakeCall).contactName)

        val call3 = IntentParser.parse("Call 9876543210")
        assertTrue(call3 is AssistantIntent.MakeCall)
        assertEquals("9876543210", (call3 as AssistantIntent.MakeCall).phoneNumber)
    }

    @Test
    fun testParseVolumeIntents() {
        val vol1 = IntentParser.parse("Set volume to 50 percent")
        assertTrue(vol1 is AssistantIntent.SetVolume)
        assertEquals(50, (vol1 as AssistantIntent.SetVolume).percent)

        val vol2 = IntentParser.parse("Volume badha do")
        assertTrue(vol2 is AssistantIntent.SetVolume)
        assertEquals(AssistantIntent.VolumeType.UP, (vol2 as AssistantIntent.SetVolume).type)

        val vol3 = IntentParser.parse("Mute phone")
        assertTrue(vol3 is AssistantIntent.SetVolume)
        assertEquals(AssistantIntent.VolumeType.MUTE, (vol3 as AssistantIntent.SetVolume).type)
    }

    @Test
    fun testParseWhatsAppIntents() {
        val wa1 = IntentParser.parse("Send WhatsApp message to Rahul saying I'll reach in 10 minutes")
        assertTrue(wa1 is AssistantIntent.SendWhatsApp)
        val waIntent = wa1 as AssistantIntent.SendWhatsApp
        assertEquals("Rahul", waIntent.contactName)
        assertTrue(waIntent.message.contains("reach in 10 minutes"))
    }

    @Test
    fun testParseAppLaunchIntents() {
        val app1 = IntentParser.parse("Open YouTube")
        assertTrue(app1 is AssistantIntent.LaunchApp)
        assertEquals("YouTube", (app1 as AssistantIntent.LaunchApp).appName)

        val app2 = IntentParser.parse("Camera kholo")
        assertTrue(app2 is AssistantIntent.LaunchApp)
        assertEquals("Camera", (app2 as AssistantIntent.LaunchApp).appName)
    }

    @Test
    fun testParseAccessibilityIntents() {
        val nav1 = IntentParser.parse("Go home")
        assertTrue(nav1 is AssistantIntent.AccessibilityCommand)
        assertEquals(AssistantIntent.SystemNavAction.HOME, (nav1 as AssistantIntent.AccessibilityCommand).navAction)

        val nav2 = IntentParser.parse("Lock screen")
        assertTrue(nav2 is AssistantIntent.AccessibilityCommand)
        assertEquals(AssistantIntent.SystemNavAction.LOCK_SCREEN, (nav2 as AssistantIntent.AccessibilityCommand).navAction)
    }
}
