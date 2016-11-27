package com.codeflight.ritik.btprint.bluetooth;

/**
 * Created by ritik on 11/24/2016.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.R.string;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

/**
 * Use of this class, you need to have the following two permissions <br />
   * & Lt; uses-permission android: name = "android.permission.BLUETOOTH" / & gt; <br />
   * & Lt; uses-permission android: name = "android.permission.BLUETOOTH_ADMIN" / & gt; <br />
   * Android supported versions LEVEL 4 or more, and LEVEL 17 support bluetooth 4 of ble equipment
 * */
@SuppressLint("NewApi")
public class BluetoothComm{
    /**Service UUID*8ce255c0-200a-11e0-ac64-0800200c9a66*/
    public final static String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    //public final static String UUID_STR = "8ce255c0-200a-11e0-ac64-0800200c9a66";
    /**Bluetooth address code*/
    private String msMAC;
    /**Bluetooth connection status*/
    private boolean mbConectOk = false;

    /* Get Default Adapter */
    private BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
    /**Bluetooth serial port connection object*/
    private BluetoothSocket mbsSocket = null;
    /** Input stream object */
    public static InputStream misIn = null;
    /** Output stream object */
    public static OutputStream mosOut = null;
    /**Constant: The current Adnroid SDK version number*/
    private static final int SDK_VER;
    static{
        SDK_VER = Build.VERSION.SDK_INT;
    };

    /**
     * Constructor
     * @param sMAC Bluetooth device MAC address required to connect
     * */
    public BluetoothComm(String sMAC){
        this.msMAC = sMAC;
    }

    /**
     * Disconnect the Bluetooth device connection
     * @return void
     * */
    public void closeConn(){
        if ( this.mbConectOk ){
            try{
                if (null != this.misIn)
                    this.misIn.close();
                if (null != this.mosOut)
                    this.mosOut.close();
                if (null != this.mbsSocket)
                    this.mbsSocket.close();
                this.mbConectOk = false;//Mark the connection has been closed
            }catch (IOException e){
                //Any part of the error, will be forced to close socket connection
                this.misIn = null;
                this.mosOut = null;
                this.mbsSocket = null;
                this.mbConectOk = false;//Mark the connection has been closed
            }
        }
        Log.e(TAG, " Closed connection");
    }
    private static final String TAG = "Prowess BT Comm";
    /**
     * Bluetooth devices establish serial communication connection <br />
     * This function is best to put the thread to call, because it will block the system when calling
     * @return Boolean false: connection creation failed / true: the connection is created successfully
     * */
    final public boolean createConn(){
        if (! mBT.isEnabled())
            return false;
        Log.e(TAG,".....create connection  1");
        //If a connection already exists, disconnect
        if (mbConectOk)
            this.closeConn();
        Log.e(TAG,".....create connection  1");
		/*Start Connecting a Bluetooth device*/
        final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.msMAC);
        final UUID uuidComm = UUID.fromString(UUID_STR);
        try{

            this.mbsSocket = device.createRfcommSocketToServiceRecord(uuidComm);
            Thread.sleep(2000);
            Log.i(TAG, ">>> Connecting ");
            this.mbsSocket.connect();
            Log.i(TAG, ">>> CONNECTED SUCCESSFULLY");
            Thread.sleep(2000);
            this.mosOut = this.mbsSocket.getOutputStream();//Get global output stream object
            this.misIn = this.mbsSocket.getInputStream(); //Get global streaming input object
            this.mbConectOk = true; //Device is connected successfully

        }catch (Exception e){
            try {
                Thread.sleep(2000);
                Log.i(TAG, ">>>>>>           Try 2  ................!");
                this.mbsSocket = device.createInsecureRfcommSocketToServiceRecord(uuidComm);
                Log.i(TAG, " Socket obtained");
                Thread.sleep(2000);
                Log.i(TAG, " Connecting again ");
                this.mbsSocket.connect();
                Log.i(TAG, " Successful connection 2nd time....... ");
                Thread.sleep(2000);
                this.mosOut = this.mbsSocket.getOutputStream();//Get global output stream object
                this.misIn = this.mbsSocket.getInputStream(); //Get global streaming input object
                this.mbConectOk = true;
            } catch (IOException e1) {
                try {
                    Log.i(TAG,"trying fallback...");

                    this.mbsSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,2);
                    this.mbsSocket.connect();

                    Log.i(TAG,"Connected");
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                    this.closeConn();//Disconnect
                    Log.e(TAG, " Returning False");
                    return false;
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                    this.closeConn();//Disconnect
                    Log.e(TAG, " Returning False");
                    return false;
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                    this.closeConn();//Disconnect
                    Log.e(TAG, " Returning False");
                    return false;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    this.closeConn();//Disconnect
                    Log.e(TAG, " Returning False");
                    return false;
                }
            }
            catch (Exception ee){
                Log.i(TAG, " Connection Failed due to other reasons....... ");
                ee.printStackTrace();
                this.closeConn();//Disconnect
                Log.e(TAG, " Returning False");
                return false;
            }
        }
        return true;
    }

    /**
     * If the communication device has been established
     * @return Boolean true: communication has been established / false: communication lost
     * */
    public boolean isConnect()	{
        return this.mbConectOk;
    }


}

