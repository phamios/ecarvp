package net.vingroup.ecar;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.entity.EntityLogin;
import net.vingroup.ecar.fragment.FinishFragment;
import net.vingroup.ecar.fragment.HomeFragment;
import net.vingroup.ecar.fragment.InProcessFragment;
import net.vingroup.ecar.fragment.MainFragment;
import net.vingroup.ecar.fragment.MenuFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String SELECTED_ITEM = "arg_selected_item";
    private BottomNavigationView mBottomNav;
    private int mSelectedItem;
    private String receiveValue = null;

    String IMEIID = null;
    String DeviceID = null;
    String DeviceName = null;
    String OSVersion = null;


    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                IMEIID = tManager.getDeviceId();
            } else {
                IMEIID = null;
            }
        }catch (Exception e){
            Log.d("GET_IMEID: ",e.toString());
        }


        DeviceName = android.os.Build.MANUFACTURER + android.os.Build.MODEL;
        OSVersion = android.os.Build.MODEL;
        DeviceID = FirebaseInstanceId.getInstance().getToken();
        Bundle receiveBundle = this.getIntent().getExtras();
        receiveValue = receiveBundle.getString("_sitename");

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

        new InsertDeviceID().execute();
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
                frag = MainFragment.newInstance(receiveValue,getColorFromRes(R.color.color_home));
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
            ft.commit();
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
                jsonRequest.put("Description", "Vinpearl eCar Service");
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



}
