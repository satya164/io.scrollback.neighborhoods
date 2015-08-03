package io.scrollback.neighborhoods;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import io.scrollback.library.AuthStatus;
import io.scrollback.library.FollowMessage;
import io.scrollback.library.NavMessage;
import io.scrollback.library.ReadyMessage;
import io.scrollback.library.ScrollbackFragment;
import io.scrollback.library.ScrollbackMessageHandler;

public class MainActivity extends AppCompatActivity {
    ScrollbackFragment scrollbackFragment = SbFragment.getInstance();
    AreaFragment areaFragment;

    FrameLayout areaFrame;
    FrameLayout sbFrame;

    private Location lastKnownLocation;
    private boolean isLocationReceived = false;

    public static boolean appOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        areaFrame = (FrameLayout) findViewById(R.id.area_container);
        sbFrame = (FrameLayout) findViewById(R.id.scrollback_container);

        // Use network provided coarse location
        final String locationProvider = LocationManager.NETWORK_PROVIDER;

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location == null) {
                    return;
                }

                if (isLocationReceived == false && areaFragment != null) {
                    areaFragment.setLocation(location);

                    isLocationReceived = true;
                }

                lastKnownLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);

        scrollbackFragment.setGcmSenderId(getString(R.string.gcm_sender_id));

        scrollbackFragment.setMessageHandler(new ScrollbackMessageHandler() {
            @Override
            public void onNavMessage(final NavMessage message) {
                if (message != null && message.mode != null) {
                    scrollbackFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (message.mode.equals("home")) {
                                showAreaFragment();
                            } else {
                                hideAreaFragment();
                            }
                        }
                    });
                }
            }

            @Override
            public void onAuthMessage(AuthStatus message) {
            }

            @Override
            public void onFollowMessage(FollowMessage message) {
            }

            @Override
            public void onReadyMessage(ReadyMessage message) {
            }
        });

        scrollbackFragment.setPrimaryColor(getResources().getColor(R.color.primary), getResources().getColor(R.color.primary_dark));

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();

        if (intent.hasExtra("scrollback_path")) {
            scrollbackFragment.loadPath(getIntent().getStringExtra("scrollback_path"));
        } else if (Intent.ACTION_VIEW.equals(action) && uri != null) {
            scrollbackFragment.loadUrl(uri.toString());
        } else {
            showAreaFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.scrollback_container, scrollbackFragment)
                .commit();
    }

    public void showAreaFragment() {
        if (areaFrame.getVisibility() == View.VISIBLE && areaFragment != null) {
            return;
        }

        getSupportActionBar().show();

        areaFragment = AreaFragment.newInstance();

        if (lastKnownLocation != null) {
            areaFragment.setLocation(lastKnownLocation);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.area_container, areaFragment)
                .commit();

        areaFrame.setVisibility(View.VISIBLE);
        sbFrame.setVisibility(View.INVISIBLE);
    }

    public void hideAreaFragment() {
        if (areaFrame.getVisibility() == View.INVISIBLE && areaFragment == null) {
            return;
        }

        // If you want to customize view animations (like material reveal) look here:
        // https://developer.android.com/training/material/animations.html#Reveal
        getSupportActionBar().hide();

        if (areaFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(areaFragment)
                    .commit();

            areaFragment = null;
        }

        areaFrame.setVisibility(View.INVISIBLE);
        sbFrame.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && areaFrame.getVisibility() == View.VISIBLE) {
            finish();

            return true;
        }

        boolean handled = scrollbackFragment.onKeyDown(keyCode, event);

        if (!handled) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();

                return true;
            }

            return super.onKeyDown(keyCode, event);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        appOpen = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        appOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        appOpen = false;
    }
}
