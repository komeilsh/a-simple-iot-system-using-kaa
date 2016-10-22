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

import static org.kaaproject.kaa.demo.cellmonitor.CellMonitorApplication.UNDEFINED;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.kaaproject.kaa.demo.cellmonitor.event.AccelerationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.CellLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.GpsLocationChanged;
import org.kaaproject.kaa.demo.cellmonitor.event.LogSent;
import org.kaaproject.kaa.demo.cellmonitor.event.SignalStrengthChanged;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.telephony.CellLocation;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// komeil for bluetooth
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



/**
 * The implementation of the {@link Fragment} class.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 * Provides a view with the information about current GSM cell location, signal strength and phone GPS location.
 * Displays current statistics about logs sent to the Kaa cluster.   
 */
public class CellMonitorFragment extends Fragment {

    private CellMonitorActivity mActivity;
    private CellMonitorApplication mApplication;
    private ActionBar mActionBar;
    
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private Calendar mCalendar = Calendar.getInstance();
    
    private TextView mNetworkOperatorValue;
    private TextView mNetworkOperatorNameValue;
    private TextView mGsmCellIdValue;
    private TextView mGsmLacValue;
    private TextView mGsmSignalStrengthValue;
    private TextView mGpsLocationValue;
    private TextView mLastLogTimeValue;
    private TextView mSentLogCountValue;
    private TextView mPhoneIdValue;
    private TextView mAccelerationX;
    private TextView mAccelerationY;
    private TextView mAccelerationZ;
    //komeil bluetooth
    private TextView myLabel;

    private static final Logger LOG = LoggerFactory
            .getLogger(CellMonitorApplication.class);
    private static final String TAG = "MyActivity";

    // komeil for bluetooth
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    final byte delimiter = 33;
    int readBufferPosition = 0;
    public static String tempAndHumidityData = "";

    public void sendBtMsg(String msg2send){
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {
            LOG.info("bluetoothk Tried to create socket!");

            Log.i(TAG, "sendBtMsg: Tried to create socket!");
            /*
            while (!mBluetoothAdapter.isEnabled())
            {
                final Handler h = new Handler();
                final int delay = 5000; //milliseconds

                h.postDelayed(new Runnable(){
                    public void run(){

                        h.postDelayed(this, delay);
                    }
                }, delay);
            }*/
            if (!mBluetoothAdapter.isEnabled())
            {
                return;
            }
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

            if (!mmSocket.isConnected()){
                mmSocket.connect();
            }

            String msg = msg2send;
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    final Handler handler = new Handler();

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    final class workerThread implements Runnable {

        private String btMsg;

        public workerThread(String msg) {
            btMsg = msg;
        }

        public void run() {
            if (mmDevice != null) {
                sendBtMsg(btMsg);
                if (mmSocket.isConnected()) {
                    while (!Thread.currentThread().isInterrupted()) {
                        int bytesAvailable;
                        boolean workDone = false;
                        try {
                            final InputStream mmInputStream;
                            mmInputStream = mmSocket.getInputStream();
                            bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                Log.e("Aquarium recv bt", "bytes available");
                                byte[] readBuffer = new byte[1024];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        tempAndHumidityData = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        //The variable data now contains our full command
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(tempAndHumidityData);
                                            }
                                        });

                                        workDone = true;
                                        break;

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }

                                if (workDone == true) {
                                    mmSocket.close();
                                    break;
                                }

                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            else{
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                LOG.info("bluetoothk searching for paired devices!");
                if (pairedDevices.size() > 0) {
                    LOG.info("bluetoothk found some paired devices!");
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                        {
                            Log.e("Aquarium", device.getName());
                            LOG.info("bluetoothk now mmDevice is being assigned!");
                            Log.i(TAG, "now mmDevice is being assigned log.i!");
                            mmDevice = device;
                            break;
                        }
                    }
                }
            }
        }

    };



    public CellMonitorFragment() {
        super();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cell_monitor, container,
                false);
        
        mNetworkOperatorValue = (TextView) rootView.findViewById(R.id.networkOperatorValue);
        mNetworkOperatorNameValue = (TextView) rootView.findViewById(R.id.networkOperatorNameValue);
        mGsmCellIdValue = (TextView) rootView.findViewById(R.id.gsmCellIdValue);
        mGsmLacValue = (TextView) rootView.findViewById(R.id.gsmLacValue);
        mGsmSignalStrengthValue = (TextView) rootView.findViewById(R.id.gsmSignalStrengthValue);
        mGpsLocationValue = (TextView) rootView.findViewById(R.id.gpsLocationValue);
        mLastLogTimeValue = (TextView) rootView.findViewById(R.id.lastLogTimeValue);
        mSentLogCountValue = (TextView) rootView.findViewById(R.id.logsSentValue);
        mPhoneIdValue = (TextView) rootView.findViewById(R.id.phoneIdValue);
        mAccelerationX = (TextView) rootView.findViewById(R.id.accelerationXValue);
        mAccelerationY = (TextView) rootView.findViewById(R.id.accelerationYValue);
        mAccelerationZ = (TextView) rootView.findViewById(R.id.accelerationZValue);
        //komeil bluetooth
        myLabel = (TextView) rootView.findViewById(R.id.btResult);




/*
        final Button tempButton = (Button) rootView.findViewById(R.id.tempButton);
        // start temp button handler
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on temp button click

                (new Thread(new workerThread("temp"))).start();

            }
        });
*/

        if(!mBluetoothAdapter.isEnabled())
        {
            //Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBluetooth, 0);
            mBluetoothAdapter.enable();
            LOG.info("bluetoothk is turning on!");
        }
/*
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            LOG.info("bluetoothk searching for paired devices!");
            if (pairedDevices.size() > 0) {
                LOG.info("bluetoothk found some paired devices!");
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                    {
                        Log.e("Aquarium", device.getName());
                        LOG.info("bluetoothk now mmDevice is being assigned!");
                        Log.i(TAG, "now mmDevice is being assigned log.i!");
                        mmDevice = device;
                        break;
                    }
                }
            }*/
        //if(mBluetoothAdapter.isEnabled()) {
            final Handler h = new Handler();
            final int delay = 10000; //milliseconds
            LOG.info("bluetoothk now in timing!");
            h.postDelayed(new Runnable() {
                public void run() {
                    if (mBluetoothAdapter.isEnabled()) {
                        updateTempAndHumidity();
                    }
                    h.postDelayed(this, delay);
                }
            }, delay);
        //}
    //end of komeil bluetooth
        updateAllView();
        Log.i(TAG, "end of on create log.i!");
        return rootView;
    }
    
    private void updateAllView() {
        String networkOperator = mApplication.getTelephonyManager().getNetworkOperator();
        if (networkOperator != null) {
            mNetworkOperatorValue.setText(networkOperator);
        } else {
            mNetworkOperatorValue.setText(R.string.unavailable);
        }
        String networkOperatorName = mApplication.getTelephonyManager().getNetworkOperatorName();
        if (networkOperatorName != null) {
            mNetworkOperatorNameValue.setText(networkOperatorName);
        } else {
            mNetworkOperatorNameValue.setText(R.string.unavailable);
        }


        /*added by komeil*/
        String phoneId = "";
        phoneId = mApplication.getTelephonyManager().getDeviceId();
        if (phoneId != null) {
            mPhoneIdValue.setText(phoneId);
        }else {
            mPhoneIdValue.setText(R.string.unavailable);
        }

        
        updateGsmCellLocation();
        updateGsmSignalStrength();
        updateGpsLocation();
        // komeil
        updateAcceleration();
        //updateTempAndHumidity();
        updateSentLogs();
    }
    
    private void updateGsmCellLocation() {
        int cid = UNDEFINED;
        int lac = UNDEFINED;
        CellLocation cellLocation = mApplication.getCellLocation();
        if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
            cid = gsmCellLocation.getCid();
            lac = gsmCellLocation.getLac();
        }
        if (cid != UNDEFINED) {
            mGsmCellIdValue.setText(String.valueOf(cid));
        } else {
            mGsmCellIdValue.setText(R.string.unavailable);
        }
        if (lac != UNDEFINED) {
            mGsmLacValue.setText(String.valueOf(lac));
        } else {
            mGsmLacValue.setText(R.string.unavailable);
        }
    }
    
    private void updateGsmSignalStrength() {
        int gsmSignalStrength = UNDEFINED;
        SignalStrength signalStrength = mApplication.getSignalStrength();
        if (signalStrength != null) {
            gsmSignalStrength = signalStrength.getGsmSignalStrength();
        }
        if (gsmSignalStrength != UNDEFINED) {
            mGsmSignalStrengthValue.setText(String.valueOf(gsmSignalStrength));
        } else {
            mGsmSignalStrengthValue.setText(R.string.unavailable);
        }
    }
    
    private void updateGpsLocation() {
        Location gpsLocation = mApplication.getGpsLocation();
        if (gpsLocation != null) {
            double latitude = gpsLocation.getLatitude();
            double longitude = gpsLocation.getLongitude();
            mGpsLocationValue.setText(String.valueOf(latitude) + ", " + longitude);
        } else {
            mGpsLocationValue.setText(R.string.unavailable);
        }
    }

    //added by komeil
    private void updateAcceleration() {
        //LOG.info("Update Acceleration changed!");
        float  accelerationX = mApplication. getAccelerationX();
        String  accelerationY = mApplication.getAccelerationY();
        String  accelerationZ = mApplication.getAccelerationZ();
        //int test = 0;
        //test = test +1;
       // if (accelerationX != 0 && accelerationY != null && accelerationZ != null) {
            mAccelerationX.setText(String.valueOf(accelerationX));
            mAccelerationY.setText(String.valueOf(accelerationY));
            mAccelerationZ.setText(accelerationZ);
    //    }
        //} else {
         //   mGpsLocationValue.setText(R.string.unavailable);
        //}
    }

        public void updateTempAndHumidity(){
            LOG.info("Request for TempAndHumidity!");
            (new Thread(new workerThread("temp"))).start();
        }



    
    private void updateSentLogs() {
        long lastLogTime = mApplication.getLastLogTime();
        if (lastLogTime > 0) {
            mCalendar.setTimeInMillis(lastLogTime);
            mLastLogTimeValue.setText(mDateFormat.format(mCalendar.getTime()));
        } else {
            mLastLogTimeValue.setText(R.string.unavailable);
        }
        mSentLogCountValue.setText(String.valueOf(mApplication.getSentLogCount()));
    }
    
    public void onEventMainThread(CellLocationChanged cellLocationChanged) {
        updateGsmCellLocation();
    }

    public void onEventMainThread(AccelerationChanged accelerationChanged) {
        updateAcceleration();
    }
    
    public void onEventMainThread(SignalStrengthChanged signalStrengthChanged) {
        updateGsmSignalStrength();
    }
    
    public void onEventMainThread(GpsLocationChanged gpsLocationChanged) {
        updateGpsLocation();
    }
    
    public void onEventMainThread(LogSent logSent) {
        updateSentLogs();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (CellMonitorActivity) activity;
            mActionBar = mActivity.getSupportActionBar();
            mApplication = mActivity.getCellMonitorApplication();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mActionBar != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setTitle(getText(R.string.cell_monitor_title));
            mActionBar.setDisplayShowTitleEnabled(true);
        }
        if (!mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().register(this);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().unregister(this);
        }
    }
    
}
