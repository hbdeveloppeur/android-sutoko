package com.example.sutokosharedelements

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import kotlin.properties.Delegates

interface UserRequestInterface {
    fun onConnected()
}

@Keep
class User(onUpdateCoinsOrDiamonds: (() -> Unit)? = null) : Parcelable {

    var uid: String? = null
        private set
    var email: String? = null
    var token: String? = null
    var profilName: String = ""
    var profilPictureUrl500: String = ""
    var profilPictureUrl64: String = ""
    var profilPictureUrl32: String = ""
    private var hasValidatedEmail: Boolean = false
    private var lastTimeValidationMailSent: Long? = null
    private var isBanned: Boolean = false
    var isPremium: Boolean = true
        private set

    var diamonds: Int by Delegates.observable(0) { property, oldValue, newValue ->
        if (onUpdateCoinsOrDiamonds != null) {
            onUpdateCoinsOrDiamonds()
        }
    }
    var coins: Int by Delegates.observable(0) { property, oldValue, newValue ->
        if (onUpdateCoinsOrDiamonds != null) {
            onUpdateCoinsOrDiamonds()
        }
    }


    fun addCoins(activity: Activity, amount: Int) {
        assert(amount >= 0)
        this.coins += amount
        this.saveLocalData(activity)
    }

    fun addDiamonds(activity: Activity, amount: Int) {
        assert(amount >= 0)
        this.diamonds += amount
        this.saveLocalData(activity)
    }

    fun deductCoinsCoerced(context: Context, amount: Int) {
        this.coins = (this.coins - amount).coerceAtLeast(0)
        this.saveLocalData(context)
    }

    fun deductCoins(context: Context, amount: Int) {
        assert(amount >= 0)
        this.coins -= amount
        assert(this.coins >= 0)
        this.saveLocalData(context)
    }

    fun deductDiamonds(activity: Activity, amount: Int) {
        assert(amount >= 0)
        this.diamonds -= amount
        assert(this.diamonds >= 0)
        this.saveLocalData(activity)
    }

    fun readLocalData(context: Context) {
        val s: SharedPreferences = context.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        uid = s.getString(KEY_UID, null)
        token = s.getString(KEY_TOKEN, null)
        email = s.getString(KEY_EMAIL, null)
        profilName = s.getString(KEY_PROFIL_NAME, "") ?: ""
        profilPictureUrl500 = s.getString(KEY_URL_500, "") ?: ""
        profilPictureUrl64 = s.getString(KEY_URL_64, "") ?: ""
        profilPictureUrl32 = s.getString(KEY_URL_32, "") ?: ""
        diamonds = s.getInt(KEY_DIAMONDS, 0)
        coins = s.getInt(KEY_COINS, 0)
    }

    fun saveLocalData(context: Context) {
        val s: SharedPreferences = context.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        s.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PROFIL_NAME, profilName)
            .putString(KEY_URL_500, profilPictureUrl500)
            .putString(KEY_URL_64, profilPictureUrl64)
            .putString(KEY_URL_32, profilPictureUrl32)
            .putInt(KEY_COINS, coins ?: 0)
            .putInt(KEY_DIAMONDS, diamonds ?: 0)
            .apply()
    }

    fun saveToken(activity: Activity, email: String, token: String, uid: String) {
        val s: SharedPreferences = activity.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        s.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_TOKEN, token)
            .putString(KEY_UID, uid)
            .apply()
    }

    fun disconnect(activity: Activity) {
        this.token = null
        val s: SharedPreferences = activity.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        s.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_TOKEN)
            .apply()
    }

    fun isConnected(): Boolean {
        return !token.isNullOrBlank() && !uid.isNullOrBlank()
    }


    fun connect(mail: String, password: String, callback: UserRequestInterface) {
    }


    /**
     * Determines if the app can send a validation mail
     * Limited to 3 minutes maximum
     * @return Boolean
     */
    fun canSendValidationMail(): Boolean {
        val cannot =
            lastTimeValidationMailSent != null && (lastTimeValidationMailSent!! - System.currentTimeMillis() > 3 * 1000 * 60)
        return !cannot
    }


    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()
        token = parcel.readString()
        profilName = parcel.readString() ?: ""
        profilPictureUrl500 = parcel.readString() ?: ""
        profilPictureUrl64 = parcel.readString() ?: ""
        profilPictureUrl32 = parcel.readString() ?: ""
        hasValidatedEmail = parcel.readByte() == 1.toByte()
        isBanned = parcel.readByte() == 1.toByte()
        diamonds = parcel.readInt()
        coins = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(token)
        parcel.writeString(profilName)
        parcel.writeString(profilPictureUrl500)
        parcel.writeString(profilPictureUrl64)
        parcel.writeString(profilPictureUrl32)
        parcel.writeByte(if (hasValidatedEmail) 1 else 0)
        parcel.writeByte(if (isBanned) 1 else 0)
        parcel.writeInt(diamonds ?: 0)
        parcel.writeInt(coins ?: 0)
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (profilName != other.profilName) return false
        if (profilPictureUrl500 != other.profilPictureUrl500) return false
        if (profilPictureUrl64 != other.profilPictureUrl64) return false
        if (profilPictureUrl32 != other.profilPictureUrl32) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profilName.hashCode()
        result = 31 * result + profilPictureUrl500.hashCode()
        result = 31 * result + profilPictureUrl64.hashCode()
        result = 31 * result + profilPictureUrl32.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(uid=$uid, email=$email, token=$token, profilName='$profilName', profilPictureUrl500='$profilPictureUrl500', profilPictureUrl64='$profilPictureUrl64', profilPictureUrl32='$profilPictureUrl32', hasValidatedEmail=$hasValidatedEmail, lastTimeValidationMailSent=$lastTimeValidationMailSent, isBanned=$isBanned, isPremium=$isPremium, diamonds=$diamonds, coins=$coins)"
    }

    companion object CREATOR : Parcelable.Creator<User> {

        private const val SHARED_PREF_NAME = "user_picture_url_SHARED_PREF_NAME"
        private const val KEY_PROFIL_NAME = "user_picture_url_profilname"
        private const val KEY_EMAIL = "KEY_EMAIL"
        private const val KEY_URL_500 = "user_picture_url500"
        private const val KEY_URL_64 = "user_picture_url64"
        private const val KEY_URL_32 = "user_picture_url32"
        private const val KEY_COINS = "KEY_COINS"
        private const val KEY_DIAMONDS = "KEY_DIAMONDS"
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_UID = "KEY_UID"


        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }

    }
}