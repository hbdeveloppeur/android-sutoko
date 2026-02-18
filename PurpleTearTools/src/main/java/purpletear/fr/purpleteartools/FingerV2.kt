/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */
package purpletear.fr.purpleteartools

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

object FingerV2 {

    private var rect: Rect? = null

    /**
     * Determines if the user finger is out of the rect or not.
     */
    fun isFingerIn(event: MotionEvent, v: View): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rect = Rect(v.left, v.top, v.right, v.bottom)
                v.getHitRect(rect)
                return true
            }
            else -> return rect!!.contains(v.left + event.x.toInt(), v.top + event.y.toInt())
        }
    }

    /**
     * Defines the event onTouch and the ACTION to run when it get fired.
     * @param v the view
     * @param c the context
     * @param r the ACTION
     */
    fun defineOnTouch(v: View?, c: Context, r: Runnable) {
        if (v == null)
            return
        v.setOnClickListener { Handler(c.mainLooper).post(r) }
        v.setOnTouchListener { v, event ->
            if (isFingerIn(event, v) && event.action == MotionEvent.ACTION_UP)
                v.performClick()
            true
        }
    }

    /**
     * Defines the event onTouch and the ACTION to run when it get fired.
     * @param v the view
     * @param c the context
     * @param r the ACTION
     */
    fun defineOnTouch(v: View?, c: Context, f: () -> Unit) {
        if (v == null)
            return
        v.setOnTouchListener { v1, event ->
            if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_UP) {
                v1.isPressed = true
                v1.performClick()
                Handler(c.mainLooper).post(f)
            }
            true
        }
    }

    /**
     * Defines the event onTouch and the ACTION to run when it get fired.
     * @param v the view
     * @param c the context
     * @param r the ACTION
     */
    fun defineOnTouch(
        v: View?,
        c: Context,
        f: () -> Unit,
        onActionDown: () -> Unit,
        onActionCancel: () -> Unit
    ) {
        if (v == null)
            return
        v.setOnTouchListener { v1, event ->
            if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_DOWN) {
                Handler(c.mainLooper).post(onActionDown)
            }
            if (event.action == MotionEvent.ACTION_CANCEL) {
                Handler(c.mainLooper).post(onActionCancel)
            }
            if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_UP) {
                v1.isPressed = true
                v1.performClick()
                Handler(c.mainLooper).post(f)
            }
            true
        }
    }

    /**
     * Defines the event onTouch and the ACTION to run when it get fired.
     * @param v the view
     * @param onLongTouch the ACTION
     */
    fun registerTimedTouch(
        v: View?,
        onLongTouch: (() -> Unit)?,
        onSimpleTouch: (() -> Unit)?,
        duration: Long
    ) {
        if (v == null)
            return

        v.setOnTouchListener(object : View.OnTouchListener {
            var thread: Thread? = null
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (isFingerIn(event, v) && event.action == MotionEvent.ACTION_DOWN) {
                    thread = Thread(Runnable {
                        try {
                            Thread.sleep(duration)
                            if (onLongTouch != null) {
                                // v.performLongClick()
                                Handler(Looper.getMainLooper()).post(onLongTouch)
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    })
                    thread!!.start()
                    return true
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    return false
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (thread!!.isAlive) {
                        if (onSimpleTouch != null) {
                            v.performClick()
                            Handler(Looper.getMainLooper()).post(onSimpleTouch)
                        }
                    }
                    thread!!.interrupt()
                    thread = null
                    return false
                } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    if (thread != null && thread!!.isAlive) {
                        thread!!.interrupt()
                        thread = null
                    }
                    return false
                }
                return true
            }
        })
    }

    fun registerTouchOnRecyclerView(v: View?, c: Context, onTouched: () -> Unit) {
        if (v == null)
            return
        /**
         * Max allowed duration for a "click", in milliseconds.
         */
        val MAX_CLICK_DURATION = 1000

        /**
         * Max allowed distance to move during a "click", in DP.
         */
        val MAX_CLICK_DISTANCE = 15

        var pressStartTime: Long = 0
        var pressedX = 0f
        var pressedY = 0f

        v.setOnTouchListener { v1, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressStartTime = System.currentTimeMillis()
                    pressedX = e.x
                    pressedY = e.y
                }
                MotionEvent.ACTION_MOVE -> {
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP -> {
                    val pressDuration =
                        System.currentTimeMillis() - pressStartTime
                    if (pressDuration < MAX_CLICK_DURATION && distance(
                            pressedX,
                            pressedY,
                            e.x,
                            e.y, c.resources
                        ) < MAX_CLICK_DISTANCE
                    ) {
                        v1.isPressed = true
                        v1.performClick()
                        Handler(Looper.getMainLooper()).post(onTouched)
                    }
                    return@setOnTouchListener false

                }
            }
            return@setOnTouchListener true
        }
    }

    private fun distance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float, resources: Resources
    ): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        val distanceInPx =
            Math.sqrt(dx * dx + dy * dy.toDouble()).toFloat()
        return pxToDp(distanceInPx, resources)
    }

    private fun pxToDp(px: Float, resources: Resources): Float {
        return px / resources.displayMetrics.density
    }


    /**
     * Defines the event onTouch with more abilities
     * @param v View
     * @param c Context
     * @param in Action to run when the user press in
     * @param up Action to run when the user release the button
     * @param out Action to run when the user move his finger out
     */
    fun defineOnTouch(v: View?, c: Context, `in`: Runnable, up: Runnable, out: Runnable) {
        if (v == null)
            return
        v.setOnTouchListener { v, event ->
            if (isFingerIn(event, v) && event.action == MotionEvent.ACTION_DOWN) {
                Handler(c.mainLooper).post(`in`)
            } else if (isFingerIn(event, v) && event.action == MotionEvent.ACTION_UP) {
                Handler(c.mainLooper).post(up)
            } else if (!isFingerIn(event, v)) {
                Handler(c.mainLooper).post(out)
            }
            true
        }
    }

    /**
     * Registers listener
     * @param a : Activity
     * @param id : Int
     * @param f : () -> Unit
     */
    fun register(a: Activity, id: Int, f: () -> Unit) {
        defineOnTouch(
            a.findViewById(id),
            a,
            f
        )
    }

    /**
     * Registers listener
     * @param a : Activity
     * @param id : Int
     * @param f : () -> Unit
     */
    fun register(view: View, f: () -> Unit, onActionDown: () -> Unit, onActionCancel: () -> Unit) {
        defineOnTouch(
            view,
            view.context,
            f, onActionDown, onActionCancel
        )
    }

    /**
     * Registers listener
     * @param a : Activity
     * @param id : Int
     * @param f : () -> Unit
     */
    fun register(root: View, id: Int? = null, f: () -> Unit) {
        if (null == id) {
            defineOnTouch(
                root,
                root.context,
                f
            )
            return
        }
        defineOnTouch(
            root.findViewById(id),
            root.context,
            f
        )
    }

    /**
     * Registers fast touch
     * @param activity : Activity
     * @param id : Int
     */
    private fun registerFastTouch(
        view: View,
        onPressedDown: (() -> Unit)?,
        onActionUp: () -> Unit
    ) {
        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (onPressedDown != null) {
                    onPressedDown()
                }
            }
            if (isFingerIn(event, v) && event.action == MotionEvent.ACTION_UP) {
                onActionUp()
            }
            return@setOnTouchListener true
        }
    }

    /**
     *
     * @param view
     * @param maxMsForATouch
     * @param onClicked
     */
    fun registerTimedTouch(view: View, maxMsForATouch: Int = 280, onClicked: () -> Unit) {
        var ms: Long = System.currentTimeMillis()
        registerFastTouch(view, {
            ms = System.currentTimeMillis()
        }, {
            if (System.currentTimeMillis() - ms < maxMsForATouch) {
                Handler(Looper.getMainLooper()).post(onClicked)
            }
        })
    }
}