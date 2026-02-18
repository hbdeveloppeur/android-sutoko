package fr.purpletear.friendzone4.game.activities.hacking;

import android.app.Activity;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.callback.IntroAnimationCallback;

class HackingGameModel {
    private Adapter adapter;
    private boolean isFirstStart;
    boolean isRunning = false;
    private int currentPosition;
    private boolean isEnd;

    HackingGameModel(boolean isEnd) {
        this.isEnd = isEnd;
        adapter = new Adapter();
        isFirstStart = true;
        currentPosition = 0;
    }

    /**
     * Determines if it is the End
     * @return boolean
     */
    public boolean isEnd() {
        return isEnd;
    }

    /**
     * Determines the Activity's state on FirstStart
     *
     * @return boolean
     */
    boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }

    /**
     * Returns the Activity's Adapter
     *
     * @return Adapter
     */
    Adapter getAdapter() {
        return adapter;
    }

    /**
     * Reads a chapter and save it into the chapter object
     */
    private String readFile(Activity activity) {
        try {
            InputStream inputStream = activity.getAssets().open(GameData.assetRootDir + File.separator + (isEnd ? "end.txt" : "code.txt"));
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    /**
     * Splits the file content into lines and returns it into a List
     *
     * @param activity Activity
     * @return List<String>
     */
    private List<String> parse(Activity activity) {
        String content = readFile(activity);
        return Arrays.asList(content.split("\n"));
    }

    void animate(Activity a, IntroAnimationCallback callback) {
        List<String> arr = parse(a);
        recurse(arr, currentPosition, callback);
    }

    /**
     * @param array List<String>
     * @param i     int
     */
    private void recurse(final List<String> array, final int i, final IntroAnimationCallback callBack) {
        currentPosition = i;
        if (!isEnd && i == array.size()) {
            callBack.onFinish();
            return;
        } else if(isEnd && i == array.size()) {
            return;
        } else if(currentPosition == (array.size() / 3)){
            callBack.onMiddleAnimation();
        }
        if(!isRunning) {
            return;
        }
        final Code code = new Code(array.get(i));
        Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.insert(code.getCode(i - 4));
                        callBack.onInsertPhrase(i);
                        recurse(array, i + 1, callBack);
                    }
                }, code.getTime()
        );
    }
}
