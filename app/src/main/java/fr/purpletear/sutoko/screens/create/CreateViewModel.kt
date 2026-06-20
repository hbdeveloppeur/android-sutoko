package fr.purpletear.sutoko.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    shopRepository: ShopRepository,
    gameRepository: GameRepository,
    gamePurchaseRepository: PurchaseRepository,
    gameInstallRepository: GameInstallRepository,
    mediaUrlResolver: MediaUrlResolver,
    appVersionProvider: AppVersionProvider,
) : ViewModel() {
    val appBuildNumber: Int = appVersionProvider.getVersionCode()

    val balance: StateFlow<Resource<Balance>> = shopRepository.observeBalance()
        .map { Resource.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = Resource.Loading(),
        )


    val games: StateFlow<List<GameItem>> = combine(
        gameRepository.observeUserGames(),
        gamePurchaseRepository.observePurchasedSkus(),
        gameInstallRepository.observeInstalls(),
        gameInstallRepository.observeDownloadProgresses(),
    ) { catalogs, purchasedSkus, installs, downloads ->
        catalogs.map { catalog ->
            GameItem(
                catalog = catalog,
                install = installs.find { it.gameId == catalog.id },
                isPurchased = catalog.skus.any { it in purchasedSkus },
                bannerUrl = mediaUrlResolver.resolveBannerUrl(catalog.banner?.storagePath),
                logoUrl = mediaUrlResolver.resolveBannerUrl(catalog.logo?.storagePath),
                menuBackgroundUrl = mediaUrlResolver.resolveBannerUrl(catalog.menuBackground?.storagePath),
                downloadProgress = downloads.getOrDefault(catalog.id, null),
            )
        }
    }.catch { e ->
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = emptyList(),
    )
}