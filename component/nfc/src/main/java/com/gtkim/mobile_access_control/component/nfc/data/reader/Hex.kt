package com.gtkim.mobile_access_control.component.nfc.data.reader

internal fun ByteArray.toHex(): String =
    joinToString(separator = "") { "%02X".format(it) }
