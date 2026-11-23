package com.purpletear.sutoko.analytics.data.statistics
import org.junit.Assert.*
import org.junit.Test
class DeliveryPolicyTest {
    @Test fun retryBudgetIsBoundedAndRespectsThrottling() {
        assertEquals(3600L,DeliveryPolicy.backoff(1))
        assertEquals(7200L,DeliveryPolicy.backoff(2))
        assertEquals(86400L,DeliveryPolicy.backoff(999))
        assertEquals(10800L,DeliveryPolicy.backoff(1,10800))
        assertEquals(86700L,DeliveryPolicy.backoff(999,999999,9999))
    }
    @Test fun errorsDoNotProduceRapidRetries() {
        assertFalse(DeliveryPolicy.isPermanentFailure(429)); assertFalse(DeliveryPolicy.isPermanentFailure(408))
        assertFalse(DeliveryPolicy.isPermanentFailure(503)); assertTrue(DeliveryPolicy.isPermanentFailure(400))
    }
    @Test fun keysUseActualLanguageAndLocaleIndependentChapterCodes() {
        assertEquals("fr-fr",ChapterKey.validated("sms","story_1","fr-FR","1A")?.language)
        assertEquals("1a",ChapterKey.validated("sms","story_1","fr-FR","1A")?.chapter)
        assertNull(ChapterKey.validated("friendzoned","160","fr","1a"))
        assertNull(ChapterKey.validated("sms","../secret","fr","1a"))
        assertNull(ChapterKey.validated("sms","valid","","1a"))
    }
}
