package com.example.sutokosharedelements

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import com.example.sutokosharedelements.tables.points.PlayerPoint
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

@Keep
class SutokoPlayerPointsManager {

    private var localPoints: ArrayList<PlayerPoint> = ArrayList()

    companion object {

        private const val KEY_POINTS = "POINTS";
        private const val SHARED_PREF_NAME = "sutoko_purpletear_shared_preferences";

        enum class AbortedCodes {
            NOT_ENOUGH_POINT,
            USER_NOT_CONNECTED,
        }

        fun updateFirebasePointsIfNecessary(
            activity: Activity,
            user: FirebaseUser?,
            onlyIfNecessary: Boolean,
            instance: FirebaseFirestore,
            onSuccess: () -> Unit,
            onFailure: (exception: FirebaseException?) -> Unit,
            onCompletion: (() -> Unit)? = null
        ) {
            if (user != null && user.isEmailVerified) {
                val o = SutokoPlayerPointsManager()
                o.readLocalData(activity)
                o.localPoints.forEachIndexed { index, point ->
                    if (onlyIfNecessary && !point.isSynchronizedWithFirebase) {
                        o.addOrSetPointOnFirebase(user, instance, point, {
                            o.updateIsSynchronized(activity, point, true)
                            onSuccess()
                        }, onFailure)
                    } else if (!onlyIfNecessary) {
                        o.addOrSetPointOnFirebase(user, instance, point, {
                            o.updateIsSynchronized(activity, point, true)
                            onSuccess()
                        }, onFailure)
                    }
                }
            }
            if (null != onCompletion) {
                Handler(Looper.getMainLooper()).post(onCompletion)
            }
        }

        fun consume(
            activity: Activity,
            pointValue: Int,
            productId: Int,
            user: FirebaseUser?,
            instance: FirebaseFirestore,
            onSuccess: () -> Unit,
            onAborted: (code: AbortedCodes) -> Unit,
            onFailure: (exception: FirebaseException?) -> Unit
        ) {
            if (user != null && user.isEmailVerified) {
                val o = SutokoPlayerPointsManager()
                o.readLocalData(activity)
                o.getFirebasePointOperations(activity, user, instance, { pointsOperations ->

                    if (o.hasConsistentData(activity, pointsOperations)) {
                        o.buy(
                            activity,
                            pointValue,
                            productId,
                            user,
                            instance,
                            onSuccess,
                            onAborted,
                            onFailure
                        )
                    } else {
                        o.updateData(activity, pointsOperations, user, instance, {
                            o.buy(
                                activity,
                                pointValue,
                                productId,
                                user,
                                instance,
                                onSuccess,
                                onAborted,
                                onFailure
                            )
                        }, onFailure)
                    }
                }, onFailure)
            } else {
                onAborted(AbortedCodes.USER_NOT_CONNECTED)
            }
        }

        fun reloadData(
            activity: Activity,
            user: FirebaseUser?,
            instance: FirebaseFirestore,
            onSuccess: () -> Unit,
            onAborted: (code: AbortedCodes) -> Unit,
            onFailure: (exception: FirebaseException?) -> Unit
        ) {
            if (user != null && user.isEmailVerified) {
                val o = SutokoPlayerPointsManager()
                o.readLocalData(activity)
                o.getFirebasePointOperations(activity, user, instance, { pointsOperations ->
                    if (o.hasConsistentData(activity, pointsOperations)) {
                        Handler(Looper.getMainLooper()).post(onSuccess)
                    } else {
                        o.updateData(activity, pointsOperations, user, instance, {
                            Handler(Looper.getMainLooper()).post(onSuccess)
                        }, onFailure)
                    }
                }, onFailure)
            } else {
                onAborted(AbortedCodes.USER_NOT_CONNECTED)
            }
        }

        fun hasOrder(activity: Activity, productId: Int): Boolean {
            val o1 = SutokoPlayerPointsManager()
            val o2 = PlayerPoint(PlayerPoint.PointType.CONSUMPTION, 0, -1, productId)
            return o1.localDataHas(activity, o2)
        }

        fun getOrderList(activity: Activity): ArrayList<PlayerPoint> {
            val o1 = SutokoPlayerPointsManager()
            o1.readLocalData(activity)
            return o1.localPoints
        }

        fun getOrdersCount(activity: Activity): Int {
            val o = SutokoPlayerPointsManager()
            return o.getOrdersCount(activity)
        }

        fun getPointsCount(activity: Activity): Int {
            val o = SutokoPlayerPointsManager()
            return o.getPointsCount(activity)
        }
    }

    private fun buy(
        activity: Activity,
        pointValue: Int,
        productId: Int,
        user: FirebaseUser,
        instance: FirebaseFirestore,
        onSuccess: () -> Unit,
        onAborted: (code: AbortedCodes) -> Unit,
        onFailure: (exception: FirebaseException?) -> Unit
    ) {

        if (hasEnoughPointLocally(activity, pointValue)) {
            val point = PlayerPoint(
                PlayerPoint.PointType.CONSUMPTION,
                pointValue,
                System.currentTimeMillis(),
                productId
            )
            addOrSetPointOnFirebase(user, instance, point, {
                addPointToLocal(activity, point, true)
                onSuccess()
            }, onFailure)
        } else {
            onAborted(AbortedCodes.NOT_ENOUGH_POINT)
        }
    }

    fun updateData(
        activity: Activity,
        pointsOperations: ArrayList<PlayerPoint>,
        firebaseUser: FirebaseUser,
        instance: FirebaseFirestore,
        onSuccess: () -> Unit,
        onFailure: (exception: FirebaseException?) -> Unit
    ) {
        readLocalData(activity)
        localPoints = mergeArrays(localPoints, pointsOperations)
        unvalidateAllLocalOperations()
        saveLocalData(activity)
        updateFirebasePointsIfNecessary(
            activity,
            firebaseUser,
            false,
            instance,
            onSuccess,
            onFailure
        )
    }

    private fun mergeArrays(
        a1: ArrayList<PlayerPoint>,
        a2: ArrayList<PlayerPoint>
    ): ArrayList<PlayerPoint> {
        val arr: ArrayList<PlayerPoint> = ArrayList()
        a1.addAll(a2)
        a1.forEach { operation ->
            if (!arr.contains(operation)) {
                arr.add(operation)
            }
        }
        return arr
    }

    private fun unvalidateAllLocalOperations() {
        for (i in 0 until localPoints.count()) {
            localPoints[i].isSynchronizedWithFirebase = false
        }
    }

    private fun localDataAreAllSynced(activity: Activity): Boolean {
        readLocalData(activity)
        localPoints.forEach {
            if (!it.isSynchronizedWithFirebase) {
                return false
            }
        }
        return true
    }

    private fun hasConsistentData(
        activity: Activity,
        firebasePointsOperations: ArrayList<PlayerPoint>
    ): Boolean {
        return localDataAreAllSynced(activity) && localPoints.count() == firebasePointsOperations.count()
    }

    private fun getFirebasePointOperations(
        activity: Activity,
        firebaseUser: FirebaseUser,
        instance: FirebaseFirestore,
        onSuccess: (operations: ArrayList<PlayerPoint>) -> Unit,
        onFailure: (exception: FirebaseException?) -> Unit
    ) {
        instance
            .collection(PrivateFirebaseTreeStructure.getPathToPoints(firebaseUser))
            .get()
            .addOnSuccessListener {
                onSuccess(PlayerPoint.getOperationsFromFirebaseDocumentSnapshot(it))
            }
            .addOnFailureListener {
                onFailure(it as FirebaseException)
            }
    }

    private fun getPointsCount(activity: Activity): Int {
        var availablePoints = 0
        readLocalData(activity)
        localPoints.forEach { operation ->
            if (operation.type == PlayerPoint.PointType.CONSUMPTION) {
                availablePoints -= operation.value
            } else {
                availablePoints += operation.value
            }
        }
        return availablePoints
    }

    private fun getOrdersCount(activity: Activity): Int {
        var count = 0
        readLocalData(activity)
        localPoints.forEach { operation ->
            if (operation.type == PlayerPoint.PointType.CONSUMPTION) {
                count++
            }
        }
        return count
    }

    private fun hasEnoughPointLocally(activity: Activity, requestPointValue: Int): Boolean {
        return getPointsCount(activity) >= requestPointValue

    }

    private fun addPoint(
        activity: Activity,
        firebaseUser: FirebaseUser,
        instance: FirebaseFirestore,
        point: PlayerPoint,
        onSuccess: () -> Unit,
        onFailure: (exception: FirebaseException?) -> Unit
    ) {
        readLocalData(activity)
        if (localDataHas(activity, point)) {
            return
        } else {
            addPointToLocal(activity, point, false)
            hasPointOnFirebase(firebaseUser, instance, point, { hasPointOnFirebase ->
                if (hasPointOnFirebase) {
                    onSuccess()
                } else {
                    addOrSetPointOnFirebase(firebaseUser, instance, point, {
                        updateIsSynchronized(activity, point, true)
                    }, {
                        updateIsSynchronized(activity, point, false)
                        onFailure(it)
                    })
                }
            }, onFailure)
        }
    }

    private fun hasPointOnFirebase(
        firebaseUser: FirebaseUser,
        instance: FirebaseFirestore,
        point: PlayerPoint,
        onSuccess: (hasPointOnFirebase: Boolean) -> Unit,
        onFailure: (exception: FirebaseException?) -> Unit
    ) {
        instance
            .collection(PrivateFirebaseTreeStructure.getPathToPoints(firebaseUser))
            .document(point.codeIdentifier)
            .get().addOnSuccessListener {
                onSuccess(it.exists())
            }
            .addOnFailureListener {
                onFailure(it as FirebaseException)
            }
    }

    private fun addOrSetPointOnFirebase(
        firebaseUser: FirebaseUser,
        instance: FirebaseFirestore,
        point: PlayerPoint,
        onSuccess: () -> Unit,
        onFailure: (exception: FirebaseException?) -> Unit
    ) {
        instance
            .collection(PrivateFirebaseTreeStructure.getPathToPoints(firebaseUser))
            .document(point.codeIdentifier).set(point)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it as FirebaseException)
            }
    }


    /**
     * Adds a point in the local storage
     *
     * @param activity
     * @param point
     * @param isSynchronized
     */
    internal fun addPointToLocal(activity: Activity, point: PlayerPoint, isSynchronized: Boolean) {
        if (!localDataHas(activity, point)) {
            point.isSynchronizedWithFirebase = isSynchronized
            localPoints.add(point)
            saveLocalData(activity)
        }
    }

    private fun updateIsSynchronized(
        activity: Activity,
        point: PlayerPoint,
        isSynchronized: Boolean
    ) {
        val index = localPoints.indexOf(point)
        localPoints[index].isSynchronizedWithFirebase = isSynchronized
        saveLocalData(activity)
    }

    /**
     * Determines if the point is contained in the local storage
     *
     * @param activity
     * @param point
     * @return
     */
    private fun localDataHas(activity: Activity, point: PlayerPoint): Boolean {
        readLocalData(activity)
        return localPoints.contains(point)
    }

    /**
     * Reads the local storage
     *
     * @param activity
     */
    private fun readLocalData(activity: Activity) {
        val s: SharedPreferences = activity.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val json = s.getString(KEY_POINTS, "")

        val o = Gson().fromJson(json, SutokoPlayerPointsManager::class.java)
        localPoints = o?.localPoints ?: ArrayList()
    }

    /**
     * Saves the local storage
     *
     * @param activity
     */
    private fun saveLocalData(activity: Activity) {
        val json = Gson().toJson(this)
        val s: SharedPreferences = activity.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val editor = s.edit()
        editor.putString(KEY_POINTS, json).apply()
    }
}