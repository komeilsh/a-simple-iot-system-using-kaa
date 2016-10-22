/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.cellmonitor;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.*;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.demo.cellmonitor.event.CellLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.AccelerationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.GpsLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.LogSent;
import org.kaaproject.kaa.demo.cellmonitor.event.SignalStrengthChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import de.greenrobot.event.EventBus;

import java.lang.Override;
// komeil
import java.util.*;
import android.hardware.SensorEventListener;
import org.kaaproject.kaa.demo.cellmonitor.profile.CellMonitorProfile;

/**
 * The implementation of the base {@link Application} class. Performs initialization of the
 * application resources including initialization of the Kaa client. Handles the Kaa client lifecycle.
 * Implements and registers a listener to monitor a mobile cell location and the signal strength.
 * Implements and registers a listener to monitor a phone gps location.
 * Sends cell monitor log records to the Kaa cluster via the Kaa client.
 */
public class CellMonitorApplication extends Application {

    private static final Logger LOG = LoggerFactory
            .getLogger(CellMonitorApplication.class);

    public static final int MAX_PARALLEL_UPLOADS = 10;
    public static final int TIMEOUT_PERIOD = 100;
    public static final int UPLOAD_CHECK_PERIOD = 30;
    public static final int UNDEFINED = -1;

    private EventBus mEventBus;
    private TelephonyManager mTelephonyManager;
    private CellMonitorPhoneStateListener mCellMonitorPhoneStateListener;
    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    private LocationManager mLocationManager;
    private GpsLocationListener mGpsLocationListener;
    private Location mGpsLocation;

    // komeil
    private AccelerometerClass mAccelerometer;

    private int mSentLogCount;
    private long mLastLogTime;

    private KaaClient mClient;
    private boolean mKaaStarted;

    // added by komeil
    public CellMonitorProfile profile;

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new EventBus();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mCellMonitorPhoneStateListener = new CellMonitorPhoneStateListener();
        mLocationManager = (LocationManager)  getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);
        mGpsLocation = mLocationManager.getLastKnownLocation(bestProvider);
        mGpsLocationListener = new GpsLocationListener();
        //komeil
        mAccelerometer = new AccelerometerClass();

        /*
         * Initialize the Kaa client using the Android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(
                this);
        mClient = Kaa.newClient(kaaClientContext,
                new SimpleKaaClientStateListener() {

                    /*
                     * Implement the onStarted callback to get notified as soon as
                     * the Kaa client is operational.
                     */
                    @Override
                    public void onStarted() {
                        mKaaStarted = true;
                        LOG.info("Kaa client started");
                    }
                });

        String phoneId = "";
        phoneId = mTelephonyManager.getDeviceId();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (phoneId != null) {
            profile = new CellMonitorProfile(phoneId,null,String.valueOf(currentapiVersion),null);
        }
        mClient.setProfileContainer(new ProfileContainer() {
            @Override
            public CellMonitorProfile getProfile() {
                return profile;
            }
        });


        /*
         * Define a log upload strategy used by the Kaa client for logs delivery.
         */
        mClient.setLogUploadStrategy(new LogUploadStrategy() {

            @Override
            public void onTimeout(LogFailoverCommand logFailoverCommand) {
                LOG.error("Unable to send logs within defined timeout!");
            }

            @Override
            public void onFailure(LogFailoverCommand logFailoverCommand, LogDeliveryErrorCode logDeliveryErrorCode) {
                LOG.error("Unable to send logs, error code: " + logDeliveryErrorCode);
                logFailoverCommand.retryLogUpload(10);
            }

            @Override
            public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus logStorageStatus) {
                return logStorageStatus.getRecordCount() > 0 ?
                        LogUploadStrategyDecision.UPLOAD : LogUploadStrategyDecision.NOOP;
            }

            @Override
            public int getMaxParallelUploads() {
                return MAX_PARALLEL_UPLOADS;
            }

            @Override
            public int getTimeout() {
                return TIMEOUT_PERIOD;
            }


            @Override
            public int getUploadCheckPeriod() {
                return UPLOAD_CHECK_PERIOD;
            }
        });

         /*
         * Setting callback for logs delivery.
         */
        mClient.setLogDeliveryListener(new LogDeliveryListener() {
            @Override
            public void onLogDeliverySuccess(BucketInfo bucketInfo) {
                LOG.trace("Log with bucketId: " + bucketInfo.getBucketId() + " was successfully uploaded");
            }

            @Override
            public void onLogDeliveryFailure(BucketInfo bucketInfo) {
                LOG.error("Unable to send log with bucketId " + bucketInfo.getBucketId() + " because failure");
            }

            @Override
            public void onLogDeliveryTimeout(BucketInfo bucketInfo) {
                LOG.error("Unable to send log with bucketId " + bucketInfo.getBucketId() + " within defined timeout");
            }
        });

        /*
         * Start the Kaa client workflow.
         */
        mClient.start();


    }

    public void pause() {
        mTelephonyManager.listen(mCellMonitorPhoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        mLocationManager.removeUpdates(mGpsLocationListener);

        //komeil accelerometer
        mAccelerometer.onPause();
        
        /*
         * Suspend the Kaa client. Release all network connections and application
         * resources. Suspend all the Kaa client tasks.
         */
        mClient.pause();
    }

    public void resume() {
        mTelephonyManager.listen(mCellMonitorPhoneStateListener,
                PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);
        mLocationManager.requestLocationUpdates(bestProvider, 0, 0, mGpsLocationListener);

        //komeil accelerometer
        mAccelerometer.onResume();
        
        /*
         * Resume the Kaa client. Restore the Kaa client workflow. Resume all the Kaa client
         * tasks.
         */
        mClient.resume();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        /*
         * Stop the Kaa client. Release all network connections and application
         * resources. Shut down all the Kaa client tasks.
         */
        mClient.stop();
        mKaaStarted = false;
    }

    private void sendLog() {
        if (mKaaStarted) {

            mSentLogCount++;
            mLastLogTime = System.currentTimeMillis();

            /*
             * Create an instance of a cell monitor log record and populate it with the latest values.
             */
            CellMonitorLogV3 cellMonitorLog = new CellMonitorLogV3();
            cellMonitorLog.setLogTime(mLastLogTime);
            String networkOperator = mTelephonyManager.getNetworkOperator();
            if (networkOperator == null || networkOperator.isEmpty()) {
                cellMonitorLog.setNetworkOperatorCode(UNDEFINED);
            } else {
                cellMonitorLog.setNetworkOperatorCode(Integer.valueOf(mTelephonyManager.getNetworkOperator()));
            }
            cellMonitorLog.setNetworkOperatorName(mTelephonyManager.getNetworkOperatorName());

            int cid = UNDEFINED;
            int lac = UNDEFINED;

            if (mCellLocation != null && mCellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation)mCellLocation;
                cid = gsmCellLocation.getCid();
                lac = gsmCellLocation.getLac();
            }

            cellMonitorLog.setGsmCellId(cid);
            cellMonitorLog.setGsmLac(lac);

            int gsmSignalStrength = UNDEFINED;

            if (mSignalStrength != null) {
                gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
            }
            cellMonitorLog.setSignalStrength(gsmSignalStrength);

            org.kaaproject.kaa.demo.cellmonitor.Location phoneLocation =
                    new org.kaaproject.kaa.demo.cellmonitor.Location();
            if (mGpsLocation != null) {
                phoneLocation.setLatitude(mGpsLocation.getLatitude());
                phoneLocation.setLongitude(mGpsLocation.getLongitude());
            }
            cellMonitorLog.setPhoneGpsLocation(phoneLocation);


            /*added by komeil*/
            String phoneId = "";
            phoneId = mTelephonyManager.getDeviceId();
            if (phoneId != null) {
                cellMonitorLog.setPhoneId(mTelephonyManager.getDeviceId());
            }

            ArrayList<String> test = new ArrayList<String>();
            test.add(Float.toString(mAccelerometer.deltaX));
            test.add(Float.toString(mAccelerometer.deltaY));
            test.add(Float.toString(mAccelerometer.deltaZ));
            test.add(getTempAndHumidity());
            cellMonitorLog.setArrayField(test);
            cellMonitorLog.setOtherInfo("nothing");

            
            /*
             * Pass a cell monitor log record to the Kaa client. The Kaa client will upload 
             * the log record according to the defined log upload strategy. 
             */
            mClient.addLogRecord(cellMonitorLog);

            mEventBus.post(new LogSent());
        }
    }

    public EventBus getEventBus() {
        return mEventBus;
    }

    public TelephonyManager getTelephonyManager() {
        return mTelephonyManager;
    }

    public CellLocation getCellLocation() {
        return mCellLocation;
    }

    public SignalStrength getSignalStrength() {
        return mSignalStrength;
    }

    public Location getGpsLocation() {
        return mGpsLocation;
    }

    //added by komeil
    public float getAccelerationX() {return mAccelerometer.deltaX; }
    public String getAccelerationY() {
        return Float.toString(mAccelerometer.deltaY);
    }
    public String getAccelerationZ() {
        return Float.toString(mAccelerometer.deltaZ);
    }

    public String getTempAndHumidity () {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter.isEnabled()) {
        return CellMonitorFragment.tempAndHumidityData;}
        else{
            return "N/A";
        }
    }



    public int getSentLogCount() {
        return mSentLogCount;
    }

    public long getLastLogTime() {
        return mLastLogTime;
    }



    // komeil
/*
    public void updateProfilePassword(String pass) {

        String phoneId = "";
        phoneId = mTelephonyManager.getDeviceId();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (phoneId != null) {
            profile = new CellMonitorProfile(phoneId,pass,String.valueOf(currentapiVersion),null);
        }
        //profileSet = false;
        // Sample profile
        //profile = new CellMonitorProfile("notset","notset","notset","notset");
        /*if (profileSet == false) {
            // Simple implementation of ProfileContainer interface that is provided by the SDK
            mClient.setProfileContainer(new ProfileContainer() {
                @Override
                public CellMonitorProfile getProfile() {
                    return profile;
                }
            });
        } else {
            profile.setPassword(pass);
            mClient.updateProfile();
        }//
    }
*/
    private class CellMonitorPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            mCellLocation = location;
            sendLog();
            mEventBus.post(new CellLocationChanged());
            LOG.info("Cell location changed!");
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength;
            sendLog();
            mEventBus.post(new SignalStrengthChanged());
            LOG.info("Signal strength changed!");
        }
    }

    private class GpsLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            mGpsLocation = location;
            sendLog();
            mEventBus.post(new GpsLocationChanged());
            LOG.info("GPS location changed!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}


        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}

    }


    //komeil
    public class AccelerometerClass implements SensorEventListener {

        private float lastX, lastY, lastZ;
        private SensorManager sensorManager;
        private Sensor accelerometer;
        public float deltaX = 0;
        public float deltaY = 0;
        public float deltaZ = 0;


        AccelerometerClass(){

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                // success! we have an accelerometer
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                //vibrateThreshold = accelerometer.getMaximumRange() / 2;
            } else {
                // fail! we dont have an accelerometer!
            }

        }

        //onResume() register the accelerometer for listening the events
        protected void onResume() {
            //super.onResume();
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        //onPause() unregister the accelerometer for stop listening the events
        protected void onPause() {
            //super.onPause();
            sensorManager.unregisterListener(this);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }


        @Override
        public void onSensorChanged(SensorEvent event) {

            // get the change of the x,y,z values of the accelerometer
            deltaX = Math.abs(lastX - event.values[0]);
            deltaY = Math.abs(lastY - event.values[1]);
            deltaZ = Math.abs(lastZ - event.values[2]);

            // if the change is below 2, it is just plain noise
            if (deltaX < 2)
                deltaX = 0;
            if (deltaY < 2)
                deltaY = 0;
            if (deltaZ < 2)
                deltaZ = 0;

            // set the last know values of x,y,z
            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];

            //vibrate();
            if(deltaX != 0 | deltaY!=0 | deltaZ != 0) {
                sendLog();
                mEventBus.post(new AccelerationChanged());
                //LOG.info("Acceleration changed!");
            } else{
                mEventBus.post(new AccelerationChanged());
                //LOG.info("Acceleration changed!");
            }

            }


        }

    }


