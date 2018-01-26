package net.vingroup.ecar.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import net.vingroup.ecar.MainActivity;
import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import q.rorbin.badgeview.QBadgeView;

/**
 * Created by dvmin on 1/26/2018.
 */

public class AutoUpdateBroadcastReceiver extends BroadcastReceiver {
    final public static String ONE_TIME = "onetime";
    int totalWait = 0;
    int totalInProcess = 0;
    private String receiveValue = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        //Acquire the lock
        wl.acquire();

        //You can do the processing here.
        Bundle extras = intent.getExtras();
        StringBuilder msgStr = new StringBuilder();

        if(extras != null && extras.getBoolean(ONE_TIME, Boolean.FALSE)){
            //Make sure this intent has been sent by the one-time timer button.
            msgStr.append("One time Timer : ");
        }
        Format formatter = new SimpleDateFormat("hh:mm:ss a");
        msgStr.append(formatter.format(new Date()));

        Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();

        //Release the lock
        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AutoUpdateBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After after 5 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 , pi);
    }


    private class getTotalTicket extends AsyncTask<Context, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Context... arg0) {
            totalWait = 0;
            totalInProcess = 0;
            JSONObject jsonRequest = new JSONObject();
            String registerUrl = Constant.APIURL + Constant.APIGETTICKET;
            try {
                jsonRequest.put("SiteId", receiveValue);
                String response = HttpClient.getInstance().post(arg0[0],registerUrl, jsonRequest.toString());
                if(response.trim().equals("null")){
                   Log.d("ServiceError: ","Respond" + "Do not have data !");
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
                        Log.d("ServiceError","respond: "+ "Error Data Dump!");
                    }
                }

            } catch (final JSONException e) {
               Log.d("Service","respond" + e);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }


}
