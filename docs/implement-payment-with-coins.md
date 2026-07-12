# Pay stories with coins — Implementation Plan

## Goal
Replace the Google Play Billing purchase flow on `GamePreview` with a coin-based purchase through the existing catalog-shop API.

## Context from code review

- `GamePreview` currently derives `isPurchased` from `PurchaseRepository.observePurchasedSkus()` + global premium.
- `GamePreviewPurchaseHandler` triggers `PurchaseRepository.purchase(sku)`, which launches Play Billing.
- `ShopApi` already exposes the two required endpoints (`shop/buy`, `product/is-granted`) but `buyCatalogProduct` is typed as `Response<Unit>` while the backend now returns the new balance.
- `ShopRepository` already manages balance observation / cache.
- `BalanceSyncCoordinator` keeps the balance in sync at app lifecycle boundaries.
- `game:presentation` already depends on `:auth` and `:purchase`; it does **not** yet depend on `:shop`.

## Decisions (team consensus)

1. **Keep Play Billing observation, add a parallel coin-purchase repository.**
   `PurchaseRepository` stays responsible for Play purchases and global premium.
   Coin purchases live in a dedicated `CoinPurchaseRepository` so `ShopRepository` does not become a grab bag.

2. **Session-scoped in-memory cache.**
   `CoinPurchaseRepository` caches granted SKUs in memory.
   The cache is a best-effort optimization only; the backend remains the source of truth.
   Persistence can be added later if cross-session ownership becomes an issue.

3. **Explicit error contract.**
   Backend error codes are mapped to a sealed `BuyStoryError`.
   `ValidationError` is **not** assumed to mean insufficient funds; it maps to a generic purchasability error until the backend provides a dedicated code.

4. **Put the new domain logic in `:shop`.**
   `game:presentation` will add a dependency on `:shop` and consume small use cases, matching the existing `ObserveShopBalanceUseCase` pattern.

5. **Single responsibility for the handler.**
   `GamePreviewPurchaseHandler` keeps managing the purchase-confirmation UI state but delegates the actual purchase to a new `BuyStoryWithCoinsUseCase`.

6. **Account connection is handled centrally.**
   `GamePreview` emits a new `GamePreviewEvent.OpenAccountConnection` event; `MainActivity` launches `AccountConnectionActivity`.

## Implementation steps

### 1. Backend contract fixes (`:shop`)

- Update `ShopApi.buyCatalogProduct` return type to `Response<BuyCatalogProductResponseDto>`.
- Add `BuyCatalogProductResponseDto(balance: CoinsBalanceDto)`.
- Add sealed `BuyStoryError`:
  - `AlreadyOwned`
  - `NotPurchasable` (maps to `ValidationError`)
  - `Network`
  - `Unknown`
- Parse the 400 error body (`{"code":"..."}`) in the repository and return typed failures.

### 2. Coin purchase repository (`:shop`)

- Add `CoinPurchaseRepository` interface:
  ```kotlin
  suspend fun buyStoryWithCoins(sku: String, userId: String): Result<Balance>
  suspend fun isStoryGranted(userId: String, skuIdentifiers: List<String>): Result<Boolean>
  fun observeCoinPurchasedSkus(): Flow<Set<String>>
  ```
- Implement `InMemoryCoinPurchaseRepository` backed by:
  - A `MutableStateFlow<Set<String>>` for cached SKUs.
  - `ShopApi` for buy and grant-check calls.
- `buyStoryWithCoins` calls `ShopApi.buyCatalogProduct`, updates the balance cache via `ShopRepository`, and on success adds the SKU to the in-memory cache.
- `isStoryGranted` checks the cache first, then calls `ShopApi.userHasProduct`, and updates the cache.
- `observeCoinPurchasedSkus` exposes the cache as a flow.
- Document the contract: cache is session-scoped optimization; source of truth is the backend.

### 3. Use cases (`:shop`)

- `BuyStoryWithCoinsUseCase(coinPurchaseRepository, userRepository)`
- `IsStoryGrantedUseCase(coinPurchaseRepository, userRepository)`
- `ObserveCoinPurchasedSkusUseCase(coinPurchaseRepository)`

### 4. `game:presentation` dependency

Add `implementation(project(":shop"))` to `game/presentation/build.gradle.kts`.

### 5. Update `GameActionState` / buttons

- Add `price: Int` and `isUserConnected: Boolean` to `GameActionState.Purchase`.
- Update `GameItem.toGameActionState(...)` to pass them.
- Update `GameButtonsState.toButtonsState()`:
  - Title remains `Buy`.
  - Subtitle = `<price> coins` when connected, `Connect to buy` when not connected.
- Make `ConfirmPurchase` subtitle dynamic using `price`.

### 6. Update `GamePreviewViewModel`

- Inject:
  - `UserRepository`
  - `ObserveCoinPurchasedSkusUseCase`
  - `BuyStoryWithCoinsUseCase`
  - `IsStoryGrantedUseCase`
- Expose `isUserConnected` state.
- Combine `coinPurchasedSkus` into `isPurchased`:
  ```kotlin
  isPurchased = catalog.skus.any { it in purchasedSkus || it in coinPurchasedSkus } || hasGlobalPremium
  ```
- On screen start, if connected and not purchased, call `IsStoryGrantedUseCase(catalog.skus)` once.
- On `GamePreviewAction.OnBuy`:
  - If not connected → emit `GamePreviewEvent.OpenAccountConnection`.
  - If connected → call `purchaseHandler.startPurchaseFlow()` as today.
- On `OnBuyConfirm` keep `purchaseHandler.confirmPurchase(sku)`.

### 7. Update `GamePreviewPurchaseHandler`

Replace `PurchaseRepository` dependency with `BuyStoryWithCoinsUseCase`.

### 8. Navigation event

- Add `GamePreviewEvent.OpenAccountConnection`.
- In `GamePreview`, forward it through a new callback `onOpenAccountConnection: () -> Unit`.
- In `MainActivity`, wire the callback to `openConnectionPage()`.

### 9. Error handling / alerts

- `AlreadyOwnedError` → show `already_bought_alert_title/description` and mark SKU as owned.
- `NotPurchasable` / `ValidationError` → generic purchase-failure message for now.
- Network / other → existing `GameUiError.Purchase`.

### 10. Strings

- Add `game_menu_connect_to_buy`.
- Reuse `game_menu_coins` for `<price> coins`.
- No new title string needed (`Buy` already exists).

### 11. Tests

- Add fakes for `CoinPurchaseRepository` and the new use cases.
- Update `GamePreviewPurchaseHandlerTest` to inject the new use-case fake.
- Add `GamePreviewViewModelTest` cases:
  - Not connected shows `Connect to buy` and emits `OpenAccountConnection`.
  - Connected shows `<price> coins`.
  - Successful coin purchase emits `PurchaseSuccess` and updates `isPurchased`.
  - `AlreadyOwnedError` is handled.
  - `ValidationError` maps to generic purchase failure.

## Out of scope / follow-up

- Persisting the coin-purchase cache across process death.
- Cross-device grant reconciliation beyond the single check on `GamePreview` open.
- Removing the legacy Play Billing purchase flow from other screens.

## Files likely touched

- `shop/src/main/java/com/purpletear/sutoko/shop/data/remote/ShopApi.kt`
- `shop/src/main/java/com/purpletear/sutoko/shop/domain/repository/CoinPurchaseRepository.kt`
- `shop/src/main/java/com/purpletear/sutoko/shop/data/repository/InMemoryCoinPurchaseRepository.kt`
- `shop/src/main/java/com/purpletear/sutoko/shop/di/*`
- `shop/src/main/java/com/purpletear/sutoko/shop/domain/usecase/*`
- `game/presentation/build.gradle.kts`
- `game/presentation/src/main/java/com/purpletear/game/presentation/model/GameActionState.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/common/states/GameButtonsState.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_preview/GamePreviewViewModel.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_preview/handlers/GamePreviewPurchaseHandler.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_preview/GamePreview.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_preview/events/GamePreviewEvent.kt`
- `game/presentation/src/main/res/values/strings.xml` (+ translations)
- `app/src/main/java/fr/purpletear/sutoko/screens/MainActivity.kt`
- `game/presentation/src/test/java/com/purpletear/game/presentation/game_preview/*`
