package sellerapp.android.favshop.techloft.com.dotocdo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static TextView mTxtDistance;
    public static TextView mTxtTime;
    private Button mBtnStart,mBtnPause,mBtnStop;
    static boolean status;
    LocationManager locationManager;
    static long startTime,endTime;
    static ProgressDialog progressDialog;
    static int p = 0;
    private LocationService myservice;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder)iBinder;
            myservice = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            status = false;
        }
    };

    @Override
    protected void onDestroy() {
        if (status){
            unbindService();
        }

        super.onDestroy();
    }

    private void unbindService() {
        if (!status){
            return;
        }
        Intent intent = new Intent(getApplicationContext(),LocationService.class);
        unbindService(serviceConnection);
        status = false;
    }

    @Override
    public void onBackPressed() {
        if (!status){
            onBackPressed();
        }else{
            moveTaskToBack(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"GRANTED", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"DENIED",Toast.LENGTH_SHORT).show();
                }
             break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtDistance = findViewById(R.id.distance);
        mTxtTime = findViewById(R.id.time);
        mBtnPause = findViewById(R.id.btnPause);
        mBtnStart = findViewById(R.id.btnStart);
        mBtnStop = findViewById(R.id.btnStop);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },1000);
            }
        }

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGPS();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    return;
                }
                if(!status){
                    bindService();
                }
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Getting location ...");
                progressDialog.show();

                mBtnStart.setVisibility(View.GONE);
                mBtnPause.setVisibility(View.VISIBLE);
                mBtnPause.setText("Pause");
                mBtnStop.setVisibility(View.VISIBLE);
            }
        });
        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBtnPause.getText().toString().equalsIgnoreCase("pause")){
                    mBtnPause.setText("Resume");
                    p = 1;
                }else if(mBtnPause.getText().toString().equalsIgnoreCase("resume")){
                    checkGPS();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        return;
                    }
                    mBtnPause.setText("Pause");
                }
            }
        });
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!status){
                    unbindService();
                }
                mBtnStart.setVisibility(View.VISIBLE);
                mBtnPause.setText("Pause");
                mBtnPause.setVisibility(View.GONE);
                mBtnStop.setVisibility(View.GONE);
            }
        });
    }

    private void checkGPS() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSDisabledAlert();
        }
    }

    private void showGPSDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS to use application").setCancelable(false)
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void bindService(){
        if (status){
            return;
        }
        Intent intent = new Intent(getApplicationContext(),LocationService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }
}
