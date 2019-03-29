package com.team.homeautomation;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stealthcopter.networktools.ARPInfo;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity {
    ImageView bgapp, clover;
    LinearLayout textsplash, texthome, menus;
    Animation frombottom;
    ImageView bulb1on, bulb1off, bulb2on, bulb2off, switchon, switchoff, fan;
    GifImageView gifImageView;
    TextView textView;
    HashMap<String, String> ipmac;
    String macAddress = "5c:cf:7f:18:76:4f", ip = ""; //5c:cf:7f:18:76:4f
    Boolean isanimationshow = true,show=false;
    ProgressBar progressBar;
    private int width, height;
    boolean Fan = true, swith = true, bulb1 = true, bulb2 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView2);
        bulb1on = findViewById(R.id.bulb1on);
        bulb1off = findViewById(R.id.bulb1off);
        bulb2on = findViewById(R.id.bulb2on);
        bulb2off = findViewById(R.id.bulb2off);
        switchon = findViewById(R.id.switchon);
        switchoff = findViewById(R.id.switchoff);
        fan = findViewById(R.id.fanoff);
        gifImageView = findViewById(R.id.fanon);
        progressBar = findViewById(R.id.progressBar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        frombottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);


        bgapp = findViewById(R.id.bgapp);
        clover = findViewById(R.id.clover);
        textsplash = findViewById(R.id.textsplash);
        texthome = findViewById(R.id.texthome);
        menus = findViewById(R.id.menus);
        wifichecking();

        if (isanimationshow) {
            menus.setVisibility(View.INVISIBLE);
            bgapp.animate().translationY(-(height + height / 11)).setDuration(800).setStartDelay(1500);
            clover.animate().alpha(0).setDuration(800).setStartDelay(1700);
            textsplash.animate().translationY(height / 3).alpha(0).setDuration(800).setStartDelay(1500);
            texthome.startAnimation(frombottom);

        } else {
            texthome.setVisibility(View.INVISIBLE);
            menus.setVisibility(View.INVISIBLE);
        }
        textView.setVisibility(View.INVISIBLE);
    }

    public void fan(View v) {
        if (Fan) {
            String flink = "http://"+ip+"/LED3=ON";
            webrequest(flink);
            fan.setVisibility(View.INVISIBLE);
            gifImageView.setVisibility(View.VISIBLE);
            Fan = false;
        } else {
            String flink = "http://"+ip+"/LED3=OFF";
            webrequest(flink);
            fan.setVisibility(View.VISIBLE);
            gifImageView.setVisibility(View.INVISIBLE);
            Fan = true;
        }

    }

    public void swith(View v) {
        if (swith) {
            String flink = "http://"+ip+"/LED2=ON";
            webrequest(flink);
            switchoff.setVisibility(View.INVISIBLE);
            switchon.setVisibility(View.VISIBLE);
            swith = false;
        } else {
            String flink = "http://"+ip+"/LED2=OFF";
            webrequest(flink);
            switchoff.setVisibility(View.VISIBLE);
            switchon.setVisibility(View.INVISIBLE);
            swith = true;
        }
    }

    public void bulb1(View v) {
        if (bulb1) {
            bulb1off.setVisibility(View.INVISIBLE);
            bulb1on.setVisibility(View.VISIBLE);
            String flink = "http://"+ip+"/LED1=ON";
            webrequest(flink);
            bulb1 = false;
        } else {
            String flink = "http://"+ip+"/LED1=OFF";
            webrequest(flink);
            bulb1off.setVisibility(View.VISIBLE);
            bulb1on.setVisibility(View.INVISIBLE);
            bulb1 = true;
        }
    }

    public void bulb2(View v) {
        if (bulb2) {
            String flink = "http://"+ip+"/LED4=ON";
            webrequest(flink);
            bulb2off.setVisibility(View.INVISIBLE);
            bulb2on.setVisibility(View.VISIBLE);
            bulb2 = false;
        } else {
            String flink = "http://"+ip+"/LED4=OFF";
            webrequest(flink);
            bulb2off.setVisibility(View.VISIBLE);
            bulb2on.setVisibility(View.INVISIBLE);
            bulb2 = true;
        }
    }

    public void wifichecking() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            new GetIP().execute();
        } else {
            isanimationshow = false;
            new AlertDialog.Builder(this)
                    .setTitle("Wifi connection")
                    .setMessage("You don't connect with any wifi network please connect it and restart the app")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    public class GetIP extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    progressBar.isShown();
                }
            }, 2500);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(50);
                SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                    @Override
                    public void onDeviceFound(Device device) {
                        // Stub: Found subnet device
                    }

                    @Override
                    public void onFinished(ArrayList<Device> devicesFound) {
                        // Stub: Finished scanning
                        ipmac = ARPInfo.getAllIPAndMACAddressesInARPCache();
                        for (Map.Entry<String, String> hell : ipmac.entrySet()) {
                            String key = hell.getKey();
                            String value = hell.getValue();
                            if (value.equals(macAddress)) {
                                ip = key;
                                show = true;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Log.i("makemachine", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            GenLinks(ip);
        }

        private void GenLinks(String s) {
            if (show){
            menus.setVisibility(View.VISIBLE);
            menus.startAnimation(frombottom);
            textView.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_LONG).show();
            textView.setText(s);}
            else {
                Toast.makeText(getApplicationContext(), "IP not Found Please Try again..", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean webrequest(String s){
        OkHttpClient client = new OkHttpClient();

        Request request =  new Request.Builder()
                            .url(s)
                            .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("TAG",e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG","onResponseMethod Called");
            }
        });
        return false;
    }

}