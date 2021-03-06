package com.tatoado.ramabluewingood;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button btnAcelerate, btnDesacelerate, btnAdelante, btnAtras, btnIzquierda, btnDerecha, btnStop;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    RelativeLayout layout_joystick, layout_joystick_vel;
    ImageView image_joystick, image_border;
    TextView  textView5, textView6, textView;

    JoyStickClass js;

    private SeekBar seekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAcelerate = (Button) findViewById(R.id.buttonOn);
        btnDesacelerate = (Button) findViewById(R.id.buttonOff);

        /*btnAdelante = (Button) findViewById(R.id.btnArriba);
        btnAtras = (Button) findViewById(R.id.btnAtras);
        btnIzquierda = (Button) findViewById(R.id.btnLeft);
        btnDerecha = (Button) findViewById(R.id.btnRight);
        btnStop = (Button) findViewById(R.id.btnStop);*/

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        textView5 = (TextView)findViewById(R.id.textView5);
        textView6 = (TextView)findViewById(R.id.textView6);


       // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnAcelerate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("9");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Acelerar", Toast.LENGTH_SHORT).show();
            }
        });

        btnDesacelerate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("a");    // Send "1" via Bluetooth
                Toast.makeText(getBaseContext(), "Desacelerar", Toast.LENGTH_SHORT).show();
            }
        });

        /*

        btnAdelante.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(getBaseContext(), "DOWN", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("1");
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(getBaseContext(), "UP", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("0");
                }
                return false;
            }
        });

        btnAtras.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(getBaseContext(), "DOWN", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("2");
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(getBaseContext(), "UP", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("0");
                }
                return false;
            }
        });

        btnIzquierda.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(getBaseContext(), "DOWN", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("3");
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(getBaseContext(), "UP", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("0");
                }
                return false;
            }
        });

        btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Derecha", Toast.LENGTH_SHORT).show();
            }
        });


        //demostration
        btnDerecha.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Toast.makeText(getBaseContext(), "DOWN", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("4");
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    Toast.makeText(getBaseContext(), "UP", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("0");
                }
                return false;
            }
        });*/

        UiGameControl();

        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        textView = (TextView) findViewById(R.id.textView1);

        textView.setText("Covered: " + seekBar.getProgress() + "/" + seekBar.getMax());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Covered: " + progress + "/" + seekBar.getMax());
                Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UiNewGameControl(){

    }

    private void UiGameControl(){
        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext()
                , layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);

                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {


                    int direction = js.get8Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        UtilConstants.intoUp++;
                        if (UtilConstants.intoUp == 1) {
                            mConnectedThread.write("1");
                            Log.i("DIEGO", "UP " + UtilConstants.intoUp);
                            textView5.setText("Direction : Up");
                            resetParam(1);
                        }

                    } else if (direction == JoyStickClass.STICK_RIGHT) {

                        UtilConstants.intoRight++;
                        if (UtilConstants.intoRight == 1) {
                            mConnectedThread.write("3");
                            Log.i("DIEGO", "intoRigth " + UtilConstants.intoRight);
                            textView5.setText("Direction : Right");
                            resetParam(3);
                        }
                    } else if (direction == JoyStickClass.STICK_DOWN) {

                        UtilConstants.intoDown++;
                        if (UtilConstants.intoDown == 1) {
                            mConnectedThread.write("2");
                            Log.i("DIEGO", "Down " + UtilConstants.intoDown);
                            textView5.setText("Direction : Down");
                            resetParam(2);
                        }
                    } else if (direction == JoyStickClass.STICK_LEFT) {

                        UtilConstants.intoLeft++;
                        if (UtilConstants.intoLeft == 1) {
                            mConnectedThread.write("4");
                            Log.i("DIEGO", "intoLeft " + UtilConstants.intoLeft);
                            textView5.setText("Direction : Left");
                            resetParam(4);
                        }
                    } else if (direction == JoyStickClass.STICK_NONE) {

                        UtilConstants.intoCenter++;
                        if (UtilConstants.intoCenter == 1) {
                            Log.i("DIEGO", "CENTER " + UtilConstants.intoCenter);
                            mConnectedThread.write("0");
                            resetParam(5);
                        }
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {

                    Log.i("DIEGO", "STOP");
                    mConnectedThread.write("0");
                    textView5.setText("Direction : STOP");

                    UtilConstants.intoUp = 0;
                    UtilConstants.intoCenter = 0;
                    UtilConstants.intoLeft = 0;
                    UtilConstants.intoRight = 0;
                    UtilConstants.intoDown = 0;
                }
                return true;
            }
        });


    }

    public void resetParam(int pos) {
        if (pos == 1) {
            UtilConstants.intoCenter = 0;
            UtilConstants.intoLeft = 0;
            UtilConstants.intoRight = 0;
            UtilConstants.intoDown = 0;
        }
        if (pos == 2) {
            UtilConstants.intoUp = 0;
            UtilConstants.intoCenter = 0;
            UtilConstants.intoLeft = 0;
            UtilConstants.intoRight = 0;
        }

        if (pos == 3) {
            UtilConstants.intoUp = 0;
            UtilConstants.intoCenter = 0;
            UtilConstants.intoLeft = 0;
            UtilConstants.intoDown = 0;
        }

        if (pos == 4) {
            UtilConstants.intoUp = 0;
            UtilConstants.intoCenter = 0;
            UtilConstants.intoRight = 0;
            UtilConstants.intoDown = 0;
        }

        if (pos == 5) {
            UtilConstants.intoUp = 0;
            UtilConstants.intoLeft = 0;
            UtilConstants.intoRight = 0;
            UtilConstants.intoDown = 0;
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("diego", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    // bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }


}


