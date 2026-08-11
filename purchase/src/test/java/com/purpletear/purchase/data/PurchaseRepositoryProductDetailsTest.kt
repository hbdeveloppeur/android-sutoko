package com.purpletear.purchase.data

import com.purpletear.purchase.data.PurchaseTestFixtures.productDetails
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import fr.sutoko.inapppurchase.application.domain.model.Product
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseRepositoryProductDetailsTest {

    private val fakeDao = FakePurchaseDao()
    private val fakeBilling = FakeBillingDataSource()
    private val repository =
        PurchaseRepositoryImpl(fakeDao, fakeBilling, FakeAnalyticsTracker())

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val events = mutableListOf<String>()
        override fun logEvent(name: String, params: Map<String, Any?>) {
            events += name
        }
        override fun setUserProperty(name: String, value: String?) = Unit
    }

    @Test
    fun `queryProductDetails maps billing details to domain Product`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(
            productDetails(
                sku = "sku",
                title = "Title",
                description = "Desc",
                formattedPrice = "$2.00"
            )
        )

        val result = repository.queryProductDetails("sku")

        assertTrue(result.isSuccess)
        assertEquals(
            Product(sku = "sku", title = "Title", description = "Desc", formattedPrice = "$2.00"),
            result.getOrNull()
        )
    }

    @Test
    fun `queryProductDetails returns failure when billing returns empty list`() = runTest {
        fakeBilling.queryProductDetailsListResult = emptyList()

        val result = repository.queryProductDetails("sku")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as IllegalArgumentException
        assertTrue(exception.message!!.contains("sku"))
    }

    @Test
    fun `queryProductDetails wraps billing exceptions in Result failure`() = runTest {
        val cause = RuntimeException("billing error")
        fakeBilling.throwOnQueryProductDetails = cause

        val result = repository.queryProductDetails("sku")

        assertTrue(result.isFailure)
        assertEquals(cause, result.exceptionOrNull())
    }

    @Test
    fun `queryProductDetails rejects blank SKU without calling billing`() = runTest {
        val result = repository.queryProductDetails("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(fakeBilling.queryProductDetailsCalls.isEmpty())
    }

    @Test
    fun `queryProductDetails calls billing data source exactly once`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(productDetails())

        repository.queryProductDetails("sku")

        assertEquals(1, fakeBilling.queryProductDetailsCalls.size)
    }

    @Test
    fun `queryProductDetails with multiple skus returns all products`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(
            productDetails(sku = "sku-1", formattedPrice = "$1.00"),
            productDetails(sku = "sku-2", formattedPrice = "$2.00"),
        )

        val result = repository.queryProductDetails(listOf("sku-1", "sku-2"))

        assertTrue(result.isSuccess)
        val products = result.getOrNull()!!
        assertEquals(2, products.size)
        assertEquals("$1.00", products.first { it.sku == "sku-1" }.formattedPrice)
        assertEquals("$2.00", products.first { it.sku == "sku-2" }.formattedPrice)
        assertEquals(1, fakeBilling.queryProductDetailsCalls.size)
    }

    @Test
    fun `queryProductDetails with multiple skus returns failure when one is missing`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(productDetails(sku = "sku-1"))

        val result = repository.queryProductDetails(listOf("sku-1", "sku-2"))

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as IllegalArgumentException
        assertTrue(exception.message!!.contains("sku-2"))
    }

    @Test
    fun `queryProductDetails with empty list returns success with empty list`() = runTest {
        val result = repository.queryProductDetails(emptyList())

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
        assertTrue(fakeBilling.queryProductDetailsCalls.isEmpty())
    }
}
