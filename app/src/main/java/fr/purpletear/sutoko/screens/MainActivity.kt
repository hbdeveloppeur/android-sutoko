package fr.purpletear.sutoko.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.example.sharedelements.Data
import com.example.sharedelements.SutokoAppParams
import com.example.sharedelements.theme.SutokoTheme
import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.executeFlowUseCase
import com.purpletear.aiconversation.presentation.navigation.AiConversationRouteDestination
import com.purpletear.aiconversation.presentation.screens.character.add_character.AddCharacterScreen
import com.purpletear.aiconversation.presentation.screens.character.add_character.viewmodels.AddCharacterViewModel
import com.purpletear.aiconversation.presentation.screens.conversation.ConversationScreen
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.VoiceRecordViewModel
import com.purpletear.aiconversation.presentation.screens.home.AiConversationHomeScreen
import com.purpletear.aiconversation.presentation.screens.home.viewModels.AiConversationHomeViewModel
import com.purpletear.aiconversation.presentation.screens.image_viewer.ImageViewerScreen
import com.purpletear.aiconversation.presentation.screens.media.image_generator.ImageGeneratorScreen
import com.purpletear.aiconversation.presentation.screens.shopDialog.MessagesCoinsDialogComposable
import com.purpletear.game.presentation.screens.ChaptersComposable
import com.purpletear.game.presentation.screens.GamePreview
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.GetGameUseCase
import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.ObserveNotificationRequestUseCase
import com.purpletear.sutoko.notification.usecase.SetCurrentScreenUseCase
import com.purpletear.sutoko.permission.domain.repository.PermissionRepository
import com.purpletear.sutoko.permission.domain.sealed.Permission
import com.purpletear.sutoko.popup.presentation.PopUpComposable
import com.purpletear.sutoko.user.usecase.OpenSignInPageObservableUseCase
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.helpers.NotificationHelper
import fr.purpletear.sutoko.popup.domain.PopUpIconUrl
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.screens.account.AccountActivity
import fr.purpletear.sutoko.screens.accountConnection.AccountConnectionActivity
import fr.purpletear.sutoko.screens.accountConnection.AccountConnectionActivityModel
import fr.purpletear.sutoko.screens.create.CreatePageComposable
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.MainScreenPages
import fr.purpletear.sutoko.screens.main.presentation.screens.MainScreen
import fr.purpletear.sutoko.screens.params.SutokoParamsActivity
import fr.purpletear.sutoko.screens.splashscreen.SplashScreen
import fr.purpletear.sutoko.screens.web.WebActivity
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.purpletear.sutoko.shop.coinsLogic.CustomerCallbacks
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Order
import fr.purpletear.sutoko.shop.shop.ShopActivity
import fr.sutoko.inapppurchase.data.datasource.BillingDataSource
import fr.sutoko.inapppurchase.data.repository.BillingRepositoryImpl
import fr.sutoko.inapppurchase.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject
import fr.purpletear.friendzone.activities.load.Load as Friendzoned1Loader
import fr.purpletear.friendzone2.activities.load.Load as Friendzoned2Loader
import fr.purpletear.friendzone4.game.activities.load.Load as Friendzoned4Loader
import friendzone3.purpletear.fr.friendzon3.Load as Friendzoned3Loader

@AndroidEntryPoint
class MainActivity @Inject constructor(

) : ComponentActivity(),
    CustomerCallbacks, BillingClientStateListener {
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>
    private lateinit var optionsLauncher: ActivityResultLauncher<Intent>
    private lateinit var userStoryLauncher: ActivityResultLauncher<Intent>
    private val viewModel: HomeScreenViewModel by viewModels()
    private var shopActivityLauncher: ActivityResultLauncher<Intent> =
        registerLaunchForResultShopActivity()

    private var _newDestination: MutableStateFlow<String?> =
        MutableStateFlow(null)

    @Inject
    lateinit var billingDataSource: BillingDataSource

    @Inject
    lateinit var openSignInPageObservableUseCase: OpenSignInPageObservableUseCase

    @Inject
    lateinit var billingRepository: BillingRepository

    @Inject
    lateinit var showPopUpUseCase: ShowPopUpUseCase

    @Inject
    lateinit var observePopUpInteractionUseCase: GetPopUpInteractionUseCase

    @Inject
    lateinit var permissionRepository: PermissionRepository

    @Inject
    lateinit var observeNotificationRequestUseCase: ObserveNotificationRequestUseCase

    @Inject
    lateinit var setCurrentScreenUseCase: SetCurrentScreenUseCase

    @Inject
    lateinit var symbols: TableOfSymbols

    @Inject
    lateinit var getGameUseCase: GetGameUseCase

    @Inject
    lateinit var getChaptersUseCase: GetChaptersUseCase

    @Inject
    lateinit var customer: Customer


    override fun onDestroy() {
        // Remove all observers to prevent memory leaks
        viewModel.toast.removeObservers(this)
        viewModel.navigateToNews.removeObservers(this)
        viewModel.saveSymbols.removeObservers(this)
        viewModel.navigateToShop.removeObservers(this)
        viewModel.navigate.removeObservers(this)

        (billingRepository as BillingRepositoryImpl).clearActivity()
        super.onDestroy()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _newDestination.update { intent.getStringExtra("destination") }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        this.registerOptionsLauncher()
        this.registerUserStoryLauncher()
        this.registerLoginLauncher()
        this.load()

        (billingRepository as BillingRepositoryImpl).setActivity(this)

        openSignInPageObservableUseCase().observe(this@MainActivity, accountObserver())

        setContent {
            SutokoTheme {
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
                val navController = rememberNavController()

                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val job = lifecycleOwner.lifecycleScope.launch {
                        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            _newDestination.collect {
                                it?.let {
                                    navController.navigate(it)
                                    _newDestination.value = null
                                }
                            }
                        }
                    }
                    onDispose { job.cancel() }
                }

                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener { _, destination, arguments ->
                        val characterId = arguments?.getInt("character_id")
                        val screen = when (destination.route) {
                            AiConversationRouteDestination.Conversation.route -> {
                                characterId?.let {
                                    Screen.Conversation(characterId = it)
                                } ?: Screen.Unspecified
                            }

                            else -> {
                                Screen.Unspecified
                            }
                        }
                        setCurrentScreenUseCase(screen)
                    }
                }

                Box(Modifier.Companion.imePadding()) {
                    NavHost(
                        navController,
                        // startDestination = MainScreenPages.SplashScreen.route,
                        startDestination = MainScreenPages.Create.route,
                    ) {

                        composable(MainScreenPages.SplashScreen.route) {
                            SplashScreen(
                                onNavigateToMain = {
                                    navController.navigate(MainScreenPages.Home.route) {
                                        // Keeps the stack clean
                                        popUpTo(MainScreenPages.SplashScreen.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        animatedComposable(
                            route = MainScreenPages.GamePreview.route,
                            arguments = listOf(
                                navArgument("gameId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            GamePreview(
                                onNavigateToGame = onNavigateToGame@{ gameId, isGranted ->
                                    // Legacy game IDs are now string-based; checks moved to repository layer
                                    if (gameId.hashCode() in intArrayOf(159, 161, 162, 163)) {
                                        startFriendzoned(gameId, isGranted)
                                        return@onNavigateToGame
                                    }
                                    startSmsGameLoaderActivity(
                                        gameId = gameId,
                                        isGranted = isGranted
                                    )
                                },
                                onBuyGame = { game ->
                                    onBuyGame(game)
                                },
                                onOpenChapters = { game, _ ->
                                    navController.navigate(MainScreenPages.Chapters.createRoute(game.id))
                                },
                                onOpenShop = {
                                    startShop()
                                },
                                onGameDeleted = {
                                    navController.navigateUp()
                                }
                            )
                        }

                        animatedComposable(MainScreenPages.Create.route) { backStackEntry ->
                            CreatePageComposable(
                                onAccountButtonPressed = {
                                    onAccountPressed()
                                },
                                onCoinsButtonPressed = {
                                    onCoinsPressed()
                                },
                                onDiamondsButtonPressed = {
                                    onDiamondPressed()
                                },
                                onOptionsButtonPressed = {
                                    onOptionsPressed()
                                }
                            )
                        }

                        composable(
                            route = MainScreenPages.Chapters.route,
                            arguments = listOf(
                                navArgument("gameId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            ChaptersComposable(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                            )
                        }

                        // Sutoko - Home screen.
                        composable(MainScreenPages.Home.route) {
                            viewModel.displayAiConversationCard(
                                this@MainActivity.getAppParams(),
                            )

                            MainScreen(
                                viewModel = viewModel,
                                size = widthSizeClass,
                                mainNavController = navController
                            )
                        }

                        // Sutoko - Ai Conversation - Home screen.
                        animatedComposable(
                            route = AiConversationRouteDestination.Home.route,
                        ) {
                            val viewModel: AiConversationHomeViewModel = hiltViewModel()
                            AiConversationHomeScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }

                        // Sutoko - Ai Conversation - Add character screen.
                        animatedComposable(AiConversationRouteDestination.AddCharacter.route) { backStackEntry ->
                            val savedStateHandle =
                                navController.currentBackStackEntry?.savedStateHandle
                            val viewModel: AddCharacterViewModel = hiltViewModel(backStackEntry)
                            LaunchedEffect(Unit) {
                                viewModel.bindNavigationChanges(savedStateHandle = savedStateHandle)
                            }
                            AddCharacterScreen(
                                viewModel,
                                navController = navController
                            )
                        }

                        // Sutoko - Ai Conversation - Conversation screen.
                        animatedComposable(
                            route = AiConversationRouteDestination.Conversation.route,
                            arguments = listOf(AiConversationRouteDestination.Conversation.namedNavArgument!!)
                        ) {
                            val savedStateHandle =
                                navController.currentBackStackEntry?.savedStateHandle
                            val viewModel: ConversationViewModel = hiltViewModel()
                            val voiceRecordViewModel: VoiceRecordViewModel = hiltViewModel()
                            LaunchedEffect(Unit) {
                                viewModel.bindNavigationChanges(savedStateHandle = savedStateHandle)
                                voiceRecordViewModel.microphonePermissionRequired.observe(
                                    this@MainActivity,
                                    audioPermissionObserver
                                )
                            }
                            ConversationScreen(
                                viewModel = viewModel,
                                voiceRecordViewModel = voiceRecordViewModel,
                                navController = navController
                            )
                        }

                        animatedComposable(AiConversationRouteDestination.GenerateImage.route) { _ ->
                            ImageGeneratorScreen(
                                viewModel = hiltViewModel(),
                                navController = navController
                            )
                        }

                        animatedComposable(AiConversationRouteDestination.ImageViewer().route) { backStackEntry ->
                            ImageViewerScreen(url = backStackEntry.arguments?.getString("url")!!)
                        }
                    }

                    MessagesCoinsDialogComposable()

                    PopUpComposable()
                }
            }
        }

        this.observers()
    }

    private val toasterObserver = Observer<UiText.StringResource> { text ->
        Toast.makeText(applicationContext, getString(text.id), Toast.LENGTH_SHORT).show()
    }

    private val audioPermissionObserver = Observer<Unit> {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onResume() {
        super.onResume()
        viewModel.customer.read(this)
        viewModel.onEvent(MainEvents.OnAppear)
    }


    override fun onCoinsOrDiamondsUpdated(coins: Int, diamonds: Int) {

    }

    /**
     * Load books and customer data from the intent and the ViewModel respectively.
     * If the API level is 33 or higher, use the generic method to retrieve the list of books.
     * Otherwise, use the deprecated method to retrieve the list of books.
     * Finally, update the ViewModel with the loaded customer data.
     */
    private fun load() {

        executeFlowUseCase({
            permissionRepository.permissionRequest
        }, onStream = { permission ->
            when (permission) {
                is Permission.Notification -> {
                    requestNotificationPermission()
                }

                else -> {}
            }
        })

        executeFlowUseCase({
            observeNotificationRequestUseCase()
        }, onStream = { notification ->
            notification?.let {
                val helper = NotificationHelper()
                helper(context = this, notification = it)
            }
        })
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                val tag = showPopUpUseCase(
                    popUp = SutokoPopUp(
                        title = UiText.StringResource(R.string.ai_conversation_confirm_delete_title),
                        icon = PopUpIconUrl("https://data.sutoko.app/resources/sutoko-ai/image/background_waiting_screen.jpg"),
                        iconHeight = 68.dp,
                        buttonText = UiText.StringResource(fr.purpletear.sutoko.shop.presentation.R.string.sutoko_continue),
                        description = UiText.DynamicText("Eva is able to send notification"),
                    )
                )

                executeFlowUseCase({
                    observePopUpInteractionUseCase(tag)
                }, { interaction ->
                    when (interaction.event) {
                        PopUpUserInteraction.Confirm -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        else -> {}
                    }
                })
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private fun accountObserver() = Observer<Unit> {
        val intent =
            AccountConnectionActivity.Companion.require(
                this,
                AccountConnectionActivityModel.Page.SIGNIN
            )
        this.loginLauncher.launch(intent)
    }

    /**
     * Observes the live data object below in the view model and starts the
     * StoryPreviewActivity when the value changes:
     *  - navigateToCard : MutableLiveData<Card>
     *      Navigates to the StoryPreviewActivity with the card as an extra.
     *  - navigateToNews : MutableLiveData<News>
     *      Navigates to the WebActivity with the news as an extra.
     */
    private fun observers() {

        viewModel.toast.observe(this, toasterObserver)
        val newsNavigationObserver = Observer<News> { news ->
            news.link?.let {
                val intent = WebActivity.Companion.require(this, it, null, SutokoAppParams())
                this.startActivity(intent)
            }
        }

        viewModel.navigateToNews.observe(this, newsNavigationObserver)

        // saveSymbols
        val saveSymbolsObserver = Observer<TableOfSymbols> { symbols ->
            symbols.save(this)
        }
        viewModel.saveSymbols.observe(this, saveSymbolsObserver)

        val navigateToShopObserver = Observer<Unit> {
            this.startShop()
        }

        viewModel.navigateToShop.observe(this, navigateToShopObserver)

        val navigateObserver = Observer<MainEvents> { event ->
            when (event) {

                is MainEvents.AccountButtonPressed -> {
                    this.onAccountPressed()
                }

                is MainEvents.OptionButtonPressed -> {
                    this.onOptionsPressed()
                }

                is MainEvents.CoinButtonPressed -> {
                    this.onCoinsPressed()
                }

                is MainEvents.DiamondButtonPressed -> {
                    this.onDiamondPressed()
                }

                else -> {
                    // Log the unhandled event instead of throwing an exception to prevent crashes
                    android.util.Log.w("MainActivity", "Unhandled event: $event")
                }
            }
        }

        viewModel.navigate.observe(this, navigateObserver)
    }

    private fun onBuyGame(game: Game) {
        customer.onBuyStory(
            activity = this,
            card = game,
            money = Order.Money.COINS
        )
    }

    private fun onAccountPressed() {
        val intent = Intent(this, AccountActivity::class.java)
        this.startActivity(intent)
    }

    private fun getAppParams(): SutokoAppParams {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Data.Companion.Extra.APP_PARAMS.id,
                SutokoAppParams::class.java
            ) ?: SutokoAppParams()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Data.Companion.Extra.APP_PARAMS.id) ?: SutokoAppParams()
        }
    }

    private fun startFriendzoned(gameId: String, isGranted: Boolean) {
        // Legacy game loaders - these will need to be updated with new string IDs
        // For now, we hash the string ID to match the old Int-based logic
        val intent = when (gameId.hashCode()) {
            162 -> Intent(this, Friendzoned1Loader::class.java)
            161 -> Intent(this, Friendzoned2Loader::class.java)
            159 -> Intent(this, Friendzoned3Loader::class.java)
            163 -> Intent(this, Friendzoned4Loader::class.java)
            else -> throw IllegalArgumentException("Invalid gameId: $gameId")
        }

        intent.putExtra("symbols", symbols as Parcelable)
        intent.putExtra("granted", isGranted)

        startActivity(intent)
    }

    private fun startSmsGameLoaderActivity(gameId: String, isGranted: Boolean) {

    }

    private fun registerLoginLauncher() {
        this.loginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                viewModel.customer.read(this)
                val isSuccessful = result.resultCode == RESULT_OK

                if (isSuccessful) {
                    // sync eventual
                }
            }
    }

    private fun registerOptionsLauncher() {
        this.optionsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                viewModel.customer.read(this)
            }
    }

    private fun registerUserStoryLauncher() {
        this.userStoryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->


            }
    }

    private fun onOptionsPressed() {
        val intent = SutokoParamsActivity.Companion.require(this, SutokoAppParams())
        this.optionsLauncher.launch(intent)
    }

    private fun onDiamondPressed() {
        this.viewModel.onEvent(MainEvents.TapDiamondsLabel)
    }

    private fun onCoinsPressed() {
        this.viewModel.onEvent(MainEvents.TapCoinsLabel)
    }

    private fun registerLaunchForResultShopActivity(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                this.viewModel.customer.read(this@MainActivity)
            }
        }
    }

    private fun startShop() {
        val intent = Intent(this, ShopActivity::class.java)
        shopActivityLauncher.launch(intent)
    }

    override fun onBillingServiceDisconnected() {
        // Billing service disconnection is handled by BillingRepositoryImpl
        // No additional action needed in MainActivity
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        // Billing setup completion is handled by BillingRepositoryImpl
        // No additional action needed in MainActivity
    }
}
