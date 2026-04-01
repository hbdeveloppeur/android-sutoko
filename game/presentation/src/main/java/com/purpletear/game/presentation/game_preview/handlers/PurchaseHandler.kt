package com.purpletear.game.presentation.game_preview.handlers

import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.shop.data.exception.InsufficientFundsException
import com.purpletear.shop.data.exception.InternetConnectivityException
import com.purpletear.shop.data.exception.ItemAlreadyOwnedErrorException
import com.purpletear.shop.data.exception.ProductNotFoundException
import com.purpletear.shop.data.exception.SkuIdentifierNotFoundException
import com.purpletear.shop.domain.usecase.BuyCatalogProductUseCase
import com.purpletear.shop.domain.usecase.RegisterOrderUseCaseIfNecessary
import com.purpletear.shop.domain.usecase.UserHasProductUseCase
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.sutoko.inapppurchase.domain.usecase.GooglePlayClientOwnsProductUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Handles game purchase operations.
 * Encapsulates purchase flow, ownership verification, and error handling.
 */
class PurchaseHandler @Inject constructor(
    private val customer: Customer,
    private val buyCatalogProductUseCase: BuyCatalogProductUseCase,
    private val userHasProductUseCase: UserHasProductUseCase,
    private val googlePlayClientOwnsProductUseCase: GooglePlayClientOwnsProductUseCase,
    private val registerOrderUseCaseIfNecessary: RegisterOrderUseCaseIfNecessary,
    private val makeToastService: MakeToastService,
) {
    sealed class Result {
        data object Success : Result()
        data object AlreadyOwned : Result()
        data object InsufficientFunds : Result()
        data class Error(val messageResId: Int) : Result()
    }

    suspend fun buyStory(game: Game): Result {
        require(customer.user.uid != null)
        require(customer.user.token != null)
        require(game.skuIdentifiers.isNotEmpty())

        val skuIdentifier = game.skuIdentifiers.first()

        return try {
            delay(1200)
            buyCatalogProductUseCase(
                userId = customer.getUserId(),
                skuIdentifier = skuIdentifier,
                type = "story"
            ).first().fold(
                onSuccess = {
                    delay(3200)
                    Result.Success
                },
                onFailure = { throw it }
            )
        } catch (e: InsufficientFundsException) {
            e.printStackTrace()
            Result.InsufficientFunds
        } catch (e: ItemAlreadyOwnedErrorException) {
            e.printStackTrace()
            Result.AlreadyOwned
        } catch (e: SkuIdentifierNotFoundException) {
            makeToastService(R.string.game_story_preview_buy_story_story_not_buyable_anymore)
            e.printStackTrace()
            Result.Error(R.string.game_story_preview_buy_story_story_not_buyable_anymore)
        } catch (e: ProductNotFoundException) {
            e.printStackTrace()
            Result.Error(R.string.game_story_preview_buy_story_unknown_error_occured_you_have_been_refunded)
        } catch (e: InternetConnectivityException) {
            makeToastService(R.string.game_buy_story_error_check_internet)
            Result.Error(R.string.game_buy_story_error_check_internet)
        } catch (e: Exception) {
            e.printStackTrace()
            makeToastService(R.string.game_story_preview_buy_story_unknown_error_occured_you_have_been_refunded)
            Result.Error(R.string.game_story_preview_buy_story_unknown_error_occured_you_have_been_refunded)
        }
    }

    suspend fun verifyOwnership(game: Game): Boolean {
        require(game.skuIdentifiers.isNotEmpty())
        require(customer.isUserConnected())

        val userId = customer.getUserId()
        val userToken = customer.getUserToken()

        val foundInGooglePlay = checkGooglePlayItems(game, userId, userToken)
        if (foundInGooglePlay) return true

        return try {
            userHasProductUseCase(
                userId = userId,
                skuIdentifiers = game.skuIdentifiers
            ).first().getOrThrow()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun checkGooglePlayItems(
        game: Game,
        userId: String,
        userToken: String,
    ): Boolean {
        return try {
            val googlePlayResult = googlePlayClientOwnsProductUseCase(
                sku = game.skuIdentifiers
            ).first().getOrThrow()

            val foundAtLeastOneSku = googlePlayResult.keys.any { sku -> sku in game.skuIdentifiers }

            for ((skuIdentifier, purchaseToken) in googlePlayResult) {
                registerOrderUseCaseIfNecessary(
                    purchaseToken,
                    skuIdentifier,
                    userId,
                    userToken,
                ).first()
            }

            foundAtLeastOneSku
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
