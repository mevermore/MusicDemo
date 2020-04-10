package com.smasher.music.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
//import androidx.viewpager2.widget.ViewPager2;

import com.smasher.music.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

/**
 * @author matao
 */
public class MainActivity extends AppCompatActivity implements Handler.Callback {

    private static final String TAG = "MainActivity";
    @BindView(R.id.buttonAddress)
    Button buttonStart;
    @BindView(R.id.buttonPermission)
    Button buttonStop;
    @BindView(R.id.buttonSkip)
    Button front;
    @BindView(R.id.tvAddress)
    TextView textView;
    @BindView(R.id.exit)
    Button exit;


    String[] permissions;

    private Handler mHandler;
    public static final int REQUEST_CODE = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mHandler = new Handler(this);
        ViewPager viewPager = new ViewPager(this);
//        ViewPager2 viewPager2 = new ViewPager2(this);
        initPermissionNeed();
    }

    private void initPermissionNeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
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

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @OnClick({R.id.buttonAddress, R.id.buttonPermission, R.id.buttonSkip,
            R.id.tvAddress, R.id.exit})
    public void onViewClicked(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buttonAddress:
                initAddress();
                break;
            case R.id.buttonPermission:
                doCheckPermission();
                break;
            case R.id.buttonSkip:
                doSkip();
                break;
            case R.id.tvAddress:
                break;
            case R.id.exit:

                break;
            default:
                break;
        }


    }


    private void initAddress() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Path:").append('\n');

            String data = Environment.getDataDirectory().getPath();
            Log.d(TAG, "data: " + data);
            stringBuilder.append("data:").append(data).append('\n').append('\n');

            String dataAbs = Environment.getDataDirectory().getAbsolutePath();
            Log.d(TAG, "data_abs: " + dataAbs);
            stringBuilder.append("dataAbs:").append(dataAbs).append('\n').append('\n');

            String cache = Environment.getDownloadCacheDirectory().getPath();
            Log.d(TAG, "cache: " + cache);
            stringBuilder.append("cache:").append(cache).append('\n').append('\n');

            String cacheAbs = Environment.getDownloadCacheDirectory().getAbsolutePath();
            Log.d(TAG, "cache_abs: " + cacheAbs);
            stringBuilder.append("cacheAbs:").append(cacheAbs).append('\n').append('\n');

            String system = Environment.getRootDirectory().getPath();
            Log.d(TAG, "system: " + system);
            stringBuilder.append("system:").append(system).append('\n').append('\n');

            String systemAbs = Environment.getRootDirectory().getAbsolutePath();
            Log.d(TAG, "system_abs: " + systemAbs);
            stringBuilder.append("systemAbs:").append(systemAbs).append('\n').append('\n');

            String music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
            Log.d(TAG, "music: " + music);
            stringBuilder.append("music:").append(music).append('\n').append('\n');

            String musicAbs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
            Log.d(TAG, "music_abs: " + musicAbs);
            stringBuilder.append("musicAbs:").append(musicAbs).append('\n').append('\n');

            textView.setText(stringBuilder.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doSkip() {
        Intent intent = new Intent();
        intent.setClass(this, PlayListActivity.class);
        startActivity(intent);
    }

    private void doCheckPermission() {
        if (checkPermissions()) {
            Toast.makeText(this, "has permission", Toast.LENGTH_SHORT).show();
        } else {
            //do nothing
            Log.d(TAG, "permission is requesting");
        }
    }


    @AfterPermissionGranted(REQUEST_CODE)
    private boolean checkPermissions() {
        boolean perms = EasyPermissions.hasPermissions(this, permissions);
        if (perms) {
            //do nothing
            return true;
        } else {
            PermissionRequest.Builder builder = new PermissionRequest.Builder(this, REQUEST_CODE, permissions);
            PermissionRequest request = builder.build();
            EasyPermissions.requestPermissions(request);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }


}
