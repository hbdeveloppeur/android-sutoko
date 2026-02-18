/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package purpletear.fr.purpleteartools;

import android.os.Handler;

import java.util.ArrayList;

public class MemoryHandler {
    private ArrayList<CallBackHR> arrayOfCallBacks = new ArrayList<>();

    public void kill() {
        for (CallBackHR callBack : arrayOfCallBacks) {
            callBack.setDone();
            callBack.handler.removeCallbacks(callBack.runnable);
        }
        arrayOfCallBacks.clear();
    }

    public void kill(String name) {
        boolean found = false;
        int position = 0;
        for (CallBackHR callBack : arrayOfCallBacks) {
            if (callBack.getRunnable().getName().equals(name)) {
                callBack.setDone();
                callBack.handler.removeCallbacks(callBack.runnable);
                found = true;
                break;
            }
            position++;
        }

        if (found) {
            arrayOfCallBacks.remove(position);
        }
    }

    public void removeAll(String name) {
        for(;;) {
            boolean found = false;
            int position = 0;
            for (CallBackHR callBack : arrayOfCallBacks) {
                if (callBack.getRunnable().getName().equals(name)) {
                    callBack.setDone();
                    callBack.handler.removeCallbacks(callBack.runnable);
                    found = true;
                    break;
                }
                position++;
            }
            if(found) {
                arrayOfCallBacks.remove(position);
            } else {
                break;
            }
        }
    }

    public boolean has(String name) {
        for (CallBackHR callBack : arrayOfCallBacks) {
            if (callBack.getRunnable().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void push(Runnable2 runnable) {
        arrayOfCallBacks.add(new CallBackHR(new Handler(), runnable));
    }

    public void run(Runnable2 runnable2) {
        final CallBackHR callBackHR = findByRunnable(runnable2);
        final Runnable2 runnable = callBackHR.runnable;
        if (runnable.getDuration() > 0) {
            callBackHR.handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if(callBackHR.isDone()) {
                                return;
                            }
                            callBackHR.setDone();
                            runnable.run();
                        }
                    },
                    runnable.getDuration()
            );
        } else {
            callBackHR.handler.post(runnable);
            callBackHR.setDone();
        }
    }

    private CallBackHR findByRunnable(Runnable2 runnable) {

        for (CallBackHR callBack : arrayOfCallBacks) {
            if (callBack.runnable.equals(runnable) && !callBack.isDone()) {
                return callBack;
            }
        }
        throw new IllegalArgumentException("Handler called but not found");
    }


    class CallBackHR {
        Handler handler;
        Runnable2 runnable;
        boolean done;

        CallBackHR(Handler h, Runnable2 runnable) {
            this.handler = h;
            this.runnable = runnable;
            done = false;
        }

        public Handler getHandler() {
            return handler;
        }

        public Runnable2 getRunnable() {
            return runnable;
        }

        private void setDone() {
            done = true;
        }

        private boolean isDone() {
            return done;
        }
    }
}

