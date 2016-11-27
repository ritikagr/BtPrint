package com.codeflight.ritik.btprint;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.prowesspride.api.Setup;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

public class BTDiscovery extends AppCompatActivity implements View.OnClickListener {

    private ListView mlvDiscoveredDeviceList;
    private ArrayList<HashMap<String,Object>> malDeviceItemList = null;
    private SimpleAdapter msaDeviceItemAdapter = null;
    private Hashtable<String,Hashtable<String,String>> mhtFDS = null;
    private boolean _btDiscoveryFinished;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static Setup impressSetUp = null;

    public static final String EXTRA_DEVICE_TYPE = "android.bluetooth.device.extra.DEVICE_TYPE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_discovery);

        mlvDiscoveredDeviceList = (ListView) findViewById(R.id.bt_discovered_devices_list);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("Select the Printer");
        }

        try {
            impressSetUp = new Setup();
            //boolean activate = impressSetUp.blActivateLibrary(context, R.raw.licencefull_pride_gen);
            /*if (activate == true) {
                Log.d("Prow Pride Demo App", "Library Activated......");
            } else if (activate == false) {
                Log.d("Prow Pride Demo App", "Library Not Activated...");
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        mlvDiscoveredDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent resultIntent = new Intent();

                String dMac = ((TextView) view.findViewById(R.id.device_mac)).getText().toString();

                resultIntent.putExtra("MAC", dMac);
                resultIntent.putExtra("NAME", String.valueOf(mhtFDS.get(dMac).get("NAME")));
                resultIntent.putExtra("RSSI", String.valueOf(mhtFDS.get(dMac).get("RSSI")));
                resultIntent.putExtra("COD", String.valueOf(mhtFDS.get(dMac).get("COD")));
                resultIntent.putExtra("BOND", String.valueOf(mhtFDS.get(dMac).get("BOND")));

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        new scanDeviceTask().execute("");
    }

    private BroadcastReceiver _foundReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Hashtable<String,String> htDeviceInfo =  new Hashtable<String, String>();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Bundle b = intent.getExtras();
                if(device.getName() == null)
                    htDeviceInfo.put("NAME", "Null");
                else
                    htDeviceInfo.put("NAME", device.getName());

                htDeviceInfo.put("RSSI" , String.valueOf(b.get(BluetoothDevice.EXTRA_RSSI)));

                if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                    htDeviceInfo.put("BOND" , getString(R.string.actDiscovery_bond_bonded));
                else
                    htDeviceInfo.put("BOND" , getString(R.string.actDiscovery_bond_nothing));

                htDeviceInfo.put("COD" , String.valueOf(b.get(BluetoothDevice.EXTRA_CLASS)));

                String sDeviceType = String.valueOf(b.get(EXTRA_DEVICE_TYPE));
                if(!sDeviceType.equals("null"))
                    htDeviceInfo.put("DEVICE_TYPE", sDeviceType);
                else
                    htDeviceInfo.put("DEVICE_TYPE", "-1");

                mhtFDS.put(device.getAddress(),htDeviceInfo);

                //Refresh Device list
                showDevices();
            }
        }
    };

    private BroadcastReceiver _finishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                _btDiscoveryFinished = true;
                unregisterReceiver(_foundReciever);
                unregisterReceiver(_finishedReceiver);

                if(mhtFDS!=null && mhtFDS.size()>0)
                {
                    showToast(getString(R.string.actDiscovery_msg_select_device));
                }
                else
                {
                    showToast(getString(R.string.actDiscovery_msg_not_find_device));
                }
            }
        }
    };

    public void showDevices()
    {
        if(malDeviceItemList==null)
            this.malDeviceItemList = new ArrayList<HashMap<String, Object>>();

        if(msaDeviceItemAdapter==null)
            this.msaDeviceItemAdapter = new SimpleAdapter(this , malDeviceItemList , R.layout.list_view_item_device,
                    new String[]{"MAC", "NAME", "RSSI", "BOND", "COD"},
                    new int[]{R.id.device_mac, R.id.device_name, R.id.device_rssi, R.id.device_bond, R.id.device_cod});

        this.mlvDiscoveredDeviceList.setAdapter(msaDeviceItemAdapter);

        this.malDeviceItemList.clear();

        Enumeration<String> e = this.mhtFDS.keys();

        while (e.hasMoreElements())
        {
            HashMap<String,Object> map = new HashMap<String,Object>();
            String skey = e.nextElement();
            map.put("MAC", skey);
            map.put("NAME", this.mhtFDS.get(skey).get("NAME"));
            map.put("RSSI", this.mhtFDS.get(skey).get("RSSI"));
            map.put("COD", this.mhtFDS.get(skey).get("COD"));
            map.put("BOND", this.mhtFDS.get(skey).get("BOND"));

            this.malDeviceItemList.add(map);
        }

        this.msaDeviceItemAdapter.notifyDataSetChanged();
    }

    private class scanDeviceTask extends AsyncTask<String, String, Integer>
    {
        private ProgressDialog mpd = null;
        private static final int RET_BLUETOOTH_NOT_START = 0x0001;
        private static final int RET_SCAN_DEVICE_FINISHED = 0x0002;
        private static final int miWAIT_TIME = 12;
        private static final int miSLEEP_TIME = 150;

        @Override
        protected void onPreExecute() {
            this.mpd = new ProgressDialog(BTDiscovery.this);
            this.mpd.setMessage(getString(R.string.actDiscovery_msg_scaning_device));
            this.mpd.setCancelable(true);
            this.mpd.setCanceledOnTouchOutside(false);
            this.mpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    _btDiscoveryFinished = true;
                }
            });

            this.mpd.show();

            startSearch();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if(!mBluetoothAdapter.isEnabled())
                return RET_BLUETOOTH_NOT_START;

            int iWait = miWAIT_TIME * 1000;

            while (iWait > 0)
            {
                if(_btDiscoveryFinished)
                    return RET_SCAN_DEVICE_FINISHED;
                else
                    iWait -= miSLEEP_TIME;
                SystemClock.sleep(miSLEEP_TIME);
            }
            return RET_SCAN_DEVICE_FINISHED;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(this.mpd.isShowing())
                this.mpd.dismiss();

            if(mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();

            if(result == RET_SCAN_DEVICE_FINISHED)
            {
                _btDiscoveryFinished = true;
            }
            else if(result == RET_BLUETOOTH_NOT_START)
            {
                showToast(getString(R.string.actDiscovery_msg_bluetooth_not_start));
            }
        }
    }

    public void startSearch()
    {
        _btDiscoveryFinished = false;

        if(mhtFDS == null)
            this.mhtFDS = new Hashtable<String, Hashtable<String, String>>();
        else
            this.mhtFDS.clear();

        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(_foundReciever, foundFilter);

        IntentFilter finishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(_finishedReceiver, finishedFilter);

        mBluetoothAdapter.startDiscovery();
        this.showDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }

    public void showToast(String s)
    {
        Toast.makeText(BTDiscovery.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {

    }
}
