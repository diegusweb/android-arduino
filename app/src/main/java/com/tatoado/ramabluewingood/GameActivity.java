package com.tatoado.ramabluewingood;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class GameActivity extends Activity {

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address  20:14:11:28:32:39
    private static String address = "20:14:11:28:32:39";


    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    private TextView direTextView;
    // Importing as others views
    private JoystickView joystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
        direTextView = (TextView) findViewById(R.id.direTextView);
        // referring as others views
        joystick = (JoystickView) findViewById(R.id.joystickView);

        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                direTextView.setText("direction " + String.valueOf(direction) + "°");
                angleTextView.setText("Angle " + String.valueOf(angle) + "°");
                powerTextView.setText("Power " + String.valueOf(power) + "% ---- "+Redondear(power));

                // DecimalFormat decf = new DecimalFormat("#,##0");
                //System.out.println(decf.format(cantTotal));

                switch (direction) {
                    case JoystickView.FRONT:
                        directionTextView.setText(R.string.front_lab);
                        //double res = (power/25);
                        //if(power % 2 == 0)
                            mConnectedThread.write("1"+Redondear(power)+"");

                        Log.i("Diego", "1" + Redondear(power));
                        break;

                    case JoystickView.FRONT_RIGHT:
                        directionTextView.setText(R.string.front_right_lab);
                        mConnectedThread.write("6" + Redondear(power) + "");
                        break;

                    case JoystickView.RIGHT:
                        directionTextView.setText(R.string.right_lab);
                            mConnectedThread.write("3"+Redondear(power)+"");

                        break;

                    case JoystickView.RIGHT_BOTTOM:
                        directionTextView.setText(R.string.right_bottom_lab);
                        mConnectedThread.write("8" + Redondear(power) + "");
                        break;

                    case JoystickView.BOTTOM:
                        directionTextView.setText(R.string.bottom_lab);
                            mConnectedThread.write("2"+Redondear(power)+"");
                        Log.i("Diego", "1" + Redondear(power));
                        break;

                    case JoystickView.BOTTOM_LEFT:
                        mConnectedThread.write("7" + Redondear(power)+"");
                        directionTextView.setText(R.string.bottom_left_lab);
                        break;

                    case JoystickView.LEFT:
                        directionTextView.setText(R.string.left_lab);
                        mConnectedThread.write("4" + Redondear(power)+"");
                        break;

                    case JoystickView.LEFT_FRONT:
                        directionTextView.setText(R.string.left_front_lab);
                        mConnectedThread.write("6" + Redondear(power)+"");
                        break;

                    case JoystickView.CENTYER:

                        if(power == 0){
                            directionTextView.setText("CENTER - STOP");
                            mConnectedThread.write("000");
                        }

                        break;

                    default:
                        directionTextView.setText(R.string.center_lab);

                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    public String Redondear(int numero)
    {
        numero = (numero*99)/100;
        if(numero < 10)
            return "0"+numero;
        else
            return Integer.toString(numero);
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
