package com.example.test7;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity3 extends AppCompatActivity {
    ImageButton ibRight, ibLeft, ibStop;
    Button btDisconnect, btManual, btAuto;
    TextView tvText;

    private ProgressDialog progress;
    private String address = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private boolean isBTconnected= false;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public void anhxa(){
        ibLeft = findViewById(R.id.left);
        ibRight = findViewById(R.id.right);
        ibStop = findViewById(R.id.stop);
        btDisconnect = findViewById(R.id.disconnect);
        tvText = findViewById(R.id.text);
        btManual = findViewById(R.id.manual);
        btAuto = findViewById(R.id.auto);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity2.EXTRA_DEVICE_ADDRESS);

        anhxa();
        new ConnectBT().execute();

        ibLeft.setEnabled(false);
        ibRight.setEnabled(false);
        ibStop.setEnabled(false);
        btManual.setEnabled(true);
        btAuto.setEnabled(true);

        ibRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket!=null)
                {
                    try
                    {
                        bluetoothSocket.getOutputStream().write("1".getBytes());
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
            }
        });

        ibLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket!=null)
                {
                    try
                    {
                        bluetoothSocket.getOutputStream().write("2".getBytes());
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket!=null)
                {
                    try
                    {
                        bluetoothSocket.getOutputStream().write("3".getBytes());
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
            }
        });

        btDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket!=null) //If the btSocket is busy
                {
                    try
                    {
                        bluetoothSocket.close(); //close connection
                    }
                    catch (IOException e)
                    { msg("Error");}
                }
                finish(); //return to the first layout
            }
        });

        btManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket!=null)
                {
                    try
                    {
                        bluetoothSocket.getOutputStream().write("A".getBytes());
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
                ibLeft.setEnabled(true);
                ibRight.setEnabled(true);
                ibStop.setEnabled(true);
                btManual.setEnabled(false);
                btAuto.setEnabled(true);
            }
        });

        btAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket!=null)
                {
                    try
                    {
                        bluetoothSocket.getOutputStream().write("B".getBytes());
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
                ibLeft.setEnabled(false);
                ibRight.setEnabled(false);
                ibStop.setEnabled(false);
                btManual.setEnabled(true);
                btAuto.setEnabled(false);
            }
        });


    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case 1:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    tvText.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private class  SendReceive extends Thread
    {
        private  final BluetoothSocket bluetoothSocket;
        private  final InputStream inputStream;
        public  SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
        }
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;
            while(true)
            {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(1,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity3.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (bluetoothSocket == null || !isBTconnected)
                {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    bluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();//start connection
                    if(bluetoothSocket != null)
                    {
                        SendReceive sendReceive = new SendReceive(bluetoothSocket);
                        sendReceive.start();
                    }
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBTconnected = true;
            }
            progress.dismiss();
        }
    }
}
