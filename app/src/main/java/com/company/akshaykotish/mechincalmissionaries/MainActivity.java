package com.company.akshaykotish.mechincalmissionaries;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {

    LinearLayout toucharea;
    TextView countertxt;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    private OutputStream outputStream;


    SendRecieve sendRecieve;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "BluetoothCom";
    private static final UUID MY_UUID = UUID.fromString("eec1294e-b33d-11ea-b3de-0242ac130004");
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    BluetoothSocket socket;

    float temp_y = 0;
    float angle = 190;
    int a = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewByIds();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        init_sockets();

        toucharea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float l_y = toucharea.getY();
                float l_h = toucharea.getHeight();

                int output_y = 0;

                float need_value = (l_h /2) + l_y;

                temp_y = event.getY();
                if(temp_y >need_value)
                {
                    angle--;
                    output_y = 0;
                }
                else if(temp_y < need_value)
                {
                 angle++;
                    output_y = 1;
                }
                if(angle >  180)
                {
                    angle =0;
                }

                if(angle < 0)
                {
                    angle = 180;
                }

                a = (int)angle;
                String te = Integer.toString(a);
                    countertxt.setText(te + "°");

                Automatic_Message(output_y);
                Automatic_Message(output_y);
                return false;
            }
        });

        //Automatic_List();
    }

    private void Automatic_List()
    {
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        btArray = new BluetoothDevice[bt.size()];
        int index = 0;

        if(bt.size() > 0)
        {
            for(BluetoothDevice device : bt)
            {
                btArray[index] = device;
                strings[index] = device.getName();
                if(device.getName().equals("Ak"))
                {
                    Automatic_Connect(index);
                }
                index++;
            }
        }
    }

    private void Automatic_Connect(int i)
    {
        //ClientClass clientClass = new ClientClass(btArray[i]);
        //clientClass.start();

        //status.setText("Connecting");
        //Automatic_Message();
    }


    void init_sockets()
    {
        //sendRecieve.write(msge.getBytes());

        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        int index = 0;

        if(bt.size() > 0)
        {
            for(BluetoothDevice device : bt)
            {
                if(device != null && device.getName().equals("Ak"))
                {
                    try {
                        socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                        socket.connect();
                        outputStream = socket.getOutputStream();
                        Toast.makeText(getApplicationContext(),
                                "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();
                        countertxt.setText("Ready!");
                    }
                    catch (IOException e) {
                        e.printStackTrace(); Toast.makeText(getApplicationContext(),
                                e.toString(), Toast.LENGTH_LONG).show();
                    }


                }
                index++;
            }
        }

    }



    private void Automatic_Message(int value)
    {
        String msge = Integer.toString(value);
        try {
            socket.connect();
            outputStream.write(msge.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what)
            {
                case STATE_LISTENING:
                    //status.setText("Listening");
                    break;

                case STATE_CONNECTING:
                    //status.setText("Connecting");
                    break;

                case STATE_CONNECTED:
                    //status.setText("Connected");
                    break;

                case STATE_CONNECTION_FAILED:
                    //status.setText("Failed");
                    break;

                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);
                    //status.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void findViewByIds()
    {
        toucharea = (LinearLayout)findViewById(R.id.toucharea);
        countertxt = (TextView)findViewById(R.id.countertxt);
        //status = (TextView)findViewById(R.id.status);
    }

    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;

        public ServerClass()
        {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket = null;

            while(socket == null)
            {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();

                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket != null)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendRecieve = new SendRecieve(socket);
                    sendRecieve.start();
                    break;
                }
            }
        }

    }


    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device = device1;

            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

                //Automatic_Message();

            }
            catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendRecieve extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieve(BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            InputStream tempin= null;
            OutputStream tempout = null;

            try {
                tempin = bluetoothSocket.getInputStream();
                tempout = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempin;
            outputStream = tempout;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes ;
            while (true)
            {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}