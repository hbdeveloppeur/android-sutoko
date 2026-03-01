package com.purpletear.game.data.di

import javax.inject.Qualifier

/**
 * Qualifier annotation for the Sutoko API Retrofit instance.
 * Used to distinguish between the Sutoko API Retrofit and the Portal Retrofit.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SutokoRetrofit
