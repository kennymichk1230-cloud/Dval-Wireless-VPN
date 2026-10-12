package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PublicIpService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    private val tag = "PublicIpService"

    /**
     * Queries multiple reliable IP lookup endpoints with fallback to ensure
     * real, high-availability public IP and geo location retrieval.
     */
    suspend fun fetchPublicIp(): Result<PublicIpInfo> = withContext(Dispatchers.IO) {
        val providers = listOf(
            ::fetchFromIpApi,
            ::fetchFromIpInfo,
            ::fetchFromIpify
        )

        var lastException: Throwable? = null

        for (provider in providers) {
            try {
                val result = provider()
                if (result.isSuccess) {
                    val info = result.getOrThrow()
                    if (isValidIp(info.ip)) {
                        return@withContext Result.success(info)
                    }
                } else {
                    lastException = result.exceptionOrNull()
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(tag, "Provider failed, trying next fallback: ${e.message}")
            }
        }

        Result.failure(lastException ?: Exception("All public IP verification services unreachable"))
    }

    private fun fetchFromIpApi(): Result<PublicIpInfo> {
        val request = Request.Builder()
            .url("https://ipapi.co/json/")
            .header("User-Agent", "Dval-Wireless-VPN/1.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("ipapi.co returned HTTP ${response.code}"))
            }
            val body = response.body?.string() ?: return Result.failure(Exception("Empty body from ipapi.co"))
            val json = JSONObject(body)
            if (json.optBoolean("error", false)) {
                return Result.failure(Exception(json.optString("reason", "ipapi error")))
            }

            val ip = json.optString("ip").trim()
            val country = json.optString("country_name").takeIf { it.isNotBlank() }
            val city = json.optString("city").takeIf { it.isNotBlank() }
            val region = json.optString("region").takeIf { it.isNotBlank() }
            val org = json.optString("org").takeIf { it.isNotBlank() }

            return Result.success(PublicIpInfo(ip, country, city, region, org))
        }
    }

    private fun fetchFromIpInfo(): Result<PublicIpInfo> {
        val request = Request.Builder()
            .url("https://ipinfo.io/json")
            .header("User-Agent", "Dval-Wireless-VPN/1.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("ipinfo.io returned HTTP ${response.code}"))
            }
            val body = response.body?.string() ?: return Result.failure(Exception("Empty body from ipinfo.io"))
            val json = JSONObject(body)

            val ip = json.optString("ip").trim()
            val country = json.optString("country").takeIf { it.isNotBlank() }
            val city = json.optString("city").takeIf { it.isNotBlank() }
            val region = json.optString("region").takeIf { it.isNotBlank() }
            val org = json.optString("org").takeIf { it.isNotBlank() }

            return Result.success(PublicIpInfo(ip, country, city, region, org))
        }
    }

    private fun fetchFromIpify(): Result<PublicIpInfo> {
        val request = Request.Builder()
            .url("https://api.ipify.org?format=json")
            .header("User-Agent", "Dval-Wireless-VPN/1.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("ipify.org returned HTTP ${response.code}"))
            }
            val body = response.body?.string() ?: return Result.failure(Exception("Empty body from ipify.org"))
            val json = JSONObject(body)
            val ip = json.optString("ip").trim()

            return Result.success(PublicIpInfo(ip = ip))
        }
    }

    private fun isValidIp(ip: String): Boolean {
        if (ip.isBlank()) return false
        // Basic IPv4 / IPv6 validation
        val isIpv4 = ip.split(".").size == 4
        val isIpv6 = ip.contains(":")
        return isIpv4 || isIpv6
    }
}
