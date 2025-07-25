package com.fvk_solutions.fvkstock

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.system.measureTimeMillis

fun getDeviceIpAddress(context: Context): String? {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ipAddress = wifiInfo.ipAddress
    return if (ipAddress == 0) null else
        String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
}



fun scanLocalNetwork(context: Context): List<String> {
    val ipAddress = getDeviceIpAddress(context) ?: return emptyList()
    val prefix = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1)
    val foundIpAddresses = mutableListOf<String>()
    val totalAddressesToScan = 254
    var scannedAddresses = 0
    val startTime = System.currentTimeMillis()

    for (i in 1..254) {
        val testIp = prefix + i.toString()
        try {
            val address = InetAddress.getByName(testIp)
            val reachable = address.isReachable(1) // Таймаут в миллисекундах, увеличен для надежности
            if (reachable) {
                foundIpAddresses.add(testIp)
            }
        } catch (e: Exception) {
            Log.e("NetworkScan", "Error scanning IP $testIp: ${e.message}")
        }
        scannedAddresses++
        val elapsedTime = System.currentTimeMillis() - startTime
        val estimatedTimeRemaining = if (scannedAddresses > 0) (elapsedTime.toDouble() / scannedAddresses * (totalAddressesToScan - scannedAddresses)) / 1000 else 0.0
        Log.d("NetworkScan", "Scanning IP: $testIp. Progress: $scannedAddresses/$totalAddressesToScan. Estimated time remaining: ${"%.2f".format(estimatedTimeRemaining)} seconds.")
    }
    return foundIpAddresses
}

fun scanPorts(ip: String) {
    val commonPorts = listOf(22, 80, 443, 8080) // Пример распространенных портов
    for (port in commonPorts) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 500) // Таймаут подключения
            socket.close()
            // Порт открыт, значит устройство активно
            // Можно добавить IP в список
            break
        } catch (e: Exception) {
            // Порт закрыт или хост не отвечает
        }
    }
}