package com.ethanzone.hpdisplay;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get preferences
        SharedPreferences prefs = getSharedPreferences("com.ethanzone.hpdisplay", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        // Update switches
        Button active = findViewById(R.id.toggle);;
        Switch deletefrombar = findViewById(R.id.deletefrombar);
        deletefrombar.setChecked(prefs.getBoolean("deletefrombar", false));

        // Set listeners
        deletefrombar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("deletefrombar", isChecked);
            editor.apply();
        });

        active.setOnClickListener((compoundButton) -> {

            if (!MainActivity.this.isNotificationServiceEnabled(MainActivity.this)) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("We need permission...")
                        .setMessage("To want permission to read notifications, so we can show them in the display. On the next screen, find this app, and allow the permission.")

                        .setPositiveButton("Continue", (dialog, which) -> {

                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("Back", null)
                        .show();

            } else {
                // Toast to show that the app has permissions
                Toast.makeText(MainActivity.this, "You've already given this app permissions!", Toast.LENGTH_SHORT).show();
            }

        });


    }


    // Check if we have notification access

    private boolean isNotificationServiceEnabled(Context c) {
        String pkgName = c.getPackageName();
        final String flat = Settings.Secure.getString(c.getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}