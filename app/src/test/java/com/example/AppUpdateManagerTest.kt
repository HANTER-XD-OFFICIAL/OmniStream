package com.example

import com.example.data.api.AppUpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun testVersionComparison_newerMajor_returnsTrue() {
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0-beta", "v3.0.0"))
    }

    @Test
    fun testVersionComparison_newerMinor_returnsTrue() {
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0-beta", "v2.1.0"))
    }

    @Test
    fun testVersionComparison_newerPatch_returnsTrue() {
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0-beta", "v2.0.1"))
    }

    @Test
    fun testVersionComparison_stableOverBeta_returnsTrue() {
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0-beta", "v2.0.0"))
    }

    @Test
    fun testVersionComparison_olderVersion_returnsFalse() {
        assertFalse(AppUpdateManager.isNewerVersion("2.0.0-beta", "v1.0.0.OmniStreamPro"))
    }

    @Test
    fun testVersionComparison_sameVersion_returnsFalse() {
        assertFalse(AppUpdateManager.isNewerVersion("2.0.0-beta", "v2.0.0-beta"))
        assertFalse(AppUpdateManager.isNewerVersion("2.0.0", "v2.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("3.0.0", "v3.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("3.0.0", "v2.0.0-beta"))
        assertTrue(AppUpdateManager.isNewerVersion("3.0.0", "v3.0.1"))
        assertFalse(AppUpdateManager.isNewerVersion("3.0.0-Beta", "v3.0.0-Beta"))
        assertFalse(AppUpdateManager.isNewerVersion("3.0.0-Beta", "v2.0.0-beta"))
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0-beta", "v3.0.0-Beta"))
        assertTrue(AppUpdateManager.isNewerVersion("3.0.0-Beta", "v3.0.0"))
    }
}
