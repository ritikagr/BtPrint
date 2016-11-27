package com.codeflight.ritik.btprint;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.codeflight.ritik.btprint.bluetooth.BluetoothPair;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    private Switch enable_bt;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERY_BT = 2;

    private Hashtable<String, String> mhtDeviceInfo = null;
    private LinearLayout mllSelectedDeviceInfo;
    private Button mbtPair;
    private Button mbtConnect;
    private TextView mtvDeviceInfo;

    private BluetoothDevice mbDevice = null;
    private boolean mblBonded = false;
    private GlobalPool mGp = null;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enable_bt = (Switch) findViewById(R.id.enable_bt_switch);

        mllSelectedDeviceInfo = (LinearLayout) findViewById(R.id.selected_device);
        mtvDeviceInfo = (TextView) findViewById(R.id.device_info);
        mbtPair = (Button) findViewById(R.id.btPair);
        mbtConnect = (Button) findViewById(R.id.btConnect);
        this.mGp = new GlobalPool();

        mhtDeviceInfo = new Hashtable<String, String>();

        if(mBluetoothAdapter.isEnabled())
            enable_bt.setChecked(true);

        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                startBluetoothDeviceTask(compoundButton,b);
            }
        });

    }

    public void startBluetoothDeviceTask(CompoundButton compoundButton,boolean b)
    {

        if(b)
        {
            if(mBluetoothAdapter!=null)
            {
                if(!mBluetoothAdapter.isEnabled())
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                }
            }
        }
        else
        {
            mBluetoothAdapter.disable();
            compoundButton.setChecked(false);
        }
    }

    public void scanBluetoothDeviceTask(View v)
    {
        if(mBluetoothAdapter==null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter.isEnabled())
        {
            Intent btDiscoveryIntent = new Intent(MainActivity.this, BTDiscovery.class);
            startActivityForResult(btDiscoveryIntent, REQUEST_DISCOVERY_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_ENABLE_BT)
        {
            if (resultCode==RESULT_OK)
            {
                enable_bt.setChecked(true);
            }
            else if(resultCode==RESULT_CANCELED)
            {
                enable_bt.setChecked(false);
            }
        }
        else if(requestCode == REQUEST_DISCOVERY_BT)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if(mhtDeviceInfo==null)
                    mhtDeviceInfo = new Hashtable<String, String>();

                this.mhtDeviceInfo.put("MAC", data.getStringExtra("MAC"));
                this.mhtDeviceInfo.put("NAME", data.getStringExtra("NAME"));
                this.mhtDeviceInfo.put("RSSI", data.getStringExtra("RSSI"));
                this.mhtDeviceInfo.put("COD", data.getStringExtra("COD"));
                this.mhtDeviceInfo.put("BOND", data.getStringExtra("BOND"));

                this.mllSelectedDeviceInfo.setVisibility(View.VISIBLE);

                this.mtvDeviceInfo.setText(this.mhtDeviceInfo.get("NAME"));

                if(!this.mhtDeviceInfo.get("BOND").equals(getString(R.string.actDiscovery_bond_nothing)))
                {
                    this.mbtConnect.setEnabled(true);
                    this.mbtConnect.setVisibility(View.VISIBLE);
                    this.mbtPair.setVisibility(View.GONE);
                }
                else
                {
                    mbDevice = mBluetoothAdapter.getRemoteDevice(this.mhtDeviceInfo.get("MAC"));

                    this.mbtPair.setEnabled(true);
                    this.mbtPair.setVisibility(View.VISIBLE);
                    this.mbtConnect.setVisibility(View.GONE);

                    //new Settings(this).savePrinterMacAddress(this.mhtDeviceInfo.get("MAC"));
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickBtnPair(View v)
    {
        new PairTask().execute(this.mhtDeviceInfo.get("MAC"));
        mbtPair.setEnabled(false);
    }

    public void onClickBtnConnect(View v)
    {
        new ConnSocketTask().execute(this.mhtDeviceInfo.get("MAC"));
        mbtConnect.setEnabled(false);
    }

    BroadcastReceiver _mPairingRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mblBonded = (device.getBondState() == BluetoothDevice.BOND_BONDED);
            }
        }
    };

    private class PairTask extends AsyncTask<String,String,Integer>
    {
        private static final int RET_BOND_OK = 0x00;
        private static final int RET_BOND_FAIL = 0x01;

        private static final int iTimeOut = 1000*15;

        @Override
        protected void onPreExecute() {
            showToast("Pairing...");
            IntentFilter pairingRequestFilter = new IntentFilter(BluetoothPair.PAIRING_REQUEST);
            IntentFilter bondStateChangedFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

            registerReceiver(_mPairingRequest, pairingRequestFilter);
            registerReceiver(_mPairingRequest, bondStateChangedFilter);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            final int iStepTime = 150;
            int iWait = iTimeOut;

            try{
                mbDevice = mBluetoothAdapter.getRemoteDevice(strings[0]);
                BluetoothPair.createBond(mbDevice);
                mblBonded = false;
            } catch (Exception e) {
                Log.d(getString(R.string.app_name), "create bond failed");
                e.printStackTrace();
                return RET_BOND_FAIL;
            }

            while(!mblBonded && iWait>0)
            {
                SystemClock.sleep(iStepTime);
                iWait-=iStepTime;
            }

            if(iWait>0)
            {
                Log.e("Application", "create Bond successful! RET_BOND_OK ");
            }
            else {
                Log.e("Application", "create Bond failed! RET-BOND_FAIL");
            }

            return (iWait>0) ? RET_BOND_OK : RET_BOND_FAIL;
        }

        @Override
        protected void onPostExecute(Integer result) {
            unregisterReceiver(_mPairingRequest);

            if(RET_BOND_OK == result)
            {
                showToast("Bluetooth Bonding Successful!");

                mbtPair.setVisibility(View.GONE);
                mbtConnect.setVisibility(View.VISIBLE);
                mhtDeviceInfo.put("BOND", getString(R.string.actDiscovery_bond_bonded));

            }
            else {
                showToast("Bluetooth Bonding Failed!");

                try{
                    BluetoothPair.removeBond(mbDevice);
                } catch (Exception e) {
                    Log.d(getString(R.string.app_name), "RemoveBond Failed!");
                    e.printStackTrace();
                }

                mbtPair.setEnabled(true);
                //new ConnSocketTask().execute(mbDevice.getAddress());
            }
        }
    }

    private class ConnSocketTask extends AsyncTask<String,String,Integer>{
        private static final int CONN_FAIL = 0x01;
        private static final int CONN_SUCCESS = 0x02;
        private ProgressDialog mpd = null;

        @Override
        protected void onPreExecute() {
            this.mpd = new ProgressDialog(MainActivity.this);
            this.mpd.setMessage("Connecting to Device...");
            this.mpd.setCancelable(false);
            this.mpd.setCanceledOnTouchOutside(false);
            this.mpd.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if(mGp.createConn(strings[0]))
            {
                return CONN_SUCCESS;
            }
            else
                return CONN_FAIL;
        }

        @Override
        protected void onPostExecute(Integer result) {
            this.mpd.dismiss();

            if(CONN_SUCCESS == result)
            {
                mbtConnect.setVisibility(View.GONE);
                showToast("Bluetooth Connection Established Successfuly!");
            }
            else {
                mbtConnect.setEnabled(true);
                showToast("Bluetooth Connection Failed!");
            }
        }
    }

    public void showToast(String s)
    {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}
