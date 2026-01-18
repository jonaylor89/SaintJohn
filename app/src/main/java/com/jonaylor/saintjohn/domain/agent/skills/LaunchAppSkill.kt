package com.jonaylor.saintjohn.domain.agent.skills

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import com.jonaylor.saintjohn.domain.repository.AppLauncher
import javax.inject.Inject

class LaunchAppSkill @Inject constructor(
    private val appLauncher: AppLauncher
) : AgentSkill(
    name = "launch_app",
    description = "Launches an application on the device given its name or package name. Try name first.",
    params = listOf(
        SkillParameter(
            name = "app_name",
            type = "string",
            description = "The name of the application to launch (e.g., 'Chrome', 'Calendar')",
            required = false
        ),
        SkillParameter(
            name = "package_name",
            type = "string",
            description = "The package name of the application (e.g., 'com.android.chrome')",
            required = false
        )
    )
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val appName = args["app_name"] as? String
        val packageName = args["package_name"] as? String

        if (packageName != null) {
            val success = appLauncher.launchApp(packageName)
            return if (success) "Successfully launched app with package: $packageName" else "Failed to launch app with package: $packageName"
        }

        if (appName != null) {
            val success = appLauncher.launchAppByName(appName)
            return if (success) "Successfully launched $appName" else "Failed to find or launch app: $appName"
        }

        return "Error: specify either app_name or package_name"
    }
}
