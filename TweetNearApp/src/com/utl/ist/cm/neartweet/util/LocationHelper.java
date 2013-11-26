package com.utl.ist.cm.neartweet.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper
{	
    private LocationManager mLocationManager;
    private LocationResult mLocationResult;
    
    private boolean mGPSProviderEnabled = false, mNetworkProviderEnabled = false;

    public boolean start(Context context, LocationResult locationResult)
    {
        mLocationResult = locationResult;
        
        if (mLocationManager == null)
        {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        
        try
        {
        	mGPSProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        	mNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception e) {}

        if (!mGPSProviderEnabled && !mNetworkProviderEnabled)
        {
            return false;
        }
        
        if (mGPSProviderEnabled)
        {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
        }
        
        if (mNetworkProviderEnabled)
        {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
        }
        
        return true;
    }
    
    public void stop()
    {
         mLocationManager.removeUpdates(gpsLocationListener);
         mLocationManager.removeUpdates(networkLocationListener);

         Location networkLocation = null, gpsLocation = null;
         
         if (mGPSProviderEnabled)
         {
             gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         }
         
         if (mNetworkProviderEnabled)
         {
             networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         }
         
         if ((gpsLocation != null) && (networkLocation != null))
         {
             if (gpsLocation.getTime() > networkLocation.getTime())
             {
                 mLocationResult.setLocation(gpsLocation);
             }
             else
             {
                 mLocationResult.setLocation(networkLocation);
             }
             return;
         }

         if (gpsLocation != null)
         {
             mLocationResult.setLocation(gpsLocation);
             return;
         }
         
         if (networkLocation != null)
         {
             mLocationResult.setLocation(networkLocation);
             return;
         }
         
         mLocationResult.setLocation(null);
    }

    private LocationListener gpsLocationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            mLocationResult.setLocation(location);
        }
        
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private LocationListener networkLocationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            mLocationResult.setLocation(location);
        }
        
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    public static abstract class LocationResult
    {
        public abstract void setLocation(Location location);
    }
}