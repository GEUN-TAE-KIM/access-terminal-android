package com.gtkim.mobile_access_control.logger

import timber.log.Timber

class DebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String =
        "[Access] ${super.createStackElementTag(element)}"
}
