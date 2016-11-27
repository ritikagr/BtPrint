package com.codeflight.ritik.btprint.bluetooth;

/**
 * Created by ritik on 11/27/2016.
 */
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
/**
 * Private API calls Tools Bluetooth interface
 * */

public class BluetoothPair{
    /**Constants: Bluetooth pairing binding filter listener name*/
    static public final String PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    /**
     * Pairing Bluetooth devices
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Log.e("BlueTooth Control", "<<<<<<<<<<<<<<createBond>>>>>>>>>>>>>>");
        Class<? extends BluetoothDevice> btClass = btDevice.getClass();
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * Lift the Bluetooth device pairing
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(BluetoothDevice btDevice)
            throws Exception
    {
        Log.e("BlueTooth Control", "<<<<<<<<<<<<<<removeBond>>>>>>>>>>>>>>");
        Class<? extends BluetoothDevice> btClass = btDevice.getClass();
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * Setting a passcode
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean setPin(BluetoothDevice btDevice, String str)
            throws Exception
    {
        Log.e("BlueTooth Control", "<<<<<<<<<<<<<<setPin>>>>>>>>>>>>>> "+str);
        Boolean returnValue = false;
        try{
            Class<? extends BluetoothDevice> btClass = btDevice.getClass();
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[] { byte[].class });
//        	byte[] ar = new byte[]{0x31,0x32,0x33,0x34};
//        	Method removeBondMethod =
//        		btClass.getMethod("setPin",new Class[]{Array.newInstance(byte.class,4).getClass()});
//            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, ar);
            returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[] { str.getBytes() });
            Log.d("returnValue", ">>setPin:" + returnValue.toString());
        }catch (SecurityException e){
            // throw new RuntimeException(e.getMessage());
            Log.e("returnValue", ">>setPin:" + e.getMessage());
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            // throw new RuntimeException(e.getMessage());
            Log.e("returnValue", ">>setPin:" + e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            Log.e("returnValue", ">>setPin:" + e.getMessage());
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    /**
     * cancel Pairing User Input
     * @see android.permission.WRITE_SECURE_SETTINGS<br/>
     *      Permission is only granted to system apps
     * */
    static public boolean cancelPairingUserInput(BluetoothDevice btDevice)
            throws Exception
    {
        Log.e("BlueTooth Control", "<<<<<<<<<<<<<<cancelPairingUserInput>>>>>>>>>>>>>>");
        Class<? extends BluetoothDevice> btClass = btDevice.getClass();
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * cancel Bond Process
     * @see android.permission.WRITE_SETTINGS*/

    static public boolean cancelBondProcess(BluetoothDevice btDevice)
            throws Exception
    {
        Log.e("BlueTooth Control", "<<<<<<<<<<<<<<cancelBondProcess>>>>>>>>>>>>>>");
        Boolean returnValue = false;
        try
        {
            Class<? extends BluetoothDevice> btClass = btDevice.getClass();
            Method createBondMethod = btClass.getMethod("cancelBondProcess");
            returnValue = (Boolean) createBondMethod.invoke(btDevice);
        }
        catch (SecurityException e)
        {
            // throw new RuntimeException(e.getMessage());
            Log.e("returnValue", ">>cancelBondProcess:" + e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // throw new RuntimeException(e.getMessage());
            Log.e("returnValue", ">>cancelBondProcess:" + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Log.e("returnValue", ">>cancelBondProcess:" + e.getMessage());
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }
}

