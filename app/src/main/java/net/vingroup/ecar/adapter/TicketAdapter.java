package net.vingroup.ecar.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Entity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dvmin on 1/19/2018.
 */

public class TicketAdapter extends ArrayAdapter<EntityTicket> {
    ArrayList<EntityTicket> bookingList = new ArrayList<EntityTicket>();
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
    ArrayList<String> worldlist = new ArrayList<>(0);
    private Spinner spinnerDriver;

    public TicketAdapter(Context context, int resource, ArrayList<EntityTicket> bookList, String listSiteMain) {
        super(context, resource, bookList);
        this.context = context;
        this.resource = resource;
        this.bookingList = bookList;
        this.listSite = listSiteMain;
    }

    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        BookingHolder holder = new BookingHolder();
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(resource, null, false);
            view = layoutInflater.inflate(R.layout.custom_listview, null);
            holder.txtRoom = (TextView) view.findViewById(R.id.txtRoom);
            holder.txtAddress = (TextView) view.findViewById(R.id.txtAddress);
            holder.bttStatus = (Button) view.findViewById(R.id.bttStatus);
            holder.frameevent = (RelativeLayout) view.findViewById(R.id.rowitemList);
            holder.txtDatecreate = (TextView) view.findViewById(R.id.txtCreateDate);
            holder.txtSiteName = (TextView) view.findViewById(R.id.SiteName);
            holder.txtDriver = (TextView) view.findViewById(R.id.txtDriver);
            view.setTag(holder);
        } else {
            holder = (BookingHolder) view.getTag();
        }
        TextView bookingRoom = view.findViewById(R.id.txtRoom);
        TextView bookingAddress = view.findViewById(R.id.txtAddress);
        TextView dateCreate = view.findViewById(R.id.txtCreateDate);
        TextView txtSitename = view.findViewById(R.id.SiteName);
        final Button bttStatus = view.findViewById(R.id.bttStatus);
        TextView txtDriver  = view.findViewById(R.id.txtDriver);
        bttStatus.setTag(position);


        if (bookingList.size() != 0) {
            bookingRoom.setText(bookingList.get(position).getPlace());
            bttStatus.setText(bookingList.get(position).getTotalTime());
            String[] txtTime = bookingList.get(position).getCreatedTime().split(" ");
            dateCreate.setText(txtTime[1]);
            txtSitename.setText(bookingList.get(position).getSiteName());
            bookingAddress.setText(bookingList.get(position).getTitle());
            txtDriver.setText("Lái xe:" + bookingList.get(position).getTechnicianName());

            holder.frameevent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listSite = bookingList.get(position).getSiteID();
                    workerID = String.valueOf(bookingList.get(position).getWorlOrderId());
                    final AppCompatDialog dialog = new AppCompatDialog(getContext());
                    dialog.setContentView(R.layout.dialog);
                    dialog.setTitle(bookingList.get(position).getTitle() + " - " + bookingList.get(position).getServiceName());
                    Button bttSubmit = (Button) dialog.findViewById(R.id.btn_yes);
                    Button bttHuychuyen = (Button) dialog.findViewById(R.id.btn_no);
                    spinnerDriver = (Spinner) dialog.findViewById(R.id.driverspinner);
                    worldlist.clear();
                    Log.d("SiteIDTechNical","SiteID: " + listSite);
                    new GetDriverAsyncTask().execute();
                    dialog.show();

                    spinnerDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view,int i, long l) {
                            String selectedItemText = (String) adapterView.getItemAtPosition(position);
                            driverCurrent = spinnerDriver.getSelectedItem().toString();
                            driverCurrent = worldlist.get(i);
                            Log.d("DRIVER SELECTED", ": " + selectedItemText + "|" + driverCurrent);
                            Toast.makeText(getContext(), "Đã chọn: " + spinnerDriver.getSelectedItem().toString(),  Toast.LENGTH_SHORT) .show();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });


                    bttSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ChangeStatus().execute();
                            bookingList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(getContext(), "Đã cập nhật thay đổi !.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                    bttHuychuyen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Bạn đã bỏ qua gán điều xe.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            });
        }
        return view;
    }

    public void setData(ArrayList<EntityTicket> data) {
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
            String getticketurl = Constant.APIURL + Constant.APIGETDRIVER;
            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("SiteId", listSite);
                String response = HttpClient.getInstance().post(getContext(), getticketurl, jsonRequest.toString());
                if (response.trim().equals("null")) {
//                    Toast.makeText(getContext(), "Do not have data !",  Toast.LENGTH_LONG) .show();
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("LISTDRIVER", "response : " + reader.toString());
                    if (reader.getString("responseMsg").trim().equals("Success")) {
                        myDriver.clear();
                        worldlist.clear();
                        JSONArray contacts = reader.getJSONArray("data");
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject c = contacts.getJSONObject(i);
                            String UserID = String.valueOf(c.getInt("UserID"));
                            String UserName = c.getString("UserName");
                            String FullName = c.getString("FullName");
                            myDriver.add(new EntityDriver(UserID, UserName, FullName));
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
            try{
                ArrayAdapter aa = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,worldlist);
                aa.notifyDataSetChanged();
                spinnerDriver.setAdapter(aa);

            } catch(Exception e){
                Toast.makeText(getContext(),"Có lỗi trong quá trình lấy danh sách lái xe",Toast.LENGTH_SHORT).show();
            }
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
            String getticketurl = Constant.APIURL + Constant.APIASSIGNDRIVER;

            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("WorkOrderId", workerID);
                jsonRequest.put("StatusName", "Đang chờ xử lý");
                jsonRequest.put("Technician", driverCurrent);
                String response = HttpClient.getInstance().post(getContext(), getticketurl, jsonRequest.toString());
                if (response.trim().equals("null")) {
//                    Toast.makeText(getContext(), "Do not have data !",  Toast.LENGTH_LONG) .show();
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("LISTDRIVER", "response : " + reader.toString());
                    String data = reader.getString("data");
                    JSONObject reader2 = new JSONObject(data);
                    JSONArray respond = reader2.getJSONArray("result");
                    String status = null;
                    String message = null;
                    for (int i = 0; i < respond.length(); i++) {
                        JSONObject c = respond.getJSONObject(i);
                        status = c.getString("status");
                        message = c.getString("message");
                    }
                    if (status.trim().equals("Success")) {
                        Log.d("UPDATESTATUS", "Respond: Success");
                    } else {
                        Log.d("UPDATESTATUS", "Respond: Failed");
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

}

class BookingHolder {

    TextView txtAddress;
    TextView txtRoom;
    TextView txtDatecreate;
    TextView txtDangcho;
    TextView txtDangdieuxe;
    TextView txtDaDon;
    Button bttStatus;
    RelativeLayout frameevent;
    TextView txtSiteName;
    TextView txtDriver;
}

