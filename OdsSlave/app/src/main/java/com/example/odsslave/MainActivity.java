package com.example.odsslave;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements WifiP2pManager.ActionListener, WifiP2pManager.ChannelListener,WifiP2pManager.PeerListListener {
    public ArrayList<CommonVideo> videosList=null;
    HashMap<Integer, Bitmap> hashMap=new HashMap<Integer, Bitmap>();

    private VideoView video;

    RecyclerView recyclerVideo;
    RecyclerView.LayoutManager layoutManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private List<WifiP2pDevice> peers;
    WifiP2pManager.PeerListListener myPeerListListener;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    boolean wifiDirectEnable;
    public static MainActivity mainActivity;
    static int MY_PERMISSIONS_REQUEST_READ_CONTACTS=44;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter = new IntentFilter();
        peers = new ArrayList<WifiP2pDevice>();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mainActivity=this;
        mChannel = mManager.initialize(this, getMainLooper(), null);
        recyclerVideo=findViewById(R.id.recycler_Video);
        connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                try {
                    Toast.makeText(MainActivity.this, ""+info.groupOwnerAddress.getHostAddress(), Toast.LENGTH_SHORT).show();

                }catch (Exception e){}
            }
        };
        myPeerListListener=new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                Toast.makeText(MainActivity.this, "peer"+peerList, Toast.LENGTH_SHORT).show();
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
        checkStoregReadPermission();
    }

    private void checkStoregReadPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("We need read external storage permission to proceed")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.dismiss();
                            }
                        });
                builder.create();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_SETTINGS  },
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }else {
           runThread();
        }
    }
    public  void  runThread(){
     thread=   new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                      videosList= getVideoList();
                        //getAllVideoPath= getAllVideoList();
                        //setViewPager(viewPager);

                    }
                }
        );
     thread.start();
      threadAlive();
    }
    public void threadAlive(){
        if (thread.isAlive()){
            try {
                Thread.sleep(1000);
                threadAlive();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
       else {
           videoLoad();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==MY_PERMISSIONS_REQUEST_READ_CONTACTS){
            if( grantResults[0]==PackageManager.PERMISSION_GRANTED){
             runThread();
            }
            else {
                Toast.makeText(this, "Restart App And give Permission for the Process", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static   MainActivity getInstance(){
        return mainActivity;
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
                new FileServerAsyncTask(MainActivity.this);
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
            }
        });
    }

    private void createServerSocket() {
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

    public class OdsBroadcastReceiver extends BroadcastReceiver{
        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MainActivity mActivity;
        public OdsBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mainActivity) {
            this.mManager=mManager;
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
            case R.id.server_setup:
                FileServerAsyncTask fileServerAsyncTask=    new FileServerAsyncTask(MainActivity.this);
                fileServerAsyncTask.execute();
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
    public static class FileServerAsyncTask extends AsyncTask{

        private Context context;
        private TextView statusText;
        MainActivity mainActivity;
        VideoActivity videoActivity=VideoActivity.getInstance();
        public FileServerAsyncTask(Context context) {
            this.context =   context;

        }

        @Override
        protected String doInBackground(Object[] params) {
                  mainActivity=MainActivity.getInstance();

            try {
                ServerSocket serverSocket = new ServerSocket(8989);
                Log.d("Tag", " Socket opened");
                Socket client = serverSocket.accept();
                Log.d("Tag", "Server Connect");
                InputStream inputstream = client.getInputStream();
                String stringReader=inputstream.toString();
                Toast.makeText(mainActivity, ""+stringReader, Toast.LENGTH_SHORT).show();
                // copyFile(inputstream, new FileOutputStream(f));
                DataInputStream dIn = new DataInputStream(client.getInputStream());


                boolean done = false;
                while(!done) {
                    byte messageType = dIn.readByte();

                    switch(messageType)
                    {
                        case 1: // Type
                            try {
                                videoActivity.setVolume(Integer.getInteger(dIn.readUTF()));

                            }catch (Exception e){}
                            break;
                        case 2: // Type B
                            try {
                                videoActivity.setScreenBrighNess(Integer.getInteger(dIn.readUTF()));

                            }catch (Exception e){}
                            break;
                        case 3: // Type C
                             videoActivity.moveTOPreivi();
                            break;
                        case 4: // Type C
                            videoActivity.pause();
                            break;
                        case 5: // Type C
                             videoActivity.play();
                            break;
                        case 6: // Type C
                            videoActivity.moveToNext();
                            break;
                        default:
                            done = true;
                    }
                }

                dIn.close();

                return inputstream.toString();
            } catch (IOException e) {
                Log.e("oh", e.getMessage());
                return null;
            }


        }


        /**
         * Start activity that can handle the JPEG image
         */

    }

    private void videoLoad() {

     if(videosList==null){
       videosList=getVideoList();
     }
        layoutManager=new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,true);
        recyclerVideo.setLayoutManager(layoutManager);
        VideoListAdpter listAdpter=new VideoListAdpter();
        recyclerVideo.setAdapter(listAdpter);
    }

    private void creatCacheBitmap(int a,String path){
        hashMap.put(a, ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND));

    }
    public ArrayList<CommonVideo> getVideoList(){
        ArrayList<CommonVideo> videList=new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        //String string = MediaStore.Video.Media.;
        int count=0;
        String[] projection={
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,    // filepath of the audio file
                MediaStore.Audio.Media._ID,     // context id/ uri id of the file
                MediaStore.Audio.Media.DISPLAY_NAME,};
        Uri videoUri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        Cursor cursorAudio = contentResolver.query(videoUri, projection, null, null, MediaStore.Video.Media.DISPLAY_NAME);
        if(cursorAudio!=null && cursorAudio.moveToFirst()){

            do{String videoTitle=cursorAudio.getString(0);
                String videoArtist=cursorAudio.getString(1);
                String videoAlbum=cursorAudio.getString(2);
                String videoDuration=cursorAudio.getString(3);
                String videoData=cursorAudio.getString(4);
                String videoId=cursorAudio.getString(5);
                String videoDisplayName=cursorAudio.getString(6);
                videList.add(new CommonVideo(videoTitle,videoArtist,videoAlbum,videoDuration,videoData,videoId,videoDisplayName));
                creatCacheBitmap(count,videoData);
                count++;
            }while (cursorAudio.moveToNext());

        }
        else {
            Toast.makeText(this,"No Video Faund",Toast.LENGTH_SHORT).show();
        }

        return  videList;
    }

    private class VideoListAdpter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View layoutView = LayoutInflater.from(MainActivity.this).inflate(R.layout.video_view,null);

            //fab2.show();
            ViewHolder videoitemHolder = new ViewHolder(layoutView);

            return videoitemHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
           viewHolder.countryName.setText(videosList.get(i).getVideoTtile());
           viewHolder.videoPerView.setImageBitmap(hashMap.get(i));
        }

        @Override
        public int getItemCount() {
            return videosList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView countryName;
        // public ImageView countryPhoto;
        ImageView videoPerView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName = (TextView) itemView.findViewById(R.id.country_name);
            //countryPhoto = (ImageView) itemView.findViewById(R.id.country_photo);
            videoPerView=itemView.findViewById(R.id.videoView);
            itemView.setTag(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent=new Intent(MainActivity.this,VideoActivity.class);
            intent.putExtra("jeet",videosList.get(getAdapterPosition()).getVideoFilePath());
            intent.putExtra("position",getAdapterPosition());
            startActivity(intent);
        }
    }
}

