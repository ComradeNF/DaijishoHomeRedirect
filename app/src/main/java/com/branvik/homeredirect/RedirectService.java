package com.branvik.homeredirect;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
public class RedirectService extends AccessibilityService {
 private static final String DAIJISHO = "com.magneticchen.daijishou";
 private String stockHome;
 private long lastLaunch;
 private boolean wasOnHome;
 @Override public void onServiceConnected() { super.onServiceConnected(); updateHome(); }
 private void updateHome() {
  Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
  ResolveInfo resolved = getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
  stockHome = resolved == null || resolved.activityInfo == null ? null : resolved.activityInfo.packageName;
  if (stockHome != null && (stockHome.equals(getPackageName()) || stockHome.equals(DAIJISHO) || stockHome.equals("android"))) stockHome = null;
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent event) {
  if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getEventType() != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return;
  if (event.getPackageName() == null) return;
  String pkg = event.getPackageName().toString();
  if (stockHome == null) { updateHome(); if (stockHome == null) return; }
  if (!pkg.equals(stockHome)) { if (!pkg.equals("com.android.systemui")) wasOnHome = false; return; }
  // Stock launcher events can also represent overview; this remains experimental.
  if (wasOnHome) return;
  long now = SystemClock.elapsedRealtime();
  if (now - lastLaunch < 1500) return;
  Intent launch = getPackageManager().getLaunchIntentForPackage(DAIJISHO);
  if (launch == null) return;
  wasOnHome = true;
  lastLaunch = now;
  launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
  try { startActivity(launch); } catch (RuntimeException ex) { wasOnHome = false; }
 }
 @Override public void onInterrupt() { }
}
