package jp.co.cyberagent.stf.rotationwatcher;

import android.content.Context;
import android.os.Build;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IRotationWatcher;
import android.view.IWindowManager;

public class WindowManagerCompat {

    private final IInterface manager;

    public WindowManagerCompat() {
        manager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
    }


    public int getRotation() throws RemoteException {
        if (Build.VERSION.SDK_INT < 26) {
            return ((IWindowManager) manager).getRotation();
        } else {
            return getRotation26();
        }
    }

    private int getRotation26() {
        try {
            Class<?> cls = manager.getClass();
            try {
                return (Integer) manager.getClass().getMethod("getRotation").invoke(manager);
            } catch (NoSuchMethodException e) {
                // method changed since this commit:
                // https://android.googlesource.com/platform/frameworks/base/+/8ee7285128c3843401d4c4d0412cd66e86ba49e3%5E%21/#F2
                return (Integer) cls.getMethod("getDefaultDisplayRotation").invoke(manager);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    public void watchRotation(IRotationWatcher iRotationWatcher) throws RemoteException {
        if (Build.VERSION.SDK_INT < 26) {
            ((IWindowManager) manager).watchRotation(iRotationWatcher);
        } else {
            watchRotation26(iRotationWatcher);
        }
    }


    private void watchRotation26(IRotationWatcher iRotationWatcher) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("watchRotation", IRotationWatcher.class).invoke(manager, iRotationWatcher);
            } catch (NoSuchMethodException e) {
                // display parameter added since this commit:
                // https://android.googlesource.com/platform/frameworks/base/+/35fa3c26adcb5f6577849fd0df5228b1f67cf2c6%5E%21/#F1
                cls.getMethod("watchRotation", IRotationWatcher.class, int.class).invoke(manager, iRotationWatcher, 0);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException {
        if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 26) {
            try {
                ((IWindowManager) manager).removeRotationWatcher(watcher);
            } catch (RemoteException e) {
                // No-op
            }
        }
    }

}
