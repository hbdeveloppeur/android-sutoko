package com.purpletear.framework.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import javax.inject.Inject

/**
 * Opens a URL using Discord app if installed; otherwise, defaults to a web browser.
 *
 * @param context The context from which the function is called.
 * @param url The URL to be opened.
 */

private fun openBrowser(context: Context, url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // Add the new task flag
    }
    if (browserIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(browserIntent)
    }
}

class OpenDiscordOrBrowserService @Inject constructor(private val context: Context) {

    operator fun invoke(url: String) {
        val discordIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.discord")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val packageManager = context.packageManager
        val discordAppInstalled = try {
            packageManager.getPackageInfo("com.discord", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        if (discordAppInstalled) {
            if (discordIntent.resolveActivity(packageManager) != null) {
                context.startActivity(discordIntent)
            } else {
                openBrowser(context, url)
            }
        } else {
            openBrowser(context, url)
        }
    }
}