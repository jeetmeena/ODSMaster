package com.example.ods;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.se.omapi.Channel;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements WifiP2pManager.ActionListener, WifiP2pManager.ChannelListener,WifiP2pManager.PeerListListener,View.OnClickListener {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private List<WifiP2pDevice> peers;
    WifiP2pManager.PeerListListener myPeerListListener;
    boolean wifiDirectEnable;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    Socket socket=null;
    TextView textStatus,textVolume,textBrightNess;
    SeekBar seekBarVolume,seekBarBrightNess;
    Button skipToNext,play_pause,skipToPerviuse;
    String deviceAddress;
    boolean play=false;
    DataOutputStream dOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textStatus=findViewById(R.id.text_status);
        textVolume=findViewById(R.id.text_volume);
        textBrightNess=findViewById(R.id.text_bright_ness);
        seekBarVolume=findViewById(R.id.volume_seekbar);
        seekBarBrightNess=findViewById(R.id.bright_ness_seekbar);
        seekBarVolume.incrementProgressBy(3);
        seekBarBrightNess.incrementProgressBy(50);
        mIntentFilter = new IntentFilter();
        peers = new ArrayList<WifiP2pDevice>();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Toast.makeText(MainActivity.this, ""+info.groupOwnerAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
             deviceAddress=info.groupOwnerAddress.getHostAddress();
            }
        };
        skipToPerviuse=findViewById(R.id.play_pause);
        skipToNext=findViewById(R.id.skip_next);
        play_pause=findViewById(R.id.play_pause);

       myPeerListListener=new WifiP2pManager.PeerListListener() {
           @Override
           public void onPeersAvailable(WifiP2pDeviceList peerList) {
               //Toast.makeText(MainActivity.this, "peer"+peerList, Toast.LENGTH_SHORT).show();
               //wifiArray[0]="not";
               List<WifiP2pDevice> refreshedPeers =new ArrayList<> (peerList.getDeviceList());
               if (!refreshedPeers.equals(peers)) {
                   peers.clear();
                   peers.addAll(refreshedPeers);
                   String[] wifiArray=new String[peers.size()];
                  if(peers.size()>0){
                      for (int i=0;i<peers.size();i++){
                          wifiArray[i]=peers.get(i).deviceName;
                      }
                      AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                      builder.setTitle("List OF Device")
                              .setItems(wifiArray, new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {
                                      tryTocanncatDevice(peers.get(which));
                                      deviceAddress=peers.get(which).deviceAddress;

                                  }
                              });
                      builder.show();
                  }

                   Toast.makeText(MainActivity.this, ""+refreshedPeers, Toast.LENGTH_SHORT).show();

               }

               if (peers.size() == 0) {
                   //Log.d(MainActivity.TAG, "No devices found");
                   return;
               }
           }
       };
       seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               try {
                   textVolume.setText(String.valueOf(progress));
                   sendRequesttoserver(String.valueOf(progress),1);
               } catch (IOException e) {
                   e.printStackTrace();
               }

           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {

           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

           }
       });
       seekBarBrightNess.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               try {
                   textBrightNess.setText(String.valueOf(progress));
                   sendRequesttoserver(String.valueOf(progress),2);

               } catch (IOException e) {
                   e.printStackTrace();
               }
           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {

           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

           }
       });
    }
    public void tryTocanncatDevice(WifiP2pDevice mDevice){
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pDevice device=mDevice;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                //createServerSocket();

            }

            @Override
            public void onFailure(int reason) {
                //failure logic
            }
        });
    }



    @Override
    public void onSuccess() {
        // Picking the first device found on the network.
        //tryTocanncatDevice();
    }

    @Override
    public void onFailure(int reason) {

    }

    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.skip_pervious:
                try {
                    sendRequesttoserver("pervious",3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_pause:
                try {
                    if(!play){
                        sendRequesttoserver("play",4);
                    }
                    else {
                        sendRequesttoserver("play",5);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.skip_next:
                try {
                    sendRequesttoserver("next",6);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.disconnect:
                closeSocket();
                break;
        }
    }

    public class OdsBroadcastReceiver extends BroadcastReceiver{
        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MainActivity mActivity;
        public OdsBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mainActivity) {
             this.mChannel=mChannel;
            this.mActivity=mainActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //mActivity.set
                    wifiDirectEnable=true;
                    Toast.makeText(context, "enabled", Toast.LENGTH_SHORT).show();
                } else {
                    wifiDirectEnable=false;
                    Toast.makeText(context, "!enabled", Toast.LENGTH_SHORT).show();

                    // activity.setIsWifiP2pEnabled(false);
                    Log.d("Tag", "P2P state changed - " + state);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a l ist of current peers

                // request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                if (mManager != null) {
                    mManager.requestPeers(mChannel, myPeerListListener);
                    Toast.makeText(context, "Hii", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "mManager=null", Toast.LENGTH_SHORT).show();


                }
                Log.d("Tag", "P2P peers changed");

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                if (mManager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP

                    mManager.requestConnectionInfo(mChannel,connectionInfoListener);
                } else {
                    // It's a disconnect
                    Toast.makeText(context, "DisconnectPeers", Toast.LENGTH_SHORT).show();
                    // activity.resetData();
                }
                // Toast.makeText(context, "ewtjwo", Toast.LENGTH_SHORT).show();
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                //Toast.makeText(context, ""+WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.enable_direct:
                if (mManager != null && mChannel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e("Tag", "channel or manager is null");
                }
                return true;
             case R.id.discover_peers:
                 mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                     @Override
                     public void onSuccess() {
                         // Code for when the discovery initiation is successful goes here.
                         // No services have actually been discovered yet, so this method
                         // can often be left blank. Code for peer discovery goes in the
                         // onReceive method, detailed below.
                         mManager.requestPeers(mChannel,myPeerListListener);
                         Toast.makeText(MainActivity.this, "Discovery Success", Toast.LENGTH_SHORT).show();

                     }

                     @Override
                     public void onFailure(int reasonCode) {
                         // Code for when the discovery initiation fails goes here.
                         Toast.makeText(MainActivity.this, "Discovery Fails", Toast.LENGTH_SHORT).show();
                         // Alert the user that something went wrong.
                     }
                 });
                return true;
            case R.id.client_socket:
                try {
                    setUpConnectionToServer(deviceAddress,8888);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
                 default:
                    return super.onOptionsItemSelected(item);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new OdsBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    public void setUpConnectionToServer(String hostIpAddress,int port) throws IOException {


        try {

            socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress("192.168.49.71", 8989)),9000);
            Log.d("Tag", "Client:  connected");
            Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        }catch (Exception e){}

    }    public void sendRequesttoserver(String req,int bytesSize) throws IOException {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }
        Log.e("Tag", "Opening client socket - ");

        dOut = new DataOutputStream(socket.getOutputStream());
        dOut.writeByte(bytesSize);
        dOut.writeUTF(req);
        dOut.flush();

    }
    public void closeSocket(){
            if (socket != null) {
                if (socket.isConnected()) {
                    try { dOut.close();
                        Toast.makeText(this, "send", Toast.LENGTH_SHORT).show();
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
    }

}
