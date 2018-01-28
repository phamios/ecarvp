package net.vingroup.ecar;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.entity.EntityLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by dvmin on 1/18/2018.
 */

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    String TAG = "ECAR-LOGIN";
    Button _loginButton;
    String[] domain ={"-","VINGROUP.LOCAL"};
    String current_domain = "";
    EditText _emailText = null;
    EditText _password = null;
    Spinner spinDomain  ;
    private ProgressDialog pDialog;
    private static final int REQUEST_READ_PHONE_STATE = 0;
    String IMEIID = null;
    String DeviceID = null;
    String phonenum, IMEI;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private boolean shouldRecreate = false;
    SharedPreferences sp;
    boolean movingInApp = false;

    private void initialise() {
        _emailText = (EditText)findViewById(R.id.input_email);
        _password = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        spinDomain = (Spinner) findViewById(R.id.usertype);
    }
    String sitename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialise();


        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                IMEIID = tel.getDeviceId().toString();
            } catch (Exception e) {
                phonenum = "Error!!";
                IMEIID = "Error!!";
            }
        }

        spinDomain.setOnItemSelectedListener(this);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,domain);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDomain.setAdapter(aa);

        if(isNetworkOnline()){
            _loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!validate()) {
                        onLoginFailed();
                        return;
                    } else {
                        new MyAsyncTask().execute();
                    }

                }
            });
        } else {
            Toast.makeText(getApplicationContext(),"Network not available",Toast.LENGTH_SHORT).show();
        }


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



    private class MyAsyncTask extends AsyncTask<Void, Void, Void>
    {
        ProgressDialog mProgressDialog;
        @Override
        protected void onPostExecute(Void result) {
            if (pDialog.isShowing())
                pDialog.dismiss();
            sharedPref = getApplicationContext().getSharedPreferences("VINECAR", Context.MODE_PRIVATE);
            editor = sharedPref.edit();
            editor.putString("_site", sitename);
            editor.putString("username", _emailText.getText().toString());
            editor.putString("password", _password.getText().toString());
            editor.putBoolean("connected", true);

            editor.commit();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONObject jsonRequest = new JSONObject();
            String registerUrl = Constant.APIURL + Constant.APILOGIN;
            try {
                jsonRequest.put("Username", _emailText.getText().toString());
                jsonRequest.put("Password", _password.getText().toString());
                jsonRequest.put("Domain", current_domain);
                String response = HttpClient.getInstance().post(getApplicationContext(),registerUrl, jsonRequest.toString());
                if(response.trim().equals("null")){
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu",  Toast.LENGTH_LONG) .show();
                        }
                    });
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i(TAG, "response : "+reader.toString());
                    if(reader.getString("responseMsg").trim().equals("Success")){
                        JSONObject dataRespond  = reader.getJSONObject("data");
                        JSONObject jsonObj = new JSONObject(dataRespond.toString());
                        sitename = jsonObj.getString("SiteName");
                        movingInApp = true;
                        Bundle sendBundle = new Bundle();
                        sendBundle.putInt("UserID",jsonObj.getInt("UserID"));
                        sendBundle.putString("FirstName",jsonObj.getString("FirstName"));
                        sendBundle.putString("_sitename", jsonObj.getString("SiteName"));
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.putExtras(sendBundle);
                        startActivity(i);
                        finish();
                    } else {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu",  Toast.LENGTH_LONG) .show();
                            }
                        });
                    }
                }
            }catch (JSONException e){
                Log.e(TAG, "JSON Exception", e);
            } catch (IOException e) {
                Log.e(TAG, "HttpClient Exception", e);
            } catch(Exception e){
                Log.e(TAG, "Exception ", e);
                return null;
            }

            Log.d(TAG, "Post request to Register: " + registerUrl);
            Log.d(TAG, "json : "+jsonRequest.toString());
            return null;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
//        Toast.makeText(getApplicationContext(),"Đã chọn: " + domain[position], Toast.LENGTH_SHORT).show();
        current_domain = domain[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }


    /**
     * ==========================================================================
     */
    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _password.getText().toString();

        if (email.isEmpty()) {
            _emailText.setError("Vui lòng điền mã nhân viên");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if(current_domain != "-"){
            if (password.isEmpty() ) {
                _password.setError("Mật khẩu chưa đúng");
                valid = false;
            } else {
                _password.setError(null);
            }
        }
        return valid;
    }

    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            boolean isWiFi = netInfo.getType() == ConnectivityManager.TYPE_WIFI;
            if(isWiFi){
                Toast.makeText(getBaseContext(), "You are using Wifi !", Toast.LENGTH_SHORT).show();
            }
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences myPrefs = this.getSharedPreferences("VINECAR",MODE_WORLD_READABLE);
        myPrefs.edit().clear();
        myPrefs.edit().commit();
        Log.d("onDesTroy App", "BEMBEM LOGIN");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            shouldRecreate = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ECar Resume","OK");
        movingInApp = false;

    }


}
