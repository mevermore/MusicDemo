package com.smasher.music.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smasher.music.IMusicService;
import com.smasher.music.R;
import com.smasher.music.activity.adapter.MusicListAdapter;
import com.smasher.music.adapter.OnItemClickListener;
import com.smasher.music.constant.Constant;
import com.smasher.music.constant.PlayerState;
import com.smasher.music.entity.MediaInfo;
import com.smasher.music.entity.RequestInfo;
import com.smasher.music.loader.MusicLoader;
import com.smasher.music.service.MusicService;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 播放页面
 *
 * @author matao
 */
public class PlayListActivity extends AppCompatActivity implements Handler.Callback,
        OnItemClickListener {

    private static final String TAG = "PlayListActivity";
    @BindView(R.id.previous)
    ImageButton previous;
    @BindView(R.id.play_pause)
    ImageButton playAndPause;
    @BindView(R.id.next)
    ImageButton next;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private Handler mHandler;
    private MusicLoader loader;
    private MusicListAdapter musicListAdapter;

    private IMusicService mBinder;

    private ArrayList<MediaInfo> mList = new ArrayList<>();


    private boolean isBind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.bind(this);
        mHandler = new Handler(this);

        loader = MusicLoader.getInstance(getContentResolver());

        initToolbar();
        initState();
        initList();
        initData();
    }

    private void initToolbar() {
        toolbar.setTitle("MUSIC");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(mMenuItemClickListener);

    }

    private void initData() {

        loader.getMusicList(list -> {
            mList.addAll(list);
            musicListAdapter.setData(mList);
            musicListAdapter.notifyDataSetChanged();
            if (mBinder != null) {
                int size = mList.size();
                MediaInfo[] temp = mList.toArray(new MediaInfo[size]);
                Log.d(TAG, "initData: " + temp.length);
                try {
                    mBinder.setList(temp, null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });


        String foldPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        File file = new File(foldPath);
        if (!file.exists()) {
            Log.d(TAG, "play: exists: false");
            return;
        } else {
            Log.d(TAG, "play: exists: true");
            Log.d(TAG, "play: isDirectory" + file.isDirectory());
            String[] list = file.list();

        }

    }

    private void initList() {

        musicListAdapter = new MusicListAdapter(this);
        musicListAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(musicListAdapter);

    }

    private void initState() {
        if (mBinder == null) {
            playAndPause.setTag(Constant.MUSIC_STATE_PLAY);
            playAndPause.setImageResource(R.drawable.music_play);
            return;
        }

        boolean isPlaying = false;
        try {
            isPlaying = mBinder.isPlayingOnTheSurface();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        playAndPause.setTag(isPlaying ? Constant.MUSIC_STATE_PAUSE : Constant.MUSIC_STATE_PLAY);
        playAndPause.setImageResource(isPlaying ? R.drawable.music_pause : R.drawable.music_play);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommandType(RequestInfo.COMMAND_START);

        Intent intent = new Intent();
        intent.setClass(this, MusicService.class);
        intent.putExtra(RequestInfo.REQUEST_TAG, requestInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        mHandler.postDelayed(() -> {
            Intent intent1 = new Intent();
            intent1.setClass(PlayListActivity.this, MusicService.class);
            isBind = bindService(intent1, mConnection, BIND_AUTO_CREATE);
        }, 200);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBind) {
            unbindService(mConnection);
            isBind = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View view, int position) {
        MediaInfo item = mList.get(position);
        String url = item.getUrl();
        Toast.makeText(this, item.getTitle() + " path:" + url, Toast.LENGTH_SHORT).show();


        if (mBinder != null) {
            try {
                mBinder.playPos(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mHandler.postDelayed(this::initState, 500);
    }


    OnMenuItemClickListener mMenuItemClickListener = item -> {

        switch (item.getItemId()) {
            case R.id.action_exit:
                try {
                    if (mBinder != null) {
                        mBinder.exit();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
        return true;
    };


    private void changeMusicAction(View view, int id) {
        try {
            int temp = mBinder.getCurPos();
            int position = temp > -1 ? temp : 0;
            switch (id) {
                case R.id.previous:
                    position = position - 1;
                    mBinder.playPos(position);
                    break;
                case R.id.play_pause:
                    if (mBinder.isPlayingOnTheSurface()) {
                        mBinder.pause(false);
                    } else if (mBinder.getPlayState() == PlayerState.PLAY_STATE_PAUSE.getState()) {
                        mBinder.resume();
                    } else {
                        mBinder.play();
                    }
                    break;
                case R.id.next:
                    position = position + 1;
                    mBinder.playPos(position);
                    break;
                default:
                    break;
            }


            mHandler.postDelayed(this::initState, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MediaInfo getTargetItem(int position) {
        if (position < mList.size() && position >= 0) {
            return mList.get(position);
        }
        return null;
    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            mBinder = IMusicService.Stub.asInterface(service);
            int size = mList.size();
            MediaInfo[] list = (MediaInfo[]) mList.toArray(new MediaInfo[size]);
            Log.d(TAG, "onServiceConnected: " + list.length);
            try {
                mBinder.setList(list, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };


    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }


    @OnClick({R.id.previous, R.id.play_pause, R.id.next})
    public void onViewClicked(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.previous:
            case R.id.play_pause:
            case R.id.next:
                changeMusicAction(view, id);
                break;
            default:
                break;
        }
    }
}
