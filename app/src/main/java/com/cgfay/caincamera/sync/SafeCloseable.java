package com.cgfay.caincamera.sync;

/**
 * Created by cain.huang on 2017/10/30.
 */

public interface SafeCloseable extends AutoCloseable {

    @Override
    void close();
}
