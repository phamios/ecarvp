package net.vingroup.ecar.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.vingroup.ecar.R;
import net.vingroup.ecar.Util.Constant;
import net.vingroup.ecar.Util.HttpClient;
import net.vingroup.ecar.entity.EntityDriver;
import net.vingroup.ecar.entity.EntityTicket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dvmin on 1/19/2018.
 */

public class MainAdapter extends ArrayAdapter<EntityTicket>  {
    ArrayList<EntityTicket> bookingList = new ArrayList<EntityTicket>();
    private ArrayList<EntityTicket> arraylist;
    EntityTicket ticket;
    ArrayList<HashMap<String, String>> listDriver;
    ArrayList<EntityDriver> myDriver = new ArrayList<>(0);
    Context context;
    int resource;
    private ProgressDialog pDialog;
    private String listSite;
    private String[] items;
    String workerID = null;
    String driverCurrent = null;
    String currentStatus = null;
    ArrayList<String> worldlist = new ArrayList<>(0);
    Spinner spinnerDriver;
    int CurrentAPICall = 0;

    public MainAdapter(Context context, int resource, ArrayList<EntityTicket> bookList,String listSiteMain) {
        super(context, resource, bookList);
        this.context = context;
        this.resource = resource;
        this.bookingList = bookList;
        this.listSite = listSiteMain;
    }

    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        BookingHolder holder = new BookingHolder();
        if(view == null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(resource, null, false);
            view = layoutInflater.inflate(R.layout.custom_listview, null);
            holder.txtRoom = (TextView)view.findViewById(R.id.txtRoom);
            holder.txtAddress = (TextView)view.findViewById(R.id.txtAddress);
            holder.bttStatus = (Button) view.findViewById(R.id.bttStatus);
            holder.frameevent = (RelativeLayout) view.findViewById(R.id.rowitemList);
            holder.txtDatecreate = (TextView) view.findViewById(R.id.txtCreateDate);
            holder.txtSiteName = (TextView) view.findViewById(R.id.SiteName);
            view.setTag(holder);
        }else
        {
            holder = (BookingHolder) view.getTag();
        }
        //getting the view elements of the list from the view
        TextView bookingRoom = view.findViewById(R.id.txtRoom);
        TextView bookingAddress = view.findViewById(R.id.txtAddress);
        TextView dateCreate = view.findViewById(R.id.txtCreateDate);
        TextView txtSitename = view.findViewById(R.id.SiteName);
        final Button bttStatus = view.findViewById(R.id.bttStatus);

        bttStatus.setTag(position);

        if(bookingList.size() != 0 ) {
            bookingRoom.setText(bookingList.get(position).getPlace() );
            if(bookingList.get(position).getStatusName().trim().equals("Mới tạo")){
                bttStatus.setBackgroundResource(R.drawable.round_button_chuadieu);
                currentStatus = "Đang chờ xử lý";
                CurrentAPICall = 1;
            }else if(bookingList.get(position).getStatusName().trim().equals("Đang chờ xử lý")){
                bttStatus.setBackgroundResource(R.drawable.round_button_dangden);
                currentStatus = "Đã hoàn thành";
                CurrentAPICall = 2;
            }else if(bookingList.get(position).getStatusName().trim().equals("Đã hoàn thành")){
                bttStatus.setBackgroundResource(R.drawable.round_button_dadon);
            }

            bttStatus.setText(bookingList.get(position).getTotalTime());
            dateCreate.setText(bookingList.get(position).getCreatedTime());
            txtSitename.setText(bookingList.get(position).getSiteName());
            bookingAddress.setText(bookingList.get(position).getTitle());

            if(bookingList.get(position).getStatusName().trim().equals("Đã hoàn thành")){

            } else {
                holder.frameevent.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        workerID = String.valueOf(bookingList.get(position).getWorlOrderId());
                        final AppCompatDialog dialog = new AppCompatDialog(getContext());
                        dialog.setContentView(R.layout.dialog);
                        dialog.setTitle(bookingList.get(position).getTitle() + " - " + bookingList.get(position).getServiceName());
                        Button bttSubmit = (Button) dialog.findViewById(R.id.btn_yes);
                        if(bookingList.get(position).getStatusName().trim().equals("Mới tạo")){
                            bttSubmit.setText("Điều xe");
                            currentStatus = "Đang chờ xử lý";
                        }else if(bookingList.get(position).getStatusName().trim().equals("Đang chờ xử lý")){
                            bttSubmit.setText("Hoàn Thành");
                            currentStatus = "Đã hoàn thành";
                        }
                        new GetDriverAsyncTask().execute();
                        Button bttHuychuyen  = (Button) dialog.findViewById(R.id.btn_no);
                        spinnerDriver = (Spinner) dialog.findViewById(R.id.driverspinner);
                        worldlist.clear();

                        try{
                            final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.spinner_item,worldlist){
                                @Override
                                public View getDropDownView(int position, View convertView,ViewGroup parent) {
                                    View view = super.getDropDownView(position, convertView, parent);
                                    TextView tv = (TextView) view;
                                    if(position%2 == 1) {
                                        tv.setBackgroundColor(Color.parseColor("#FFC9A3FF"));
                                    }
                                    else {
                                        tv.setBackgroundColor(Color.parseColor("#FFAF89E5"));
                                    }
                                    return view;
                                }
                            };
                            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                            spinnerArrayAdapter.notifyDataSetChanged();
                            spinnerDriver.setAdapter(spinnerArrayAdapter);
                            spinnerDriver.setSelection(1);
                        } catch(Exception e){
                            Log.i("LISTDRIVER ERROR", e.toString());
                            Toast.makeText(getContext(),"Có lỗi trong quá trình lấy danh sách lái xe",Toast.LENGTH_SHORT).show();
                        }

                        dialog.show();

//                        spinnerDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                            @Override
//                            public void onItemSelected(AdapterView<?> adapterView, View view,int i, long l) {
//                                String selectedItemText = (String) adapterView.getItemAtPosition(position);
//                                driverCurrent = spinnerDriver.getSelectedItem().toString();
//                                Log.d("DRIVER SELECTED", ": " + selectedItemText + "|" + driverCurrent);
//                                Toast.makeText(getContext(), spinnerDriver.getSelectedItem().toString(),  Toast.LENGTH_SHORT) .show();
//                            }
//                            @Override
//                            public void onNothingSelected(AdapterView<?> adapterView) {
//                            }
//                        });

                        bttSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bookingList.remove(position);
                                notifyDataSetChanged();
                                new ChangeStatus().execute();
                                Toast.makeText(getContext(), "Đã cập nhật thay đổi !.",  Toast.LENGTH_SHORT) .show();
                                dialog.dismiss();
                            }
                        });

                        bttHuychuyen.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Bạn đã bỏ qua gán điều xe.",  Toast.LENGTH_SHORT) .show();
                            }
                        });



                    }
                });
            }


        }

        return view;
    }


    public void setData(ArrayList<EntityTicket> data)
    {
        bookingList = data;
        notifyDataSetChanged();
    }


    /**
     * Get Driver Async Task Automatic
     */
    private class GetDriverAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = getticketurl = Constant.APIURL + Constant.APIGETDRIVER;
            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("SiteId", listSite);
                String response = HttpClient.getInstance().post(getContext(),getticketurl, jsonRequest.toString());
                if(response.trim().equals("null")){

                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("LISTDRIVER", "response : "+reader.toString());
                    if(reader.getString("responseMsg").trim().equals("Success")){
                        myDriver.clear();
                        worldlist.clear();
                        JSONArray contacts = reader.getJSONArray("data");
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject c = contacts.getJSONObject(i);
                            String UserID = String.valueOf(c.getInt("UserID"));
                            String UserName= c.getString("UserName");
                            String FullName = c.getString("FullName");
                            myDriver.add(new EntityDriver(UserID,UserName,FullName  ));
                            worldlist.add(FullName);
                        }
                    } else {
                        worldlist = null;
                    }
                }

            } catch (final JSONException e) {
                Log.e("ERROR DRIVERLIST", "Json parsing error: " + e.getMessage());


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
//            try{
//            } catch(Exception e){
//                Log.i("LISTDRIVER ERROR", e.toString());
//                Toast.makeText(getContext(),"Có lỗi trong quá trình lấy danh sách lái xe",Toast.LENGTH_SHORT).show();
//            }
        }
    }


    /**
     * Get Driver Async Task Automatic
     */
    private class ChangeStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = null;
            if(CurrentAPICall == 1){
                getticketurl = Constant.APIURL + Constant.APIASSIGNDRIVER;
            } else if(CurrentAPICall == 2){
                getticketurl = Constant.APIURL + Constant.APIUPDATETICKET;
            }
            getticketurl = Constant.APIURL + Constant.APIUPDATETICKET;

            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("WorkOrderId", workerID);
                jsonRequest.put("StatusName",currentStatus );
                jsonRequest.put("Technician",driverCurrent);
                String response = HttpClient.getInstance().post(getContext(),getticketurl, jsonRequest.toString());
                Log.i("CHANGESTATUS", "POST : "+jsonRequest.toString());
                if(response.trim().equals("null")){
//                    Toast.makeText(getContext(), "Do not have data !",  Toast.LENGTH_LONG) .show();
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("CHANGESTATUS", "response : "+reader.toString());
                    String data = reader.getString("data");
                    JSONObject reader2 = new JSONObject(data);
                    JSONArray respond = reader2.getJSONArray("result");
                    String status = null;
                    String message = null;
                    for (int i = 0; i < respond.length(); i++) {
                        JSONObject c = respond.getJSONObject(i);
                        status =  c.getString("status");
                        message = c.getString("message");
                    }
                    if(status.trim().equals("Success")){
                        Log.d("UPDATESTATUS","Respond: Success");
                    } else {
                        Log.d("UPDATESTATUS","Respond: Failed");
                    }
                }

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

    @Override
    public int getCount() {
        return bookingList.size();
    }

    @Override
    public EntityTicket getItem(int position) {
        return bookingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public void filter(String charText) {
//        charText = charText.toLowerCase(Locale.getDefault());
//         bookingList.clear();
//        if (charText.length() == 0) {
//            bookingList.addAll(arraylist);
//        }
//        else
//        {
//            for (EntityTicket wp : arraylist)
//            {
//                if (wp.getServiceName().toLowerCase(Locale.getDefault()).contains(charText))
//                {
//                    bookingList.add(wp);
//                }
//            }
//        }
//        notifyDataSetChanged();
    }



}
 
