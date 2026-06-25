package com.purpletear.sutoko.shop.domain.repository.model

sealed class CoinsPackType {
    object Low : CoinsPackType()
    object Medium : CoinsPackType()
    object High : CoinsPackType()
}
