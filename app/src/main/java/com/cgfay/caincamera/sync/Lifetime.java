package com.cgfay.caincamera.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cain.huang on 2017/10/30.
 */

public class Lifetime implements SafeCloseable {

    private final Lifetime mParent;
    private final Object mLock;
    private final Set<SafeCloseable> mCloseables;
    private boolean mClosed;

    public Lifetime() {
        mLock = new Object();
        mCloseables = new HashSet<SafeCloseable>();
        mParent = null;
        mClosed = false;
    }


    public Lifetime(Lifetime parent) {
        mLock = new Object();
        mCloseables = new HashSet<SafeCloseable>();
        mParent = parent;
        mClosed = false;
        parent.mCloseables.add(this);
    }

    public <T extends SafeCloseable> T add(T closeable) {
        boolean needToClose = false;
        synchronized (mLock) {
            if (mClosed) {
                needToClose = true;
            } else {
                mCloseables.add(closeable);
            }
        }
        if (needToClose) {
            closeable.close();
        }
        return closeable;
    }

    @Override
    public void close() {
        List<SafeCloseable> toClose = new ArrayList<SafeCloseable>();
        synchronized (mLock) {
            if (mClosed) {
                return;
            }
            mClosed = true;
            if (mParent != null) {
                mParent.mCloseables.remove(this);
            }
            toClose.addAll(mCloseables);
        }
        for (SafeCloseable closeable : toClose) {
            closeable.close();
        }
    }
}
