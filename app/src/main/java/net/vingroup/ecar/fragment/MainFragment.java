package net.vingroup.ecar.fragment;

/**
 * Created by sonpx1 on 1/22/2018.
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.vingroup.ecar.R;
import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.adapter.MainAdapter;
import net.vingroup.ecar.adapter.TicketAdapter;
import net.vingroup.ecar.entity.EntityTicket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
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

import net.vingroup.ecar.MainActivity;
import net.vingroup.ecar.R;
import net.vingroup.ecar.adapter.TicketAdapter;
import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.entity.EntityTicket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by dvmin on 1/19/2018.
 */

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_TEXT = "arg_text";
    private static final String ARG_COLOR = "arg_color";
    private static final String ARG_WAIT = "arg_wait";
    private static final String ARG_PROCESS = "arg_process";
    private String listSite;
    private int mColor;
    private View mContent;
    private TextView mTextView;
    private String TAG = MainFragment.class.getSimpleName();
    public static int totalInProcess = 0;
    public static int totalWait = 0;
    static TextView txtWait;
    static TextView txtOngoing;
    SwipeRefreshLayout swipeLayout;
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> listTicket;
    ArrayList<EntityTicket> myBook = new ArrayList<>(0);
    EditText txtSearchRoom;
    MainAdapter adapter;
    public static Fragment newInstance(int totalWait, int inprocess, String text, int color) {
        Fragment frag = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putInt(ARG_COLOR, color);
        args.putInt(ARG_WAIT,totalWait);
        args.putInt(ARG_PROCESS,inprocess);

        frag.setArguments(args);
        return frag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listTicket = new ArrayList<>();
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            listSite = args.getString(ARG_TEXT);
            mColor = args.getInt(ARG_COLOR);
            totalInProcess = args.getInt(ARG_WAIT);
            totalWait = args.getInt(ARG_PROCESS);
        } else {
            listSite = savedInstanceState.getString(ARG_TEXT);
            mColor = savedInstanceState.getInt(ARG_COLOR);
        }


        // initialize views
        mContent = view.findViewById(R.id.fragment_content_main);
        mContent.setBackgroundColor(mColor);
        txtWait = (TextView) view.findViewById(R.id.txtTongCho);
        txtOngoing = (TextView) view.findViewById(R.id.txtTongDangdi);

//        txtWait.setText(totalWait + " yêu cầu");
//        txtOngoing.setText(totalInProcess + " yêu cầu");

        Log.d("TotalWait",String.valueOf(totalWait));
        Log.d("TotalOnGoing",String.valueOf(totalInProcess));

        txtSearchRoom = (EditText)getActivity().findViewById(R.id.txtSearchRoom);
        txtSearchRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                String text = txtSearchRoom.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        });
        myBook.clear();
        lv = (ListView) view.findViewById(R.id.listViewMain);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefreshMain);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myBook.clear();
                totalWait = 0;
                totalInProcess = 0;
                new GetTicket().execute();
                swipeLayout.setRefreshing(false);
            }
        });
        swipeLayout.setColorSchemeColors(
                android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark);

        new GetTicket().execute();
    }

    @Override
    public void onRefresh() {
        myBook.clear();
        new GetTicket().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetTicket extends AsyncTask<Void, Void, Void> {

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
            totalWait = 0;
            totalInProcess = 0;
            JSONObject jsonRequest = new JSONObject();
            String registerUrl = Constant.APIURL + Constant.APIGETTICKET;
            Log.e(TAG, "Response from url: " + registerUrl);
            try {
                jsonRequest.put("SiteId", listSite);
                String response = HttpClient.getInstance().post(getActivity(),registerUrl, jsonRequest.toString());
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
                            String StatusID = c.getString("StatusID");
                            String Technical = c.getString("TechnicianName");
                            String updateDate = c.getString("Updated_Date");
                            myBook.add(new EntityTicket(
                                    Integer.valueOf(RowNumber),Integer.valueOf(WorlOrderId),Title,SiteName,
                                    Requester,ServiceName,CategoryName,CreatedTime,DueByTime,CompletedTime,
                                    ResolvedTime,Priority,StatusName,Place,TotalTime,OverTime,StatusAlert,StatusID,Technical,updateDate
                            ));
                            if(c.getString("StatusName").trim().equals("Mới tạo")){
                                totalWait = totalWait + 1;
                            } else if(c.getString("StatusName").trim().equals("Đang chờ xử lý")){
                                totalInProcess = totalInProcess + 1;
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
            adapter = new MainAdapter(getActivity(), R.layout.custom_listview, myBook,listSite);
            adapter.setData(myBook);
            lv.setAdapter(adapter);
            txtWait.setText(totalWait + " yêu cầu");
            txtOngoing.setText(totalInProcess + " yêu cầu");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_TEXT, listSite);
        outState.putInt(ARG_COLOR, mColor);
        outState.putInt(ARG_WAIT,totalWait);
        outState.putInt(ARG_PROCESS,totalInProcess);
        super.onSaveInstanceState(outState);
    }


}

