package site.jarviscopilot.jarvis.util

import android.content.Context

object DeviceUtils {

    /**
     * Determines if the current device is a tablet based on the smallest screen width.
     * A device is considered a tablet if its smallest width is 600dp or greater.
     *
     * @param context The application context
     * @return true if the device is a tablet, false if it's a phone
     */
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.smallestScreenWidthDp >= 600
    }
}
