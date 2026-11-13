package com.branvik.homeredirect;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  LinearLayout layout = new LinearLayout(this);
  layout.setOrientation(LinearLayout.VERTICAL);
  int p = (int)(24 * getResources().getDisplayMetrics().density);
  layout.setPadding(p,p,p,p);
  TextView info = new TextView(this);
  info.setText("Daijisho Home Redirect — test 0.1\n\n1. Set Lenovo stock launcher as the default Home app.\n2. Enable Daijisho Home Redirect under Accessibility.\n3. Press Home to test.\n\nIf Recents stops working, disable this service.\n\nNo network access or screen-content reading.");
  info.setTextSize(17);
  layout.addView(info);
  Button button = new Button(this);
  button.setText("Open Accessibility settings");
  button.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  layout.addView(button);
  setContentView(layout);
 }
}
