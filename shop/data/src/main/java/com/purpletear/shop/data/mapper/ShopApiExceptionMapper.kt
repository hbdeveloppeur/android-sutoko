package com.purpletear.shop.data.mapper

import com.purpletear.shop.data.exception.AccessDeniedException
import com.purpletear.shop.data.exception.AlreadyConsumedException
import com.purpletear.shop.data.exception.DataBaseException
import com.purpletear.shop.data.exception.InsufficientFundsException
import com.purpletear.shop.data.exception.InvalidTokenException
import com.purpletear.shop.data.exception.ItemAlreadyOwnedErrorException
import com.purpletear.shop.data.exception.MissingParametersException
import com.purpletear.shop.data.exception.NoTokensFoundException
import com.purpletear.shop.data.exception.ProductNotFoundException
import com.purpletear.shop.data.exception.ShopApiException
import com.purpletear.shop.data.exception.TrialConsumedException
import com.purpletear.shop.data.exception.TrialFinishedException
import com.purpletear.shop.data.exception.ValidationErrorException
import com.purpletear.shop.data.exception.SkuIdentifierNotFoundException


internal object ShopApiExceptionMapper {

    /**
     * Maps API error code to exception
     * @param errorCode API error code
     * @param errorMessage API error message
     * @return ApiException
     */
    fun mapToException(errorCode: String, errorMessage: String? = null): ShopApiException {
        return when (errorCode) {
                "missing_parameters" -> MissingParametersException(errorMessage ?: "Missing parameters")
                "invalid_token" -> InvalidTokenException(errorMessage ?: "invalid_token")
                "access_denied" -> AccessDeniedException(errorMessage ?: "Access denied")
                "already_consumed" -> AlreadyConsumedException(errorMessage ?: "already_consumed")
                "product_not_found" -> ProductNotFoundException(errorMessage ?: "Product not found")
                "database_error" -> DataBaseException(errorMessage ?: "Story not found")
                "no_tokens_found" -> NoTokensFoundException(errorMessage ?: "Story not found")
                "trial_finished" -> TrialFinishedException(errorMessage ?: "Story not found")
                "trial_consumed" -> TrialConsumedException(errorMessage ?: "Story not found")
                "ValidationError" -> ValidationErrorException(errorMessage ?: "Validation error")
                "ItemAlreadyOwnedError" -> ItemAlreadyOwnedErrorException(errorMessage ?: "Item already owned")
                "InsufficientFunds" -> InsufficientFundsException(errorMessage ?: "Insufficient funds")
                "ProductNotFound" -> ProductNotFoundException(errorMessage ?: "Product not found")
                "sku_identifier_not_found" -> SkuIdentifierNotFoundException(errorMessage ?: "SKU identifier not found")

            else -> ShopApiException(errorMessage ?: "Unknown error $errorCode")
        }
    }
}
