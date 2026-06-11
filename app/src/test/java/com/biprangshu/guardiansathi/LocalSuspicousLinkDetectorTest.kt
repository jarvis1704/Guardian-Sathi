package com.biprangshu.guardiansathi

import com.biprangshu.guardiansathi.Global.Elder.core.LinkConfidence
import com.biprangshu.guardiansathi.Global.Elder.core.LocalSuspicousLinkDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test


//test for suspicous link detector
class LocalSuspicousLinkDetectorTest {

    private val detector = LocalSuspicousLinkDetector()

    @Test
    fun testNoLinks() {
        val result = detector.detectSuspiciousLink("This is a plain message without any links.")
        assertFalse(result.isSuspicious)
        assertEquals(null, result.matchedLink)
        assertEquals(null, result.confidence)
    }

    @Test
    fun testBenignLink() {
        val result = detector.detectSuspiciousLink("Please check the official site at https://www.google.com for info.")
        assertFalse(result.isSuspicious)
        assertEquals(null, result.matchedLink)
        assertEquals(null, result.confidence)
    }

    @Test
    fun testHighConfidenceScamLink() {
        val message = "Urgent: Claim your free gifts at http://free-rewards.com/claim-now before they expire!"
        val result = detector.detectSuspiciousLink(message)
        assertTrue(result.isSuspicious)
        assertEquals("http://free-rewards.com/claim-now", result.matchedLink)
        assertEquals(LinkConfidence.HIGH, result.confidence)
    }

    @Test
    fun testMediumConfidenceLink() {
        val message = "Check out this link: https://bit.ly/scam_promo"
        val result = detector.detectSuspiciousLink(message)
        assertTrue(result.isSuspicious)
        assertEquals("https://bit.ly/scam_promo", result.matchedLink)
        assertEquals(LinkConfidence.MEDIUM, result.confidence)
    }

    @Test
    fun testLowConfidenceLink() {
        val message = "This matches a test-low pattern in the URL: http://test-low.com"
        val result = detector.detectSuspiciousLink(message)
        assertTrue(result.isSuspicious)
        assertEquals("http://test-low.com", result.matchedLink)
        assertEquals(LinkConfidence.LOW, result.confidence)
    }

    @Test
    fun testMultipleLinksTakesHighestConfidence() {
        val message = "Go to https://bit.ly/scam_promo or visit http://free-rewards.com/gift"
        val result = detector.detectSuspiciousLink(message)
        assertTrue(result.isSuspicious)
        // High confidence from free-rewards.com takes priority over Medium confidence from bit.ly
        assertEquals("http://free-rewards.com/gift", result.matchedLink)
        assertEquals(LinkConfidence.HIGH, result.confidence)
    }
}
