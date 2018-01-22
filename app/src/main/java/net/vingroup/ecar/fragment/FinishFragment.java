package net.vingroup.ecar.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.vingroup.ecar.LoginActivity;
import net.vingroup.ecar.MainActivity;
import net.vingroup.ecar.R;
import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.entity.EntityTicket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by dvmin on 1/19/2018.
 */

public class FinishFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_TEXT = "arg_text";
    private static final String ARG_COLOR = "arg_color";

    private String listSite;
    private int mColor;

    private View mContent;
    private TextView mTextView;

    private String TAG = FinishFragment.class.getSimpleName();
    SwipeRefreshLayout swipeLayout;
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> listTicket;

    public static Fragment newInstance(String text, int color) {
        Fragment frag = new FinishFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putInt(ARG_COLOR, color);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finish, container, false);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // retrieve text and color from bundle or savedInstanceState
        listTicket = new ArrayList<>();
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            listSite = args.getString(ARG_TEXT);
            mColor = args.getInt(ARG_COLOR);
        } else {
            listSite = savedInstanceState.getString(ARG_TEXT);
              mColor = savedInstanceState.getInt(ARG_COLOR);
        }

        // initialize views
        mContent = view.findViewById(R.id.fragment_content_finish);
        mContent.setBackgroundColor(mColor);

        lv = (ListView) view.findViewById(R.id.listViewFinish);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefreshFinish);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listTicket.clear();
                new GetContacts().execute();
                swipeLayout.setRefreshing(false);
            }
        });
        swipeLayout.setColorSchemeColors(android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark);
        new GetContacts().execute();

    }

    @Override
    public void onRefresh() {

        new GetContacts().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = Constant.APIURL + Constant.APIGETTICKET;
            Log.e(TAG, "Response from url: " + getticketurl);
            try {
                jsonRequest.put("SiteId", listSite);
                String response = HttpClient.getInstance().post(getActivity(),getticketurl, jsonRequest.toString());
                if(response.trim().equals("null")){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Do not have data !",  Toast.LENGTH_LONG) .show();
                        }
                    });
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i(TAG, "response : "+reader.toString());
                    if(reader.getString("responseMsg").trim().equals("Success")){
                        JSONArray contacts = reader.getJSONArray("data");
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject c = contacts.getJSONObject(i);
                            if(c.getString("StatusID").trim().equals("3")){
                                String RowNumber = String.valueOf(c.getInt("RowNumber"));
                                String WorlOrderId= String.valueOf(c.getInt("WorlOrderId"));
                                String Title = c.getString("Title");
                                String SiteName = c.getString("SiteName");
                                String Requester = c.getString("Requester");
                                String ServiceName = c.getString("Requester");
                                String CategoryName = c.getString("CategoryName");
                                String CreatedTime = c.getString("CreatedTime");
                                String DueByTime = c.getString("DueByTime");
                                String CompletedTime = c.getString("CompletedTime");
                                String ResolvedTime = c.getString("ResolvedTime");
                                String Priority = c.getString("Priority");
                                String StatusName = c.getString("StatusName");
                                String Place = c.getString("Place");
                                String TotalTime = c.getString("TotalTime");
                                String OverTime = c.getString("OverTime");
                                String StatusAlert = c.getString("StatusAlert");

                                HashMap<String, String> contact = new HashMap<>();
                                contact.put("RowNumber", RowNumber);
                                contact.put("WorlOrderId", WorlOrderId);
                                contact.put("Title", Title);
                                contact.put("SiteName", SiteName);
                                contact.put("Requester", Requester);
                                contact.put("ServiceName", ServiceName);
                                contact.put("CategoryName", CategoryName);
                                contact.put("CreatedTime", CreatedTime);
                                contact.put("DueByTime", DueByTime);
                                contact.put("CompletedTime", CompletedTime);
                                contact.put("ResolvedTime", ResolvedTime);
                                contact.put("Priority", Priority);
                                contact.put("StatusName", StatusName);
                                contact.put("Place", Place);
                                contact.put("TotalTime", TotalTime);
                                contact.put("OverTime", OverTime);
                                contact.put("StatusAlert", StatusAlert);

                                listTicket.add(contact);
                            }
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Error Data Dump!",  Toast.LENGTH_LONG) .show();
                            }
                        });
                    }
                }

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), listTicket,
                    R.layout.list_item, new String[]{"WorlOrderId", "Title",
                    "ServiceName","CategoryName"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile,R.id.category});

            lv.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_TEXT, listSite);
        outState.putInt(ARG_COLOR, mColor);
        super.onSaveInstanceState(outState);
    }

}
