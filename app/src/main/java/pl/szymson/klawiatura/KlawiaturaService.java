package pl.szymson.klawiatura;


import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class KlawiaturaService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    KeyboardView kv = null;
    Keyboard k = null;

        @Override
        public View onCreateInputView() {
            // get the KeyboardView and add our Keyboard layout to it
            kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
            k = new Keyboard(this, R.xml.number_pad);
            kv.setKeyboard(k);
            kv.setOnKeyboardActionListener(this);
            return kv;
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            Context context = getApplicationContext();
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
                case -102:
                    Uri sound = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                    MediaPlayer mp = MediaPlayer.create(context, sound);
                    mp.start();
                    break;
                case -103: //Zad3 kamera
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(cameraIntent);
                    break;
                case -104:
                    String pth = getApplicationContext().getFilesDir().getPath();
                    String pthExt = Environment.getExternalStorageDirectory().toString();

                    try {
                        File root = new File(pth);
                        File gpxfile = new File(root, "samples.txt");
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
    }