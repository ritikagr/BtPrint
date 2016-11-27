package com.codeflight.ritik.btprint;

/**
 * Created by ritik on 11/27/2016.
 */
import android.app.Application;

import com.codeflight.ritik.btprint.bluetooth.BluetoothComm;

public class GlobalPool extends Application{

    /**Bluetooth communication connection object*/
    public BluetoothComm mBTcomm = null;
    public boolean connection = false;

    @Override
    public void onCreate(){
        super.onCreate();
    }
    /**
     * Set up a Bluetooth connection
     * @param String sMac Bluetooth hardware address
     * @return Boolean
     * */
    public boolean createConn(String sMac){
        if (null == this.mBTcomm)
        {
            this.mBTcomm = new BluetoothComm(sMac);
            if (this.mBTcomm.createConn()){
                connection = true;
                return true;
            }
            else{
                this.mBTcomm = null;
                connection = false;
                return false;
            }
        }
        else
            return true;
    }

    /**
     * Close and release the connection
     * @return void
     * */
    public void closeConn(){
        if (null != this.mBTcomm){
            this.mBTcomm.closeConn();
            this.mBTcomm = null;
        }
    }
}
