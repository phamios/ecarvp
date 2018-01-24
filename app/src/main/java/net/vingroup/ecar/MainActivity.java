package net.vingroup.ecar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.fragment.FinishFragment;
import net.vingroup.ecar.fragment.HomeFragment;
import net.vingroup.ecar.fragment.InProcessFragment;
import net.vingroup.ecar.fragment.MainFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String SELECTED_ITEM = "arg_selected_item";
    private static final int REQUEST_READ_PHONE_STATE = 0;
    private BottomNavigationView mBottomNav;
    private int mSelectedItem;
    private String receiveValue = null;
    private boolean shouldRecreate = false;
    String IMEIID = null;
    String DeviceID = null;
    String DeviceName = null;
    String OSVersion = null;
    String phonenum, IMEI;
    int totalWait = 0;
    int totalInProcess = 0;

    @SuppressLint("HardwareIds")
    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                IMEIID = tel.getDeviceId().toString();
            } catch (Exception e) {
                phonenum = "Error!!";
                IMEIID =  "Error!!";
            }
        }

        DeviceName = android.os.Build.MANUFACTURER + android.os.Build.MODEL;
        OSVersion = android.os.Build.MODEL;
        DeviceID = FirebaseInstanceId.getInstance().getToken();
        Bundle receiveBundle = this.getIntent().getExtras();

        SharedPreferences sharedPreferences= this.getSharedPreferences("VINECAR", Context.MODE_PRIVATE);
        receiveValue = sharedPreferences.getString("_site", "");//receiveBundle.getString("_sitename");

        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNav.setItemIconTintList(null);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });


        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = mBottomNav.getMenu().findItem(mSelectedItem);
        } else {
            selectedItem = mBottomNav.getMenu().getItem(0);
        }
        selectFragment(selectedItem);

    }

    public void initialUISetup()
    {
        new InsertDeviceID().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            IMEIID = tel.getDeviceId().toString();
                        }
                    } catch (Exception e) {
                        phonenum = "Error!!";
                        IMEIID = tel.getDeviceId().toString();
                    }
                }
                break;

            default:
                break;
        }
    }
    

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = mBottomNav.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            // select home item
            selectFragment(homeItem);
        } else {
            super.onBackPressed();
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.menu_home:
                frag = HomeFragment.newInstance(receiveValue,getColorFromRes(R.color.color_home));
                break;
            case R.id.menu_dangdon:
                frag = InProcessFragment.newInstance(receiveValue,getColorFromRes(R.color.color_notifications));

                break;
            case R.id.menu_daxong:
                frag = FinishFragment.newInstance(receiveValue,getColorFromRes(R.color.color_search));
                break;
            case R.id.menu_main:
                new getTotalTicket().execute();
                Log.d("LOGSERVICE","test" + totalWait + "-" +totalInProcess);
                frag = MainFragment.newInstance(totalWait,totalInProcess,receiveValue,getColorFromRes(R.color.color_home));
        }

        // update selected item
        mSelectedItem = item.getItemId();

        // uncheck the other items.
        for (int i = 0; i< mBottomNav.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNav.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == item.getItemId());
        }


        updateToolbarText(item.getTitle());

        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, frag, frag.getTag());
            ft.detach(frag).attach(frag).commit();
        }


    }

    protected void setFragment(Fragment fragment) {
        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.container, fragment);
        t.commit();
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    private int getColorFromRes(@ColorRes int resId) {
        return ContextCompat.getColor(this, resId);
    }
    /**
     * ==============================InsertDeviceID===============================
     */
    private class InsertDeviceID extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = Constant.APIURL + Constant.APIINSERTDEVICE;
            Log.e("InsertDEVICE", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("Imei", IMEIID);
                jsonRequest.put("DeviceID", DeviceID);
                jsonRequest.put("ListSiteID", receiveValue);
                jsonRequest.put("DeviceName", DeviceName);
                jsonRequest.put("OsVersion", OSVersion);
                jsonRequest.put("Description", "Vinpearl Service");
                Log.d("INSERT DEVICES","POST: "+ jsonRequest.toString());
                String response = HttpClient.getInstance().post(getApplication(),getticketurl, jsonRequest.toString());
                Log.i("INSERT DEVICES", "response : "+response.toString());

            } catch (final JSONException e) {
                Log.e("ERROR UPDATESTATUS", "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR UPDATESTATUS", "IOException: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }


    private class getTotalTicket extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            totalWait = 0;
            totalInProcess = 0;
            JSONObject jsonRequest = new JSONObject();
            String registerUrl = Constant.APIURL + Constant.APIGETTICKET;
            try {
                jsonRequest.put("SiteId", receiveValue);
                String response = HttpClient.getInstance().post(MainActivity.this,registerUrl, jsonRequest.toString());
                if(response.trim().equals("null")){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Do not have data !",  Toast.LENGTH_LONG) .show();
                        }
                    });
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("CountTicket", "response : "+reader.toString());
                    if(reader.getString("responseMsg").trim().equals("Success")){
                        JSONArray contacts = reader.getJSONArray("data");
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject c = contacts.getJSONObject(i);
                            if(c.getString("StatusName").trim().equals("Mới tạo")){
                                totalWait = totalWait + 1;
                            } else if(c.getString("StatusName").trim().equals("Đang chờ xử lý")){
                                totalInProcess = totalInProcess + 1;
                            }
                        }
                    } else {
                       MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error Data Dump!",  Toast.LENGTH_LONG) .show();
                            }
                        });
                    }
                }

            } catch (final JSONException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( MainActivity.this,
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
//            txtWait.setText(totalWait + " yêu cầu");
//            txtOngoing.setText(totalInProcess + " yêu cầu");
        }
    }




    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            shouldRecreate = true;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        initialUISetup();
//        SharedPreferences sharedPreferences= this.getSharedPreferences("VINECAR", Context.MODE_PRIVATE);
//        receiveValue = sharedPreferences.getString("_site", "");//receiveBundle.getString("_sitename");
//        SharedPreferences sharedPreferences= this.getSharedPreferences("VINECAR", Context.MODE_PRIVATE);
//        if(sharedPreferences!= null) {
//            receiveValue = sharedPreferences.getString("_site", "");//receiveBundle.getString("_sitename");
//            startActivity(new Intent(this, MainActivity.class));
//        }
//        Log.v("shouldRecreate: ",String.valueOf(shouldRecreate));
//        if (shouldRecreate){
//            startActivity(new Intent(this, MainActivity.class));
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        SharedPreferences sharedPref = getSharedPreferences("VINECAR", Context.MODE_PRIVATE);
        SharedPreferences myPrefs = this.getSharedPreferences("VINECAR",Context.MODE_PRIVATE);
        myPrefs.edit().remove("_site");
        myPrefs.edit().clear();
        myPrefs.edit().commit();
        getSharedPreferences("VINECAR", 0).edit().clear().commit(); // change PREF to yours
        Log.d("onDesTroy App", "BEMBEM Activity");


    }



}
