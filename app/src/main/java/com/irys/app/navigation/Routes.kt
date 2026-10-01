package com.irys.app.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val HOME = "home"
    const val CHATS = "chats"
    const val CHAT = "chat/{conversationId}"
    const val NEARBY = "nearby"
    const val EMERGENCY = "emergency"
    const val EMERGENCY_DETAIL = "emergency/{alertId}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    fun chat(conversationId: String): String = "chat/$conversationId"
    fun emergencyDetail(alertId: String): String = "emergency/$alertId"
}
