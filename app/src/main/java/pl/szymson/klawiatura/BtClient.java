package pl.szymson.klawiatura;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BtClient {

    public static void enableClient(Context ctx){
        BluetoothHelper bh = new BluetoothHelper(ctx);
        bh.polacz();
    }

}
