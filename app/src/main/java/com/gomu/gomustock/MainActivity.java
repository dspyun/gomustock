package com.gomu.gomustock;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gomu.gomustock.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mypermission1();
        getSupportActionBar().hide();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,
                R.id.navigation_simulation)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }


    public static final int MY_PERMISSION_ACCESS_ALL = 200;

    public void mypermission1() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = {
            android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE };
            ActivityCompat.requestPermissions(this, permission_list, MY_PERMISSION_ACCESS_ALL);
        }
    }

    public void MyCheckPermission2() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {

            String[] permission_list = {android.Manifest.permission.INTERNET,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE };
            Toast.makeText(this, "권한없어서 요청", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, permission_list, MY_PERMISSION_ACCESS_ALL);
        } else {
            Log.i("권한 테스트", "처음부터 권한이 있었어요.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_ACCESS_ALL:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    // 권한이 이미 있는 경우
                    Log.i("권한 테스트", "사용자가 권한을 부여해 줬습니다.");
                    //Toast.makeText(this, "사용자가 권한을 부여해 줬습니다", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("앱 권한 설정");
                    alertDialog.setMessage("설정으로 이동합니다.");
                    alertDialog.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 이 부분은 설정으로 이동하는 코드이므로 안드로이드 운영체제 버전에 따라 상이할 수 있다.
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                    dialogInterface.cancel();
                                }
                            });

                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    alertDialog.show();
                }
        }
    }

}