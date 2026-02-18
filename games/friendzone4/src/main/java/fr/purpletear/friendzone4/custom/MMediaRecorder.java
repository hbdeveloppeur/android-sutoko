package fr.purpletear.friendzone4.custom;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;

public class MMediaRecorder extends MediaRecorder {
    public int seconds;
    public boolean isValid;
    public boolean isRecording;
    public File file;


    public MMediaRecorder(File file) {
        this.seconds = -1;
        this.isValid = true;
        this.isRecording = false;
        this.file = file;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        isRecording = true;
    }

    @Override
    public void stop() throws IllegalStateException {
        isRecording = false;
        super.stop();
    }

    public void removeFile() {
        if(hasFile() && file.exists() && !file.delete()) {
            Log.e("Purpleteardebug", "Could'nt delete file");
        } else {
            Log.e("Purpleteardebug", "Removing file");
        }
    }

    private boolean hasFile() {
        return file != null;
    }
}
