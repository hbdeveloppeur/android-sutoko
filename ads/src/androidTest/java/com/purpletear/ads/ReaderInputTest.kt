package com.purpletear.ads

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderInputTest {
    @Test fun aKeyPressCountsOnceNotOnceForDownAndAgainForUpOrRepeat() {
        assertTrue(isDeliberateReaderInput(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)))
        assertFalse(isDeliberateReaderInput(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)))
        assertFalse(isDeliberateReaderInput(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP)))
        assertTrue(isDeliberateReaderInput(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)))
        assertFalse(isDeliberateReaderInput(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 1)))
    }
    @Test fun aTouchGestureCountsOnlyItsFirstDownEvent() {
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN)) {
            val event = MotionEvent.obtain(0, 0, action, 10f, 10f, 0)
            try { assertEquals(action == MotionEvent.ACTION_DOWN, isDeliberateReaderInput(event)) }
            finally { event.recycle() }
        }
    }
}
