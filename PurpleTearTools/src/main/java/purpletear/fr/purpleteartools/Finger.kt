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
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

class Finger {
    companion object {

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
         * Defines the event onTouch and the action to run when it get fired.
         * @param v the view
         * @param c the context
         * @param r the action
         */
        fun defineOnTouch(v: View?, c: Context, r: Runnable) {
            if (v == null)
                return
            v.setOnClickListener { Handler(c.mainLooper).post(r) }
            v.setOnTouchListener { v1, event ->
                if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_UP)
                    v1.performClick()
                v1.performClick()
                true
            }
        }


        /**
         * Defines the event onTouch and the action to run when it get fired.
         * @param v the view
         * @param r the action
         */
        fun defineOnTouch(v: View?, r: Runnable?) {
            if (v == null) return
            v.setOnTouchListener(View.OnTouchListener { v, event ->
                if (isFingerIn(
                        event,
                        v
                    ) && event.action == MotionEvent.ACTION_UP
                )
                    if(r != null) {
                        Handler(Looper.getMainLooper()).post(r)
                    }
                true
            })
        }


        /**
         * Defines the event onTouch and the action to run when it get fired.
         * @param v the view
         * @param c the context
         * @param r the action
         */
        fun defineOnTouch(v: View?, c: Context, f: () -> Unit) {
            if (v == null)
                return
            v.setOnClickListener { Handler(c.mainLooper).post(f) }
            v.setOnTouchListener { v1, event ->
                if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_UP)
                    v1.performClick()
                true
            }
        }

        /**
         * Defines the event onTouch and the action to run when it get fired.
         * @param v the view
         * @param r the action
         */
        fun defineOnLongTouch(v: View?, r: Runnable, onSimpleTouch: Runnable, duration: Long) {
            if (v == null)
                return

            v.setOnTouchListener(object : View.OnTouchListener {
                internal var thread: Thread? = null
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (isFingerIn(event, v) && event.action == MotionEvent.ACTION_DOWN) {
                        thread = Thread(Runnable {
                            try {
                                Thread.sleep(duration)
                                Handler(Looper.getMainLooper()).post(r)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        })
                        thread!!.start()
                        return true
                    } else if (event.action == MotionEvent.ACTION_UP) {
                        if (thread!!.isAlive) {
                            Handler().post(onSimpleTouch)
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

        /**
         * Disables the listener
         * @param a: Activity
         * @param id: Int
         */
        fun disableListener(a: Activity, id: Int) {
            defineOnTouch(
                a.findViewById(id),
                a
            ) {}
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
            v.setOnTouchListener { v1, event ->
                if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_DOWN) {
                    Handler(c.mainLooper).post(`in`)
                } else if (isFingerIn(event, v1) && event.action == MotionEvent.ACTION_UP) {
                    Handler(c.mainLooper).post(up)
                } else if (!isFingerIn(event, v1)) {
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
        fun registerListener(a : Activity, id : Int, f : () -> Unit) {
            defineOnTouch(
                    a.findViewById(id),
                    a,
                    f
            )
        }
    }
}