package pl.szymson.klawiatura;

public class GlobalVar {
    public static BluetoothHelper.ConnectedThread ctClient;

    public static BluetoothHelper.ConnectedThread getCtClient() {
        return ctClient;
    }

    public static void setCtClient(BluetoothHelper.ConnectedThread ctClient) {
        GlobalVar.ctClient = ctClient;
    }
}
