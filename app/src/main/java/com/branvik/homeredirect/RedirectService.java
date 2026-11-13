package com.branvik.homeredirect;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;

public class RedirectService extends AccessibilityService {
    private static final String DAIJISHO = "com.magneticchen.daijishou";
    private static final long SETTLE_MS = 700L;
    private static final long COOLDOWN_MS = 1200L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String stockHome;
    private String currentPackage = "";
    private String currentClass = "";
    private long lastLaunch = 0L;
    private long sequence = 0L;
    private boolean redirectInProgress = false;
    private Runnable pending;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        updateHome();
    }

    private void updateHome() {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
        stockHome = info == null || info.activityInfo == null ? null : info.activityInfo.packageName;
        if (stockHome != null && (stockHome.equals("android") || stockHome.equals(DAIJISHO) || stockHome.equals(getPackageName()))) stockHome = null;
    }

    private boolean isOverview(String className) {
        String name = className.toLowerCase(java.util.Locale.ROOT);
        return name.contains("recent") || name.contains("overview") || name.contains("taskview")
                || name.contains("taskstack") || name.contains("recents");
    }

    private void cancelPending() {
        sequence++;
        if (pending != null) handler.removeCallbacks(pending);
        pending = null;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        if (event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        String cls = event.getClassName() == null ? "" : event.getClassName().toString();
        currentPackage = pkg;
        currentClass = cls;
        cancelPending();

        if (pkg.equals(DAIJISHO)) {
            redirectInProgress = false;
            return;
        }
        if (stockHome == null) updateHome();
        if (stockHome == null || !pkg.equals(stockHome)) {
            redirectInProgress = false;
            return;
        }
        if (isOverview(cls) || redirectInProgress) return;
        if (SystemClock.elapsedRealtime() - lastLaunch < COOLDOWN_MS) return;

        // Conservative: only a Launcher-class window is eligible, not arbitrary
        // windows from the launcher package. ZUI can use the same class for Recents.
        if (!cls.toLowerCase(java.util.Locale.ROOT).contains("launcher")) return;
        final long token = sequence;
        final String expectedClass = cls;
        pending = () -> {
            pending = null;
            if (token != sequence || !stockHome.equals(currentPackage)
                    || !expectedClass.equals(currentClass) || isOverview(currentClass)
                    || redirectInProgress) return;
            Intent launch = getPackageManager().getLaunchIntentForPackage(DAIJISHO);
            if (launch == null) return;
            lastLaunch = SystemClock.elapsedRealtime();
            redirectInProgress = true;
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            try { startActivity(launch); }
            catch (RuntimeException ex) { redirectInProgress = false; }
        };
        handler.postDelayed(pending, SETTLE_MS);
    }

    @Override public void onInterrupt() { cancelPending(); }
    @Override public void onDestroy() { cancelPending(); super.onDestroy(); }
}
