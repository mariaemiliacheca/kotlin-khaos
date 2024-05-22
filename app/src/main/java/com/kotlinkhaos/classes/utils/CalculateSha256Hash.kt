package com.kotlinkhaos.classes.utils

import java.io.InputStream
import java.security.MessageDigest

fun calculateSha256Hash(inputStream: InputStream): String {
    return inputStream.use { stream ->
        val buffer = ByteArray(1024)
        val digest = MessageDigest.getInstance("SHA-256")
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        bytesToHex(digest.digest())
    }
}

private fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuilder(2 * hash.size)
    for (byte in hash) {
        val hex = Integer.toHexString(0xff and byte.toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}
