package com.cgfay.media;

public class CainMediaRecorder {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("media_recorder");
        native_init();
    }

    public CainMediaRecorder() {

    }

    /**
     * Call it when one is done with the object. This method releases the memory
     * allocated internally.
     */
    public native void release();
    private static native void native_init();
    private native void native_setup();

    private native final void native_finalize();
    @Override
    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
