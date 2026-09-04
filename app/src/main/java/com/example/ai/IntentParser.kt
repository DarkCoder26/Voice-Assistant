package com.example.ai

import com.example.domain.model.AssistantIntent
import java.util.Locale
import java.util.regex.Pattern

object IntentParser {

    fun parse(rawInput: String): AssistantIntent {
        val input = rawInput.trim()
        val lower = input.lowercase(Locale.ROOT)

        if (lower.isBlank()) {
            return AssistantIntent.Unknown("")
        }

        // --- 1. CALL COMMANDS ---
        // English: "call rahul", "make a call to dad", "call 9876543210", "dial 123"
        // Hindi/Hinglish: "rahul ko call karo", "mummy ko phone lagao", "papa ko call lagao", "call kar do aman ko"
        parseCallIntent(lower, input)?.let { return it }

        // --- 2. WHATSAPP COMMANDS ---
        // "rahul ko whatsapp message bhejo: main 10 min me aa rha hu"
        // "send whatsapp message to rahul saying I will be late"
        // "whatsapp par rahul ko message bhejo"
        // "whatsapp kholo"
        parseWhatsAppIntent(lower, input)?.let { return it }

        // --- 3. SMS COMMANDS ---
        // "send sms to rahul", "rahul ko sms karo", "mummy ko message karo ki main ghar aa raha hoon"
        parseSmsIntent(lower, input)?.let { return it }

        // --- 4. VOLUME COMMANDS ---
        // "increase volume", "volume badha do", "volume 50 percent kar do", "mute phone", "phone silent kar do"
        parseVolumeIntent(lower)?.let { return it }

        // --- 5. MEDIA COMMANDS ---
        // "play music", "pause", "resume", "next song", "previous song", "play arijit singh on youtube"
        parseMediaIntent(lower, input)?.let { return it }

        // --- 6. APP LAUNCH COMMANDS ---
        // "open youtube", "youtube kholo", "open chrome", "camera kholo", "open settings"
        parseAppLaunchIntent(lower)?.let { return it }

        // --- 7. ACCESSIBILITY / SYSTEM NAV COMMANDS ---
        // "go home", "home jao", "back jao", "open recents", "recents kholo", "notifications panel kholo", "lock device"
        parseAccessibilityIntent(lower)?.let { return it }

        // --- 8. NOTIFICATION READ COMMANDS ---
        // "read my notifications", "notifications padho", "what are my notifications"
        if (lower.contains("read") && lower.contains("notification") ||
            lower.contains("notification padho") ||
            lower.contains("notifications batao") ||
            lower.contains("check notifications")
        ) {
            return AssistantIntent.QueryNotifications()
        }

        // --- 9. CLOCK / ALARM / TIMER ---
        // "set a timer for 10 minutes", "10 minute ka timer lagao", "set alarm for 7 am", "kal subah 8 baje mujhe yaad dilana"
        parseClockIntent(lower)?.let { return it }

        // --- 10. SYSTEM SETTINGS ---
        // "bluetooth on karo", "open wifi settings", "bluetooth settings kholo"
        parseSettingsIntent(lower)?.let { return it }

        // --- 11. DEVICE INFO (TIME, DATE, BATTERY) ---
        parseDeviceInfoIntent(lower)?.let { return it }

        // --- 12. WEB SEARCH ---
        // "search google for python", "search for android tutorials"
        val searchRegex = Regex("^(?:search\\s+(?:google\\s+)?(?:for\\s+)?|google\\s+par\\s+search\\s+karo\\s+)(.+)$", RegexOption.IGNORE_CASE)
        val searchMatch = searchRegex.find(input)
        if (searchMatch != null) {
            val query = searchMatch.groupValues[1].trim()
            return AssistantIntent.WebSearch(query)
        }

        // Fallback to Conversational AI (Gemini or local smart response)
        return AssistantIntent.ConversationalAi(input)
    }

    private fun parseCallIntent(lower: String, original: String): AssistantIntent.MakeCall? {
        // Direct number check: "call 9876543210" or "dial 9876543210"
        val numberPattern = Regex("(?:call|dial|phone lagao|call karo)\\s+([0-9+\\- ]{7,15})")
        val numMatch = numberPattern.find(lower)
        if (numMatch != null) {
            val number = numMatch.groupValues[1].replace(" ", "").trim()
            return AssistantIntent.MakeCall(phoneNumber = number)
        }

        // Hinglish: "<name> ko call karo / phone lagao / call lagao"
        val hinglishPattern = Regex("(.+?)\\s+ko\\s+(?:call\\s+karo|phone\\s+lagao|call\\s+lagao|phone\\s+karo|call\\s+kar\\s+do)")
        val hMatch = hinglishPattern.find(lower)
        if (hMatch != null) {
            val name = cleanName(hMatch.groupValues[1])
            if (name.isNotBlank()) return AssistantIntent.MakeCall(contactName = name)
        }

        // English: "call <name>", "make a call to <name>", "phone <name>"
        val engPattern = Regex("^(?:please\\s+)?(?:call|dial|phone|make a call to)\\s+(.+?)(?:\\s+please)?$")
        val eMatch = engPattern.find(lower)
        if (eMatch != null) {
            val candidate = cleanName(eMatch.groupValues[1])
            if (candidate.isNotBlank() && !candidate.startsWith("whatsapp") && !candidate.startsWith("sms")) {
                return AssistantIntent.MakeCall(contactName = candidate)
            }
        }

        return null
    }

    private fun parseWhatsAppIntent(lower: String, original: String): AssistantIntent.SendWhatsApp? {
        if (!lower.contains("whatsapp")) return null

        // Just "open whatsapp" or "whatsapp kholo" -> handled in AppLaunch, unless it contains message details
        if (lower == "whatsapp" || lower == "open whatsapp" || lower == "whatsapp kholo" || lower == "launch whatsapp") {
            return null // Let LaunchApp handle it
        }

        // "rahul ko whatsapp message bhejo: <msg>" or "whatsapp par rahul ko message bhejo: <msg>"
        // Pattern 1: Hinglish with colon or message indicator
        val hRegex = Regex("(?:whatsapp\\s+par\\s+)?(.+?)\\s+ko\\s+whatsapp\\s+(?:pe\\s+|par\\s+)?(?:message|msg)\\s+(?:bhejo|karo)(?:\\s*[:\\-]?\\s*(?:ki|saying|that)?\\s*(.+))?")
        val hMatch = hRegex.find(lower)
        if (hMatch != null) {
            val name = cleanName(hMatch.groupValues[1])
            val msg = hMatch.groupValues.getOrNull(2)?.trim() ?: ""
            return AssistantIntent.SendWhatsApp(contactName = name, message = msg)
        }

        // Pattern 2: "send whatsapp message to <name> saying/that <msg>"
        val eRegex = Regex("(?:send\\s+)?whatsapp\\s+(?:message\\s+)?(?:to\\s+)?(.+?)(?:\\s+(?:saying|that|with text|message)[:\\s]+(.+)|$)")
        val eMatch = eRegex.find(lower)
        if (eMatch != null) {
            val name = cleanName(eMatch.groupValues[1])
            val msg = eMatch.groupValues.getOrNull(2)?.trim() ?: ""
            return AssistantIntent.SendWhatsApp(contactName = name, message = msg)
        }

        // Pattern 3: "whatsapp par <name> ko message karo"
        val hRegex2 = Regex("whatsapp\\s+(?:par|pe)\\s+(.+?)\\s+ko\\s+(?:message|msg)\\s+(?:karo|bhejo)(?:\\s*[:\\-]?\\s*(.+))?")
        val hMatch2 = hRegex2.find(lower)
        if (hMatch2 != null) {
            val name = cleanName(hMatch2.groupValues[1])
            val msg = hMatch2.groupValues.getOrNull(2)?.trim() ?: ""
            return AssistantIntent.SendWhatsApp(contactName = name, message = msg)
        }

        return AssistantIntent.SendWhatsApp()
    }

    private fun parseSmsIntent(lower: String, original: String): AssistantIntent.SendSms? {
        if (!lower.contains("sms") && !lower.contains("text") && !lower.contains("message")) return null
        if (lower.contains("whatsapp")) return null

        // Hinglish: "mummy ko message karo ki main ghar aa raha hoon"
        // "rahul ko sms karo"
        val hRegex = Regex("(.+?)\\s+ko\\s+(?:sms|message|text)\\s+(?:karo|bhejo)(?:\\s*[:\\-]?\\s*(?:ki|saying|that)?\\s*(.+))?")
        val hMatch = hRegex.find(lower)
        if (hMatch != null) {
            val name = cleanName(hMatch.groupValues[1])
            val msg = hMatch.groupValues.getOrNull(2)?.trim() ?: ""
            return AssistantIntent.SendSms(contactName = name, message = msg)
        }

        // English: "send sms to rahul saying hello", "text dad I am on my way"
        val eRegex = Regex("(?:send\\s+(?:an?\\s+)?sms\\s+to|send\\s+text\\s+to|text)\\s+(.+?)(?:\\s+(?:saying|that|with message)[:\\s]+(.+)|$)")
        val eMatch = eRegex.find(lower)
        if (eMatch != null) {
            val name = cleanName(eMatch.groupValues[1])
            val msg = eMatch.groupValues.getOrNull(2)?.trim() ?: ""
            return AssistantIntent.SendSms(contactName = name, message = msg)
        }

        return null
    }

    private fun parseVolumeIntent(lower: String): AssistantIntent.SetVolume? {
        // Percentage check: "set volume to 50 percent", "volume 70 percent kar do", "volume 80%"
        val percentRegex = Regex("(?:volume|awaaz)\\s+(?:to\\s+)?([0-9]{1,3})\\s*(?:percent|%|pratishat)?")
        val pMatch = percentRegex.find(lower)
        if (pMatch != null) {
            val value = pMatch.groupValues[1].toIntOrNull()
            if (value != null) {
                return AssistantIntent.SetVolume(
                    type = AssistantIntent.VolumeType.SET_PERCENT,
                    percent = value.coerceIn(0, 100)
                )
            }
        }

        // Mute / Silent: "mute", "mute phone", "phone silent kar do", "silent mode on"
        if (lower.contains("mute") || lower.contains("silent kar do") || lower.contains("phone silent") || lower.contains("awaaz band")) {
            return AssistantIntent.SetVolume(type = AssistantIntent.VolumeType.MUTE)
        }

        // Unmute
        if (lower.contains("unmute") || lower.contains("silent band") || lower.contains("unsilent")) {
            return AssistantIntent.SetVolume(type = AssistantIntent.VolumeType.UNMUTE)
        }

        // Increase / Up
        if (lower.contains("increase volume") || lower.contains("volume up") || lower.contains("volume badha") ||
            lower.contains("awaaz badha") || lower.contains("badhao volume") || lower.contains("louder")
        ) {
            return AssistantIntent.SetVolume(type = AssistantIntent.VolumeType.UP)
        }

        // Decrease / Down
        if (lower.contains("decrease volume") || lower.contains("volume down") || lower.contains("volume kam") ||
            lower.contains("awaaz kam") || lower.contains("kam karo volume") || lower.contains("lower volume")
        ) {
            return AssistantIntent.SetVolume(type = AssistantIntent.VolumeType.DOWN)
        }

        return null
    }

    private fun parseMediaIntent(lower: String, original: String): AssistantIntent? {
        // YouTube search and play: "play arijit singh on youtube" or "youtube par arijit singh ke songs chalao"
        val ytPlayRegex1 = Regex("(?:play\\s+)?(.+?)\\s+on\\s+youtube")
        val ytPlayRegex2 = Regex("youtube\\s+(?:par|pe)\\s+(.+?)\\s+(?:ke\\s+songs\\s+)?(?:chalao|bajao|play karo)")
        val m1 = ytPlayRegex1.find(lower) ?: ytPlayRegex2.find(lower)
        if (m1 != null) {
            val query = m1.groupValues[1].trim()
            return AssistantIntent.MediaControl(
                action = AssistantIntent.MediaActionType.SEARCH_PLAY,
                query = query
            )
        }

        // Play query general: "play arijit singh", "play taylor swift", "play jazz music"
        val playQueryRegex = Regex("^play\\s+(.+)$")
        val playMatch = playQueryRegex.find(lower)
        if (playMatch != null) {
            val query = playMatch.groupValues[1].trim()
            if (query != "music" && query != "song" && query != "songs") {
                return AssistantIntent.MediaControl(
                    action = AssistantIntent.MediaActionType.SEARCH_PLAY,
                    query = query
                )
            }
        }

        // Standard controls
        if (lower == "play" || lower == "play music" || lower == "gaana bajao" || lower == "resume") {
            return AssistantIntent.MediaControl(action = AssistantIntent.MediaActionType.PLAY)
        }
        if (lower == "pause" || lower == "pause music" || lower == "stop music" || lower == "gaana roko" || lower == "roko") {
            return AssistantIntent.MediaControl(action = AssistantIntent.MediaActionType.PAUSE)
        }
        if (lower.contains("next song") || lower.contains("agla gaana") || lower == "next") {
            return AssistantIntent.MediaControl(action = AssistantIntent.MediaActionType.NEXT)
        }
        if (lower.contains("previous song") || lower.contains("pichhla gaana") || lower == "previous") {
            return AssistantIntent.MediaControl(action = AssistantIntent.MediaActionType.PREVIOUS)
        }

        return null
    }

    private fun parseAppLaunchIntent(lower: String): AssistantIntent.LaunchApp? {
        val appAliases = mapOf(
            "youtube" to "YouTube",
            "chrome" to "Chrome",
            "whatsapp" to "WhatsApp",
            "camera" to "Camera",
            "settings" to "Settings",
            "instagram" to "Instagram",
            "spotify" to "Spotify",
            "maps" to "Maps",
            "gmail" to "Gmail",
            "clock" to "Clock",
            "calculator" to "Calculator",
            "photos" to "Photos"
        )

        // English: "open youtube", "launch camera", "start chrome"
        val openRegex = Regex("^(?:open|launch|start)\\s+([a-zA-Z0-9 ]+)$")
        val oMatch = openRegex.find(lower)
        if (oMatch != null) {
            val appCandidate = oMatch.groupValues[1].trim()
            val resolved = appAliases[appCandidate] ?: appCandidate.replaceFirstChar { it.uppercase() }
            return AssistantIntent.LaunchApp(appName = resolved)
        }

        // Hinglish: "youtube kholo", "camera chalu karo", "settings open karo"
        val hOpenRegex = Regex("^([a-zA-Z0-9 ]+?)\\s+(?:kholo|chalu karo|open karo)$")
        val hoMatch = hOpenRegex.find(lower)
        if (hoMatch != null) {
            val appCandidate = hoMatch.groupValues[1].trim()
            val resolved = appAliases[appCandidate] ?: appCandidate.replaceFirstChar { it.uppercase() }
            return AssistantIntent.LaunchApp(appName = resolved)
        }

        return null
    }

    private fun parseAccessibilityIntent(lower: String): AssistantIntent.AccessibilityCommand? {
        if (lower.contains("go home") || lower == "home" || lower.contains("home screen") || lower.contains("home jao")) {
            return AssistantIntent.AccessibilityCommand(AssistantIntent.SystemNavAction.HOME)
        }
        if (lower.contains("go back") || lower == "back" || lower.contains("back jao") || lower.contains("peeche jao")) {
            return AssistantIntent.AccessibilityCommand(AssistantIntent.SystemNavAction.BACK)
        }
        if (lower.contains("recents") || lower.contains("recent apps") || lower.contains("recents kholo") || lower.contains("recent applications")) {
            return AssistantIntent.AccessibilityCommand(AssistantIntent.SystemNavAction.RECENTS)
        }
        if (lower.contains("notification panel") || lower.contains("notifications panel") || lower.contains("notification shade") || lower.contains("drop down notifications")) {
            return AssistantIntent.AccessibilityCommand(AssistantIntent.SystemNavAction.NOTIFICATIONS)
        }
        if (lower.contains("lock screen") || lower.contains("lock phone") || lower.contains("lock device") || lower.contains("phone lock karo")) {
            return AssistantIntent.AccessibilityCommand(AssistantIntent.SystemNavAction.LOCK_SCREEN)
        }
        return null
    }

    private fun parseClockIntent(lower: String): AssistantIntent.AlarmTimer? {
        // Timer: "set a timer for 10 minutes", "10 minute ka timer lagao", "5 min timer"
        val timerRegex = Regex("(?:timer\\s+(?:of\\s+|for\\s+)?|set\\s+(?:a\\s+)?timer\\s+(?:for\\s+)?)([0-9]+)\\s*(?:min|minute|minutes|sec|seconds)?")
        val tMatch = timerRegex.find(lower)
        if (tMatch != null) {
            val amount = tMatch.groupValues[1].toIntOrNull() ?: 5
            val isSeconds = lower.contains("sec")
            return if (isSeconds) {
                AssistantIntent.AlarmTimer(type = AssistantIntent.ClockType.TIMER, seconds = amount)
            } else {
                AssistantIntent.AlarmTimer(type = AssistantIntent.ClockType.TIMER, minutes = amount)
            }
        }

        if (lower.contains("minute ka timer") || lower.contains("min timer")) {
            val digits = Regex("([0-9]+)").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 5
            return AssistantIntent.AlarmTimer(type = AssistantIntent.ClockType.TIMER, minutes = digits)
        }

        // Alarm: "set alarm for 7 am", "alarm lagao 6 baje ka", "wake me up at 8 am", "kal subah 8 baje mujhe yaad dilana"
        val alarmRegex = Regex("(?:alarm\\s+(?:for\\s+|at\\s+)?|alarm\\s+lagao\\s+)([0-9]{1,2})(?::([0-9]{2}))?\\s*(am|pm)?")
        val aMatch = alarmRegex.find(lower)
        if (aMatch != null) {
            var hour = aMatch.groupValues[1].toIntOrNull() ?: 7
            val minute = aMatch.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            val ampm = aMatch.groupValues.getOrNull(3) ?: ""
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return AssistantIntent.AlarmTimer(type = AssistantIntent.ClockType.ALARM, hours = hour, minutes = minute)
        }

        if (lower.contains("subah") && lower.contains("baje")) {
            val hour = Regex("([0-9]{1,2})\\s*baje").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 7
            return AssistantIntent.AlarmTimer(type = AssistantIntent.ClockType.ALARM, hours = hour, minutes = 0)
        }

        return null
    }

    private fun parseSettingsIntent(lower: String): AssistantIntent.SettingsControl? {
        if (lower.contains("bluetooth")) {
            return AssistantIntent.SettingsControl(AssistantIntent.SettingCategory.BLUETOOTH)
        }
        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return AssistantIntent.SettingsControl(AssistantIntent.SettingCategory.WIFI)
        }
        if (lower.contains("sound setting") || lower.contains("audio setting")) {
            return AssistantIntent.SettingsControl(AssistantIntent.SettingCategory.SOUND)
        }
        if (lower.contains("display setting") || lower.contains("brightness")) {
            return AssistantIntent.SettingsControl(AssistantIntent.SettingCategory.DISPLAY)
        }
        return null
    }

    private fun parseDeviceInfoIntent(lower: String): AssistantIntent.GetDeviceInfo? {
        if (lower.contains("what time") || lower.contains("current time") || lower.contains("kitne baje") || lower.contains("kya time")) {
            return AssistantIntent.GetDeviceInfo(AssistantIntent.InfoCategory.TIME)
        }
        if (lower.contains("what date") || lower.contains("today's date") || lower.contains("konsi date") || lower.contains("aaj ki tarikh")) {
            return AssistantIntent.GetDeviceInfo(AssistantIntent.InfoCategory.DATE)
        }
        if (lower.contains("battery") || lower.contains("charging")) {
            return AssistantIntent.GetDeviceInfo(AssistantIntent.InfoCategory.BATTERY)
        }
        return null
    }

    private fun cleanName(raw: String): String {
        return raw.replace(Regex("(?:please|ko|call|sms|whatsapp|pe|par|saying|that|to)"), "")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}
