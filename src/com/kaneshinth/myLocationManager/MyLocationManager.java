package com.kaneshinth.myLocationManager;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

public class MyLocationManager {

    private class LocationDriver {

        public Context          context;
        public Location         location;
        public LocationManager  manager;
        public LocationListener listener;
        public String           provider;

        public LocationDriver(Context ctx, String provider) {
            this.context = ctx;
            this.location = null;
            this.manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            final LocationDriver self = this;
            this.listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        self.location = location;
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
            this.provider = provider;
        }

        public boolean hasLocation() {
            return this.location != null;
        }

        public boolean isProviderEnabled() {
            return this.manager.isProviderEnabled(provider);
        }

        public void requestLocationUpdates() {
            if (this.isValid()) {
                try {
                    this.manager.requestLocationUpdates(this.provider, 0, 0, this.listener);
                } catch (Exception e) {
                }
            }
        }

        public boolean isValid() {
            return this.manager != null;
        }

        public void invalidate() {
            if (this.isValid()) {
                try {
                    this.manager.removeUpdates(this.listener);
                } catch (Exception e) {
                }
            }
            this.location = null;
            this.manager = null;
        }

        public void validate() {
            if (!this.isValid()) {
                this.manager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
            }
        }

    }

    private LocationDriver mGPSLocationDriver;
    private LocationDriver mNetworkLocationDriver;

    public MyLocationManager(Context ctx) {
        mGPSLocationDriver = new LocationDriver(ctx, LocationManager.GPS_PROVIDER);
        mNetworkLocationDriver = new LocationDriver(ctx, LocationManager.NETWORK_PROVIDER);
    }

    public void requestLocationUpdates() {

        if (!mGPSLocationDriver.isValid()) {
            mGPSLocationDriver.validate();
        }
        if (!mNetworkLocationDriver.isValid()) {
            mNetworkLocationDriver.validate();
        }

        if (!mGPSLocationDriver.isProviderEnabled()) {
            mGPSLocationDriver.invalidate();
        }
        if (!mNetworkLocationDriver.isProviderEnabled()) {
            mNetworkLocationDriver.invalidate();
        }

        mGPSLocationDriver.requestLocationUpdates();
        mNetworkLocationDriver.requestLocationUpdates();

        Handler handler = new Handler();
        final long delayed = 20000;
        handler.postDelayed(gpsRunnable, delayed);
        handler.postDelayed(networkRunnable, delayed);
    }

    private void removeUpdates() {
        mGPSLocationDriver.invalidate();
        mNetworkLocationDriver.invalidate();
    }

    private Runnable gpsRunnable = new Runnable() {
        @Override
        public void run() {
            if (mNetworkLocationDriver.hasLocation()) {
                mGPSLocationDriver.invalidate();
            }
        }
    };

    private Runnable networkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mGPSLocationDriver.hasLocation()) {
                mNetworkLocationDriver.invalidate();
            }
        }
    };

}
