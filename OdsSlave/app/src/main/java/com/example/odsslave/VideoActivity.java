package com.example.odsslave;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;


import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//android:background="#11000000"
public class VideoActivity extends AppCompatActivity {

    private ComponentListener componentListener;
    SimpleExoPlayerView simpleExoPlayerView;
    SimpleExoPlayer simpleExoPlayer;
    ExoPlayer exoPlayer;
    Context context;
    int currentWindow=0;
    long playbackPosition=0;
    boolean playWhenReady=true;
    static  String file_path;
     ArrayList<CommonVideo> videoArrayListi;
    View viewVide;
    ImageButton screenMode;
    ImageButton screenLock;
    ImageButton subTitle;
    static int a;
    private View brightNesLinear;
    private View volumeLinear;
    View controlerAboveBottom;
    SeekBar brightNesSeekBar;
    SeekBar volumeSeekBar;
    MediaSource videoSource;
    private int brightness;
    int MY_PERMISSION_WRITE_SETTINGS=10;
    boolean  settingCanwrite;
    private ContentResolver cResolver;
    AudioManager audioManager;
    private Window window;
    int current_position;
    ImageButton exoPerv,exoNext;
    final static int FILE_SELECT=2;
    static VideoActivity videoActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoActivity=this;
        simpleExoPlayerView=findViewById(R.id.player_view);
        viewVide=findViewById(R.id.player_view);
        context=this;
        componentListener=new ComponentListener();
        Intent intent=getIntent();
        file_path=intent.getStringExtra("jeet");
        current_position=intent.getIntExtra("position",0);
        screenMode = findViewById(R.id.screenMode);
        screenLock=findViewById(R.id.screenLock);
        subTitle=findViewById(R.id.subTitle);
        exoNext=findViewById(R.id.exo_Nex);
        exoPerv=findViewById(R.id.exo_preve);

        controlerAboveBottom=findViewById(R.id.controlerAboveBottom);
        a=0;
        MainActivity mainActivity=MainActivity.getInstance();
        videoArrayListi=mainActivity.videosList;
        //Get the content resolver

        cResolver =  getContentResolver();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_SETTINGS)!= PackageManager.PERMISSION_GRANTED){


            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.WRITE_SETTINGS)){

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
                // Create the AlertDialog object
                builder.create();
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_SETTINGS},MY_PERMISSION_WRITE_SETTINGS);
                Toast.makeText(this, "requset", Toast.LENGTH_SHORT).show();

            }

        }
        else {
            Toast.makeText(this, "else", Toast.LENGTH_SHORT).show();
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settingCanwrite= Settings.System.canWrite(this);
            Toast.makeText(this, ""+settingCanwrite, Toast.LENGTH_SHORT).show();
        }
        if(!settingCanwrite){
            Intent intent1=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            context.startActivity(intent1);
        }
        audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        //Get the current window
        window = getWindow();
        brightNesLinear=findViewById(R.id.brightNees_LinearLa);
        volumeLinear=findViewById(R.id.volume_LinearLa);
        brightNesSeekBar=findViewById(R.id.brightNees_SeekBar);
        volumeSeekBar=findViewById(R.id.volume_SeekBar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        screenMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(a==0){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    screenLock.setVisibility(View.VISIBLE);

                }
                else if(a==1){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    screenLock.setVisibility(View.INVISIBLE);
                }


            }
        });
        screenLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        subTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*|WebVTT/*|TTML=.ttxt/*|SMPTE-TT/*|SubRip/*|audio/*");
                 intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent,"SubTitle"),FILE_SELECT);

                }catch (android.content.ActivityNotFoundException e){
                    Toast.makeText(VideoActivity.this, "Please Install a FileManager", Toast.LENGTH_SHORT).show();
                }

            }
        });

        controlerAboveBottom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Toast.makeText(VideoActivity.this, "sing", Toast.LENGTH_SHORT).show();
                int action = MotionEventCompat.getActionMasked(event);

                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        if(exoPlayer.getPlayWhenReady()){
                            exoPlayer.setPlayWhenReady(false);

                        }
                        else {
                            exoPlayer.setPlayWhenReady(true);

                        }


                        return true;
                    default :
                        return VideoActivity.super.onTouchEvent(event);
                }

            }
        });
        exoPerv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTOPreivi();
            }
        });
        exoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNext();
            }
        });


        }

    public static   VideoActivity getInstance(){
        return videoActivity;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                ViewGroup.LayoutParams layoutParamsP=simpleExoPlayerView.getLayoutParams();
                layoutParamsP.width= ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParamsP.height= ViewGroup.LayoutParams.MATCH_PARENT;
                simpleExoPlayerView.setLayoutParams(layoutParamsP);
                a=0;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                a=1;
                 ViewGroup.LayoutParams layoutParams=simpleExoPlayerView.getLayoutParams();
                layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height= ViewGroup.LayoutParams.MATCH_PARENT;
                simpleExoPlayerView.setLayoutParams(layoutParams);

                 break;
            case Configuration.ORIENTATION_SQUARE:
                 break;
            default:
                try {
                    throw new Exception("Unexpected orientation!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }

    private void lockScreenRotation(int orientation)
    {
        // Stop the screen orientation changing during an event
        switch (orientation)
        {

            case Configuration.ORIENTATION_PORTRAIT:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;



        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUiFullScreen() {
        simpleExoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }



    private void initializePlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        simpleExoPlayerView.setPlayer((SimpleExoPlayer) exoPlayer);


        exoPlayer.setPlayWhenReady(playWhenReady);

        exoPlayer.seekTo(currentWindow, playbackPosition);
        Uri uri = Uri.parse(file_path);
        videoSource = buildVideoSource(uri);
        // textSource=buildTextSource(uri);
        // MediaSource  mergingSource=new MergingMediaSource(videoSource,textSource);
        exoPlayer.prepare(videoSource, true, false);
    }


    private MediaSource buildVideoSource(Uri uri) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(VideoActivity.this,
                Util.getUserAgent(VideoActivity.this, "com.exoplayerdemo"), bandwidthMeter);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(uri,dataSourceFactory, extractorsFactory, null, null);
        return videoSource;
    }
    private MediaSource buildTextSource(Uri uri){
        //Format subTitelFormat=Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,3,lang)
        MediaSource textsource=new ExtractorMediaSource(uri,null,null,null,null);

        return textsource;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || exoPlayer == null)) {
            initializePlayer();
        }
    }
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        simpleExoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_LAYOUT_STABLE
                | SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void moveToNext(){
        exoPlayer.release();
        exoPlayer = null;
        playbackPosition=0;
        currentWindow=0;
        current_position=current_position+1;
        playWhenReady=true;
        if(current_position<videoArrayListi.size()){
            file_path=videoArrayListi.get(current_position).getVideoFilePath();
            Toast.makeText(this, "next"+current_position, Toast.LENGTH_SHORT).show();
        }
        else {
            //videoArrayListi.size()-1
            current_position=0;
            Toast.makeText(this, ""+current_position, Toast.LENGTH_SHORT).show();
            file_path=videoArrayListi.get(current_position).getVideoFilePath();
        }
        //   file_path=videoArrayListi.get(current_position+1).getVideoFilePath();
        initializePlayer();
    }
    public void moveTOPreivi(){
        exoPlayer.release();
        exoPlayer = null;
        playbackPosition=0;
        currentWindow=0;
        current_position=current_position-1;
        playWhenReady=true;
        if(current_position>=0){
            file_path=videoArrayListi.get(current_position).getVideoFilePath();
            //Toast.makeText(this, "per"+current_position, Toast.LENGTH_SHORT).show();
        }
        else {
             current_position=videoArrayListi.size()-1;
            file_path=videoArrayListi.get(current_position).getVideoFilePath();
            //Toast.makeText(this, ""+current_position, Toast.LENGTH_SHORT).show();
        }
        initializePlayer();
    }




    public void setVolume(int setVolume) {
        int media_current_Volume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int media_max_volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if(setVolume<=media_max_volume){}
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,setVolume,AudioManager.FLAG_SHOW_UI);


    }

    public void setScreenBrighNess(int screenBrightness) {
        try {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            brightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
             int mode =Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE);
             incressBrightNess(screenBrightness);
        }

        catch (Settings.SettingNotFoundException e)

        {//Throw an error case it couldn't be retrieved
            chackSelfPremison();
            Log.e("Error", "Cannot access system brightness");

            e.printStackTrace();

        }

    }



    public void incressBrightNess(int brightness){
         Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        WindowManager.LayoutParams layoutpars = getWindow().getAttributes();
        layoutpars.screenBrightness = brightness / (float)255;
        getWindow().setAttributes(layoutpars);
        //Toast.makeText(this,"curent   "+brightness, Toast.LENGTH_SHORT).show();

    }


    public void chackSelfPremison(){

        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_SETTINGS)!= PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.WRITE_SETTINGS)){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("We need Write setting  permission to proceed")
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
                // Create the AlertDialog object
                builder.create();
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS},MY_PERMISSION_WRITE_SETTINGS);


            }

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case FILE_SELECT:
                if(resultCode==RESULT_OK){
                    Uri uriSub=data.getData();
                    Toast.makeText(this, ""+uriSub, Toast.LENGTH_SHORT).show();
                }

                break;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void play() {
        exoPlayer.setPlayWhenReady(true);

    }

    public void pause() {
        exoPlayer.setPlayWhenReady(false);

    }

    private class ComponentListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady,
                                         int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";

                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d("TAG", "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }
    }









    @Override
    public void onBackPressed() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onBackPressed();
    }
}
