package com.example.data

data class PublicIpInfo(
    val ip: String,
    val country: String? = null,
    val city: String? = null,
    val region: String? = null,
    val org: String? = null
)

sealed interface IpCheckState {
    data object Idle : IpCheckState
    data object Loading : IpCheckState
    data class Success(
        val ipInfo: PublicIpInfo,
        val timestamp: Long = System.currentTimeMillis()
    ) : IpCheckState
    data class Error(
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : IpCheckState
}

enum class IpComparisonStatus {
    NONE,
    CHANGED,
    UNCHANGED,
    VERIFICATION_FAILED
}
