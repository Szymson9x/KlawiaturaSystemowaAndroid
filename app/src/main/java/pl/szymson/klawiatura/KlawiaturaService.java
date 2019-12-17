package pl.szymson.klawiatura;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.os.Environment;
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
                    setBluetoothMess("Hello I’m custom bluetooth keyboard!!!");
                    break;
                case -112: // zad 2.2
                    setBluetoothMess("616000010000123");
                    break;
                case -113: // zad 2.3
                    setBluetoothMess("616000010000124");
                    break;
                case -114: // Wybieramy klawiature
                    InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                    imeManager.showInputMethodPicker();
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
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device, String text) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                UUID MY_UUID = UUID.randomUUID();
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;

            try {
                mmSocket.getOutputStream().write(text.getBytes(Charset.forName("UTF-8")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.

            //!!!
            //bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            ///!!!
           // manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    private void setBluetoothMess(String mess){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult();
            startActivity(enableBtIntent);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                ConnectThread ct = new ConnectThread(device,mess);
            }
        }
    }


}