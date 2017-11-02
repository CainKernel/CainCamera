package com.cgfay.caincamera.sync;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * handler 工厂
 * Created by cain.huang on 2017/10/30.
 */

public class HandlerFactory {

    public Handler create(Lifetime lifetime, String threadName) {
        final HandlerThread thread = new HandlerThread(threadName);
        thread.start();
        lifetime.add(new SafeCloseable() {
            @Override
            public void close() {
                thread.quitSafely();
            }
        });
        return new Handler(thread.getLooper());
    }

    public Handler create(Lifetime lifetime, String threadName, int javaThreadPriority) {
        final HandlerThread thread = new HandlerThread(threadName);
        thread.start();
        thread.setPriority(javaThreadPriority);

        lifetime.add(new SafeCloseable() {
            @Override
            public void close() {
                thread.quitSafely();
            }
        });

        return new Handler(thread.getLooper());
    }
}
