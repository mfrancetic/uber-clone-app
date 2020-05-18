/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class MainActivity extends AppCompatActivity {

    private SwitchCompat riderDriverSwitch;
    private Button getStartedButton;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userType = getString(R.string.rider);
        checkIfUserLoggedIn();

        setUpClickListeners();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void setUpClickListeners() {
        riderDriverSwitch = findViewById(R.id.rider_driver_switch);
        getStartedButton = findViewById(R.id.get_started_button);

        riderDriverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    userType = getString(R.string.driver);
                } else {
                    userType = getString(R.string.rider);
                }
            }
        });

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.getCurrentUser().put(Constants.USER_TYPE_KEY, userType);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            if (userType.equals(getString(R.string.rider))) {
                               goToRiderActivity();
                            } else {
                                goToDriverActivity();
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    private void checkIfUserLoggedIn() {
        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        Toast.makeText(MainActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (ParseUser.getCurrentUser().getString(Constants.USER_TYPE_KEY) != null) {
                userType = ParseUser.getCurrentUser().getString(Constants.USER_TYPE_KEY);
                if (userType != null && userType.equals(getString(R.string.rider))) {
                    goToRiderActivity();
                } else if (userType != null && userType.equals(getString(R.string.driver))) {
                    goToDriverActivity();
                }
            }
        }
    }

    private void goToRiderActivity() {
        Intent goToRiderActivityIntent = new Intent(MainActivity.this, RiderActivity.class);
        startActivity(goToRiderActivityIntent);
    }

    private void goToDriverActivity() {
        Intent goToDriverActivityIntent = new Intent(MainActivity.this, DriverNearbyRequestsActivity.class);
        startActivity(goToDriverActivityIntent);
    }
}