package pl.szymson.klawiatura;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static androidx.core.app.ActivityCompat.startActivityForResult;

public class KlawiaturaService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {


    KeyboardView kv = null;
    Keyboard k = null;

        @Override
        public View onCreateInputView() {
            // get the KeyboardView and add our Keyboard layout to it
            kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
            k = new Keyboard(this, R.xml.number_pad);
            //kv.setBackgroundColor(Color.GREEN);


            kv.setOnKeyboardActionListener(this);
            kv.setKeyboard(k);

            Context context = getApplicationContext();
            AssetManager am = context.getAssets();
            InputStream is = null;
            try {
                is = am.open("test/icpng.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File("app/src/main/assets/test/icpng.png");
            kv.setBackground(Drawable.createFromStream(is,"icpng.png"));

            return kv;
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            Context context = getApplicationContext();

            //granie za kazdym razem
            Uri sound = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mp = MediaPlayer.create(context, sound);

            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            int volume_level= am.getStreamVolume(AudioManager.STREAM_SYSTEM);
            if(volume_level>0) mp.start();

            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    CharSequence selectedText = ic.getSelectedText(0);
                    if (TextUtils.isEmpty(selectedText)) {
                        // no selection, so delete previous character
                        ic.deleteSurroundingText(1, 0);
                    } else {
                        // delete the selection
                        ic.commitText("", 1);
                    }
                    break;
                case -91: //Zmiana na klawiature 1
                    k = new Keyboard(this, R.xml.number_pad);
                    kv.setKeyboard(k);
                    kv.setOnKeyboardActionListener(this);
                    break;
                case -92: //Zmiana na klawiature 2
                    k = new Keyboard(this, R.xml.number_pad2);
                    kv.setKeyboard(k);
                    kv.setOnKeyboardActionListener(this);
                    break;
                case -101: //Zad1 wprowadzenie tekstu
                    String customText = "BlaBlaBla";
                    ic.commitText(customText,1);
                    break;
                case -102://bylo granie a zmienilo sie na granie za kazdym razem
                    mp.start();
                    break;
                case -103: //Zad3 kamera
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(cameraIntent);
                    break;
                case -104:
                    String pth = getApplicationContext().getFilesDir().getPath();
                   // String pthExt = Environment.getExternalStorageDirectory().toString();

                    try {
                        File root = new File(pth);
                        File gpxfile = new File(root, "samples.txt");
                        System.out.println(gpxfile.getAbsolutePath());
                        FileWriter writer = new FileWriter(gpxfile);
                        writer.append("Tekst zapisany w pliku");
                        writer.flush();
                        writer.close();


                        StringBuilder text = new StringBuilder();
                            BufferedReader br = new BufferedReader(new FileReader(gpxfile));
                            String line;

                            while ((line = br.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            br.close();
                        System.out.println("TEXT: "+text);
                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    finally {
                        break;
                    }
                case -105: //Zad5 toast
                    String toastText = "Hello toast!";
                    customToast(context, toastText,Toast.LENGTH_SHORT);
                    break;
                case -106: //Zad 6 Odczyt pliku zamist zmiany klawiatury
                    String path = getApplicationContext().getFilesDir().getPath();
                    String pathExternal = Environment.getExternalStorageDirectory().toString();
                    try {

                        File root = new File(path);
                        File gpxfile = new File(root, "samples.txt");

                        StringBuilder text = new StringBuilder();
                        BufferedReader br = new BufferedReader(new FileReader(gpxfile));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                        ic.commitText(text,1);
                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    finally {
                        break;
                    }
                case -107: //Zad7 Sprawdzanie czy NFC włączone
                    String nfcText = "";

                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
                    if (nfcAdapter == null) {
                        nfcText = "Not found NFC";
                    } else if (!nfcAdapter.isEnabled()) {
                       nfcText = "NFC is disabled";
                    } else {
                        nfcText = "NFC is enabled";
                    }
                    customToast(context,nfcText,Toast.LENGTH_SHORT);
                    break;
                case -108: //Zad8 On/Off NFC

                    break;
                case -109: //zad9 Sprawdzenie czy dziala wifi
                    String wifiText = "";

                    WifiManager wifiManager =
                            (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    wifiText = wifiManager.isWifiEnabled()?"WiFi is enabled":"Wifi is disabled";

                    customToast(context,wifiText,Toast.LENGTH_SHORT);
                    break;
                case -110: // On/Off wifi
                    String wifiTxt1 = "";

                    WifiManager wifiManager1 =
                            (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    if(wifiManager1.isWifiEnabled()){
                        wifiManager1.setWifiEnabled(false);
                        wifiTxt1 = "WiFi is now disabled";
                    } else {
                        wifiManager1.setWifiEnabled(true);
                        wifiTxt1 = "WiFi is now enabled";
                    }
                    customToast(context,wifiTxt1,Toast.LENGTH_SHORT);
                    break;
                case -111: // zad 2.1
                    runServer();
                    break;
                case -112: // zad 2.2
                    System.out.println("ConnectedThread is null?  "+GlobalVar.getCtClient().isAlive());
                        GlobalVar.getCtClient().write("616000010000123".getBytes());
                    break;
                case -113: // zad 2.3
                    System.out.println("ConnectedThread is null?  "+GlobalVar.getCtClient().isAlive());
                        GlobalVar.getCtClient().write("616000010000124".getBytes());
                    break;
                case -114: // Wybieramy klawiature
                    InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                    imeManager.showInputMethodPicker();
                    break;
                case -151: // zad 2.3

                    break;
                default:
                    char code = (char) primaryCode;
                    ic.commitText(String.valueOf(code), 1);
            }
        }

        @Override
        public void onPress(int primaryCode) { }

        @Override
        public void onRelease(int primaryCode) { }

        @Override
        public void onText(CharSequence text) { }

        @Override
        public void swipeLeft() { }

        @Override
        public void swipeRight() { }

        @Override
        public void swipeDown() { }

        @Override
        public void swipeUp() { }

        private void customToast(Context context,String text,int duration){
            Context ctx = context;
            CharSequence txt = text;
            int time = duration;

            Toast.makeText(ctx, txt, time).show();
            return;
        }

    private static final UUID MY_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

        private void runServer(){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                //WLACZ BLUETOOTH
            }
//            Intent discoverableIntent =
//                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);

            Log.e(TAG,"RUNNING BLUETOOTH SERVICE");
            AcceptThread at = new AcceptThread();
            at.start();
        }

    private class AcceptThread extends Thread {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MY_APP", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    ConnectedThread ct = new ConnectedThread(socket);
                    Log.e(TAG,"TRY RUN CONNECTED THREAD");
                    ct.start();
                    try {
                        mmServerSocket.close();
                    }
                    catch (Exception e){
                        Log.e(TAG, "BLAD 1: ",e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


////////////////            SERVICe

    private static final String TAG = "MY_APP_DEBUG_TAG";
    //private Handler handler; // handler that gets info from Bluetooth service

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.e(TAG,"Odczytano wiadomosc: "+readMessage);
                    break;
            }
            return false;
        }
    });


    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            Log.e(TAG,"NAWIAZANO POLACZENIE");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            Log.e(TAG,"CONNECTED THREAD RUNNING");
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    String readMessage = new String(mmBuffer, 0, numBytes);
                    Log.e(TAG,String.valueOf(numBytes));
                    Log.e(TAG,"NOWE ODCZYTANO: "+readMessage);
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(readMessage, 1);
                    }else {
                        Log.e(TAG, "NIE ZNALAZLEM INPUT");
                    }

                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }




}