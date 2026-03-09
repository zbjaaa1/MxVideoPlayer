package com.app.mxvideoplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hb.xvideoplayer.MxTvPlayerWidget;
import hb.xvideoplayer.MxVideoPlayer;

public class TvActivity extends AppCompatActivity {

    private MxTvPlayerWidget mVideoPlayerWidget;

    private List<String> videoList = new ArrayList<>();
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);

        // 全屏
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        mVideoPlayerWidget = findViewById(R.id.mpw_video_player);

        // 查找U盘
        String usbPath = findUsbWithVideo();

        if(usbPath != null){

            loadVideos(usbPath);

            if(videoList.size() > 0){

                startPlay(videoList.get(index));
            }
        }

        mVideoPlayerWidget.setOnPlayStateListener(new MxTvPlayerWidget.OnPlayStateListener() {

            @Override
            public void onPlayPrepared() {

            }

            @Override
            public void onPlayBufferingUpdate(int percent) {

            }

            @Override
            public void OnPlayCompletion() {

                playNext();
            }
        });
    }

    // 播放视频
    private void startPlay(String path){

        mVideoPlayerWidget.startPlay(path,"USB Video");

        restoreProgress(path);
    }

    // 播放下一个
    private void playNext(){

        index++;

        if(index >= videoList.size()){
            index = 0;
        }

        startPlay(videoList.get(index));
    }

    // 找到包含视频的U盘
    private String findUsbWithVideo(){

        File storage = new File("/storage");

        File[] dirs = storage.listFiles();

        if(dirs == null) return null;

        for(File f : dirs){

            String path = f.getAbsolutePath();

            if(path.contains("emulated") || path.contains("self"))
                continue;

            if(f.isDirectory() && f.canRead()){

                if(hasVideoInRoot(f)){

                    return path;
                }
            }
        }

        return null;
    }

    // 判断根目录是否有视频
    private boolean hasVideoInRoot(File dir){

        File[] files = dir.listFiles();

        if(files == null) return false;

        for(File f : files){

            if(f.isFile()){

                String name = f.getName().toLowerCase();

                if(name.endsWith(".mp4")
                        || name.endsWith(".mkv")
                        || name.endsWith(".avi")
                        || name.endsWith(".ts")
                        || name.endsWith(".mov")
                        || name.endsWith(".flv")){

                    return true;
                }
            }
        }

        return false;
    }

    // 读取视频列表（只扫描一层）
    private void loadVideos(String usbPath){

        File dir = new File(usbPath);

        File[] files = dir.listFiles();

        if(files == null) return;

        for(File f : files){

            if(f.isFile()){

                String name = f.getName().toLowerCase();

                if(name.endsWith(".mp4")
                        || name.endsWith(".mkv")
                        || name.endsWith(".avi")
                        || name.endsWith(".ts")
                        || name.endsWith(".mov")
                        || name.endsWith(".flv")){

                    videoList.add(f.getAbsolutePath());
                }
            }
        }
    }

    // 恢复播放进度
    private void restoreProgress(String path){

        SharedPreferences sp = getSharedPreferences("video",MODE_PRIVATE);

        long pos = sp.getLong(path,0);

        if(pos > 0){

            mVideoPlayerWidget.seekTo((int)pos);
        }
    }

    // 保存进度
    private void saveProgress(){

        if(videoList.size()==0) return;

        String path = videoList.get(index);

        SharedPreferences sp = getSharedPreferences("video",MODE_PRIVATE);

        long pos = mVideoPlayerWidget.getCurrentPosition();

        sp.edit().putLong(path,pos).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveProgress();

        MxVideoPlayer.releaseAllVideos();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mVideoPlayerWidget.requestKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mVideoPlayerWidget.requestKeyUp(keyCode,event);
    }
}