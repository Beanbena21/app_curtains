package com.example.test7;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity2 extends AppCompatActivity {
    public static final int Access_Location = 1;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    ListView lvShow, lvScan;
    ProgressBar pbScan;

    private ArrayAdapter<String> listAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        lvShow = findViewById(R.id.dsshow);
        lvScan = findViewById(R.id.dsscan);
        pbScan = findViewById(R.id.progresBar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayList = new ArrayList<>();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lvScan.setAdapter(listAdapter);
        lvScan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Log.d("MainActivity", "Trying to pair with " + address);
                    arrayList.get(i).createBond();
                }
            }
        });
        checkCoarseLocationPermisson();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(devicesFoundReceiver);
    }

    private boolean checkCoarseLocationPermisson(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Access_Location);
            return  false;
        }
        else{
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case Access_Location:
                if(grantResults.length > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Access coarse location allowed. You can scan bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Access coarse location forbidden. You can't scan bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    arrayList.add(device);
                    listAdapter.add(device.getName() + "\n" + device.getAddress());
                    listAdapter.notifyDataSetChanged();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                pbScan.setVisibility(View.GONE);
                Toast.makeText(MainActivity2.this, "Đã dò xong", Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Toast.makeText(MainActivity2.this,"Đang dò thiết bị...", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private AdapterView.OnItemClickListener myListShow = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent itent = new Intent(MainActivity2.this, MainActivity3.class);

            //Change the activity.
            itent.putExtra(EXTRA_DEVICE_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(itent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_findbluetooth, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_paired_devices:
                pairedDevices();
                return true;
            case R.id.menu_scan_devices:
                scanDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void pairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị Bluetooth ghép nối.", Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(MainActivity2.this, android.R.layout.simple_list_item_1, list);
        lvShow.setAdapter(adapter);
        lvShow.setOnItemClickListener(myListShow);
    }

    private void scanDevices(){
        pbScan.setVisibility(View.VISIBLE);
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
            if(checkCoarseLocationPermisson()){
                listAdapter.clear();
                if(bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
            }
        }
    }
}