package com.purpletear.shop.domain.repository

import com.purpletear.shop.domain.model.AiCustomerState
import com.purpletear.shop.domain.model.AiMessagePack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ShopRepository {
    val isOpen : StateFlow<Boolean>
    suspend fun getAiMessagesPacks(): Flow<Result<List<AiMessagePack>>>
    suspend fun buy(
        userId : String,
        userToken : String,
        orderId : String,
        purchaseToken : String,
        productId : String,
    ) : Flow<Result<AiCustomerState>>
    suspend fun preBuy(
        orderId : String,
        purchaseToken : String,
        productId : String,
    ) : Flow<Result<Unit>>
    suspend fun tryMessagePack(uid: String, userToken: String): Flow<Result<Boolean>>
    fun openDialog() : Unit
    fun closeDialog() : Unit
    suspend fun getUserAccountState(userId : String) : Flow<Result<AiCustomerState>>
}