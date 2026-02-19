package com.purpletear.smsgame.activities.smsgameloader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.Data
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.purpletear.smsgame.activities.smsgame.SmsGameActivity
import com.purpletear.smsgame.activities.smsgame.objects.StoryChapter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCreatorResources
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import purpletear.fr.purpleteartools.Language
import purpletear.fr.purpleteartools.TableOfSymbols

class SmsGameLoaderActivity : AppCompatActivity() {
    private lateinit var model: SmsGameLoaderModel
    private lateinit var smsGameActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var premiumActivityResultLauncher: ActivityResultLauncher<Intent>
    private var hasGameActivityResult: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.smsGameActivityResultLauncher = this.registerSmsGameActivityResultLauncher()
        this.premiumActivityResultLauncher = this.registerPremiumActivityResultLauncher()
        this.model = SmsGameLoaderModel(this)
        FirebaseCrashlytics.getInstance().setCustomKey("story_id", model.card.id)
        FirebaseCrashlytics.getInstance()
            .setCustomKey("langCode", Language.determineLangDirectory())
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("model.symbols", model.symbols)
        outState.putParcelableArrayList("model.chapters", model.chapters)
        outState.putBoolean("hasGameActivityResult", hasGameActivityResult)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.symbols = savedInstanceState.getParcelable("model.symbols") ?: model.symbols
        model.chapters =
            savedInstanceState.getParcelableArrayList("model.chapters") ?: model.chapters
        hasGameActivityResult =
            savedInstanceState.getBoolean("hasGameActivityResult", hasGameActivityResult)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (!hasGameActivityResult) {
                this.startActivityForResult()
            }
            this.overridePendingTransition(0, 0)
        }
    }

    private fun registerSmsGameActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) foo@{ result: ActivityResult ->
            hasGameActivityResult = true
            when (result.resultCode) {
                1003 -> {
                    setResult(1003)
                    finish()
                    return@foo
                }

                Activity.RESULT_OK -> {
                    if (result.data != null) {
                        val symbols: TableOfSymbols? =
                            result.data!!.getParcelableExtra(Data.Companion.Extra.TABLE_OF_SYMBOLS.id)
                        if (symbols != null) {
                            this.model.symbols = symbols
                            this.model.symbols.save(this@SmsGameLoaderActivity)

                            if (!this.model.nextChapterIsPlayable()) {
                                setResult(1002)
                                finish()
                                return@foo
                            }

                            if (this.model.shouldStartPremiumActivity(this)) {
                                setResult(999)
                                finish()
                                return@foo
                            }
                        }
                    }

                    startGame()
                }

                Activity.RESULT_CANCELED -> {
                    setResult(1001)
                    finish()
                }

                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }

    private fun startActivityForResult() {
        this.smsGameActivityResultLauncher.launch(this.model.getSmsGameIntent(this))
    }

    private fun registerPremiumActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) foo@{ result: ActivityResult ->
            hasGameActivityResult = true
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    this@SmsGameLoaderActivity.startActivityForResult()
                    return@foo
                }

                Activity.RESULT_CANCELED -> {
                    finish()
                    return@foo
                }

                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }


    private fun startGame() {
        startActivityForResult()
    }

    companion object {

        fun require(
            activity: Activity,
            symbols: TableOfSymbols,
            card: Game,
            chapters: ArrayList<StoryChapter>,
            customer: Customer,
        ): Intent {
            val intent = Intent(activity, SmsGameLoaderActivity::class.java)
            intent.putExtra(
                Data.Companion.Extra.USER_HISTORY_HAS_AT_LEAST_ONE_ORDER.id,
                customer.history.hasBoughtAtLeastOneStory()
            )
            intent.putExtra(Data.Companion.Extra.TABLE_OF_SYMBOLS.id, symbols as Parcelable)
            intent.putExtra("isPaid", card.isPremium)
            intent.putParcelableArrayListExtra(Data.Companion.Extra.CHAPTERS_ARRAY.id, chapters)
            return SmsGameActivity.require(
                intent,
                TableOfCreatorResources(),
                card,
                chapters,
                StoryType.OFFICIAL_STORY
            )
        }
    }
}
