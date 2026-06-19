package com.purpletear.sutoko.shop.presentation

import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType

sealed class ShopEvent {
    class BuyPack(val packType: CoinsPackType) : ShopEvent()
}