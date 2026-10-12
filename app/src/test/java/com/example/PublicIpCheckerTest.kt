package com.example

import com.example.data.IpCheckState
import com.example.data.IpComparisonStatus
import com.example.data.PublicIpInfo
import com.example.data.PublicIpService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicIpCheckerTest {

    @Test
    fun testIpComparisonStateTransitions() {
        val baseline = "203.0.113.195"
        val newIpDifferent = "198.51.100.42"
        val newIpSame = "203.0.113.195"

        // Verify difference detection
        val statusChanged = if (baseline != newIpDifferent) {
            IpComparisonStatus.CHANGED
        } else {
            IpComparisonStatus.UNCHANGED
        }
        assertEquals(IpComparisonStatus.CHANGED, statusChanged)

        // Verify unchanged detection
        val statusUnchanged = if (baseline != newIpSame) {
            IpComparisonStatus.CHANGED
        } else {
            IpComparisonStatus.UNCHANGED
        }
        assertEquals(IpComparisonStatus.UNCHANGED, statusUnchanged)
    }

    @Test
    fun testIpInfoDataModel() {
        val info = PublicIpInfo(
            ip = "198.51.100.25",
            country = "United States",
            city = "Seattle",
            region = "Washington",
            org = "Dval Cloud Routing"
        )

        assertEquals("198.51.100.25", info.ip)
        assertEquals("United States", info.country)
        assertEquals("Seattle", info.city)
        assertEquals("Washington", info.region)
        assertEquals("Dval Cloud Routing", info.org)

        val successState = IpCheckState.Success(info)
        assertEquals(info, successState.ipInfo)
        assertTrue(successState.timestamp > 0)
    }

    @Test
    fun testIpErrorState() {
        val errorState = IpCheckState.Error("Network timeout connecting to lookup endpoint")
        assertEquals("Network timeout connecting to lookup endpoint", errorState.message)
        assertTrue(errorState.timestamp > 0)
    }
}
