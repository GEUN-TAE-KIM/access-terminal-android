package com.gtkim.mobile_access_control.logger

import android.util.Log
import timber.log.Timber

/**
 * 릴리스 빌드용 Timber Tree.
 * WARN 이상만 출력하고 Crashlytics 등 원격 sink 와 연결할 수 있다.
 */
class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Log.println(priority, tag, message)
    }
}
