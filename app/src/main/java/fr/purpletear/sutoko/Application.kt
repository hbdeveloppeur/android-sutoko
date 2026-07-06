package fr.purpletear.sutoko

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.os.Trace
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.bumptech.glide.Glide
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.github.anrwatchdog.ANRWatchDog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import coil.Coil
import coil.ImageLoader
import com.purpletear.sutoko.core.android.di.DefaultActivityProvider
import dagger.hilt.android.HiltAndroidApp
import dalvik.system.ZipPathValidator
import fr.purpletear.sutoko.presentation.util.DeleteCoilCache
import fr.purpletear.sutoko.symbols.SymbolsRepository
import fr.purpletear.sutoko.sync.balance.BalanceSyncCoordinator
import fr.purpletear.sutoko.sync.catalog.CatalogSyncCoordinator
import fr.purpletear.sutoko.sync.news.NewsSyncCoordinator
import fr.purpletear.sutoko.sync.purchase.PurchaseSyncCoordinator
import fr.purpletear.sutoko.sync.usergames.UserGamesSyncCoordinator
import fr.sutoko.inapppurchase.application.domain.coordinator.PurchaseBackendRegistrationCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject


private const val STRICT_MODE_TAG = "StrictMode"

@HiltAndroidApp
class Application : MultiDexApplication(), DefaultLifecycleObserver {
    @Inject
    lateinit var currentActivityProvider: DefaultActivityProvider

    @Inject
    lateinit var purchaseSyncCoordinator: PurchaseSyncCoordinator

    @Inject
    lateinit var catalogSyncCoordinator: CatalogSyncCoordinator

    @Inject
    lateinit var newsSyncCoordinator: NewsSyncCoordinator

    @Inject
    lateinit var balanceSyncCoordinator: BalanceSyncCoordinator

    @Inject
    lateinit var userGamesSyncCoordinator: UserGamesSyncCoordinator

    @Inject
    lateinit var purchaseBackendRegistrationCoordinator: PurchaseBackendRegistrationCoordinator

    @Inject
    lateinit var symbolsRepository: SymbolsRepository

    private val appSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .addLastModifiedToFileCacheKey(false)
                .build()
        )

        registerActivityLifecycleCallbacks(currentActivityProvider)

        val processLifecycleOwner = ProcessLifecycleOwner.get()
        purchaseSyncCoordinator.start(
            processLifecycleOwner.lifecycle,
            appSyncScope
        )
        catalogSyncCoordinator.start(
            processLifecycleOwner.lifecycle,
            appSyncScope
        )
        newsSyncCoordinator.start(
            processLifecycleOwner.lifecycle,
            appSyncScope
        )
        userGamesSyncCoordinator.start(
            processLifecycleOwner.lifecycle,
            appSyncScope
        )
        balanceSyncCoordinator.start(
            processLifecycleOwner.lifecycle,
            appSyncScope
        )
        purchaseBackendRegistrationCoordinator.start(appSyncScope)

        appSyncScope.launch {
            symbolsRepository.load()
        }

        processLifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    appSyncScope.cancel()
                }
            }
        )

        if (Build.VERSION.SDK_INT >= 34) {
            ZipPathValidator.clearCallback()
        }

        applicationContext.clearGlideCache()

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        initializeAnrWatchdog()

        appSyncScope.launch {
            createNotificationChannel()
        }

        appSyncScope.launch {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        }

        appSyncScope.launch {
            val config: PRDownloaderConfig = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30000)
                .setConnectTimeout(30000)
                .build()
            PRDownloader.initialize(applicationContext, config)
        }

        appSyncScope.launch {
            DeleteCoilCache.clearCache(applicationContext)
        }
    }

    private fun initializeAnrWatchdog() {
        if (!BuildConfig.ENABLE_ANR_WATCHDOG) return

        ANRWatchDog()
            .setANRListener { error ->
                runCatching {
                    FirebaseCrashlytics.getInstance().recordException(error)
                }.onFailure {
                    Log.e("ANRWatchdog", "Failed to record ANR", it)
                }
            }
            .start()
    }

    private fun Context.clearGlideCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Glide.get(this@clearGlideCache).apply {
                    clearDiskCache()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun enableStrictMode() {
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectResourceMismatches()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            threadPolicyBuilder.penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                if (violation.stackTrace.any { it.className.startsWithVendorPrefix() }) {
                    return@penaltyListener
                }
                Log.w(STRICT_MODE_TAG, "Thread policy violation", violation)
            }
        } else {
            threadPolicyBuilder.penaltyLog()
        }

        StrictMode.setThreadPolicy(threadPolicyBuilder.build())
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun String.startsWithVendorPrefix(): Boolean {
        return startsWith("com.oplus.") ||
            startsWith("com.coloros.") ||
            startsWith("com.heytap.") ||
            startsWith("com.oneplus.") ||
            startsWith("com.mediatek.")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "sutoko_fcm"
            val channelName = "Sutoko Fcm Channel"
            val channelDescription = "Sutoko Fcm Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}