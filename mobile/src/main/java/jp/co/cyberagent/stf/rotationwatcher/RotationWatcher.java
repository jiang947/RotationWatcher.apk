package jp.co.cyberagent.stf.rotationwatcher;

import android.os.RemoteException;
import android.view.IRotationWatcher;

public class RotationWatcher extends Thread {
    private static final String TAG = "RotationWatcher";

    // Get an IWindowManager using private APIs.

    private WindowManagerCompat wm = new WindowManagerCompat();

    @Override
    public void run() {
        IRotationWatcher.Stub watcher = new IRotationWatcher.Stub() {
            @Override
            public void onRotationChanged(int rotation) throws RemoteException {
                report(rotation);
            }
        };
        try {
            // Get the rotation we have right now.
            report(wm.getRotation());
            // Watch for changes in rotation.

            wm.watchRotation(watcher);

            // Just keep waiting.
            synchronized (this) {
                while (!isInterrupted()) {
                    wait();
                }
            }
        } catch (RemoteException ignore) {
        } catch (InterruptedException ignore) {
        } finally {
            // Sadly, wm.removeRotationWatcher() is only available on API >= 18. Instead, we
            // must make sure that whole process dies, causing DeathRecipient to reap the
            // watcher.
            try {
                wm.removeRotationWatcher(watcher);
            } catch (RemoteException e) {
                // No-op
            }
        }
    }

    private synchronized void report(int rotation) {
        // The internal values are very convenient, we can simply multiply by 90 to get the
        // actual degree.
        System.out.println(rotation * 90);
    }

    public static void main(String[] args) {
        try {
            RotationWatcher monitor = new RotationWatcher();
            monitor.start();
            monitor.join();
        } catch (InterruptedException ignore) {
        }
    }
}
