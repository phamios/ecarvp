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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dvmin on 1/19/2018.
 */

public class InProcessAdapter extends ArrayAdapter<EntityTicket> {
    ArrayList<EntityTicket> bookingList = new ArrayList<EntityTicket>();
    EntityTicket ticket;
    ArrayList<HashMap<String, String>> listDriver;
    ArrayList<EntityDriver> myDriver = new ArrayList<>(0);
    Context context;
    int resource;
    private ProgressDialog pDialog;
    private String listSite;
    Spinner spinnerDriver;
    private String[] items;
    String workerID = null;
    String driverCurrent = null;
    ArrayList<String> worldlist = new ArrayList<>(0);

    public InProcessAdapter(Context context, int resource, ArrayList<EntityTicket> bookList,String listSiteMain) {
        super(context, resource, bookList);
        this.context = context;
        this.resource = resource;
        this.bookingList = bookList;
        this.listSite = listSiteMain;
    }

    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        BookingHolderInProcess holder = new BookingHolderInProcess();
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
            holder.txtDriver = (TextView) view.findViewById(R.id.txtDriver);
            view.setTag(holder);
        }else
        {
            holder = (BookingHolderInProcess) view.getTag();
        }
        //getting the view elements of the list from the view
        TextView bookingRoom = view.findViewById(R.id.txtRoom);
        TextView bookingAddress = view.findViewById(R.id.txtAddress);
        TextView dateCreate = view.findViewById(R.id.txtCreateDate);
        TextView txtSitename = view.findViewById(R.id.SiteName);
        final Button bttStatus = view.findViewById(R.id.bttStatus);
        TextView txtDriver  = view.findViewById(R.id.txtDriver);
        bttStatus.setTag(position);


        if(bookingList.size() != 0 ) {
            if(bookingList.get(position).getStatusName().trim().equals("Mới tạo")){
                bttStatus.setBackgroundResource(R.drawable.round_button_chuadieu);
            }else if(bookingList.get(position).getStatusName().trim().equals("Đang chờ xử lý")){
                bttStatus.setBackgroundResource(R.drawable.round_button_dangden);
            }else if(bookingList.get(position).getStatusName().trim().equals("Đã hoàn thành")){
                bttStatus.setBackgroundResource(R.drawable.round_button_dadon);
            }
            bookingRoom.setText(bookingList.get(position).getPlace() );
            bookingAddress.setText(bookingList.get(position).getTitle());
            bttStatus.setText(bookingList.get(position).getTotalTime());
            dateCreate.setText("Cập nhật: " +bookingList.get(position).getUpdated_Date());
            txtSitename.setText(bookingList.get(position).getSiteName());
            txtDriver.setText(bookingList.get(position).getTechnicianName());
            holder.frameevent.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    workerID = String.valueOf(bookingList.get(position).getWorlOrderId());
                    final AppCompatDialog dialog = new AppCompatDialog(getContext());
                    dialog.setContentView(R.layout.inprocess_dialog);
                    dialog.setTitle(bookingList.get(position).getTitle() + " - " + bookingList.get(position).getServiceName());
                    Button bttSubmit = (Button) dialog.findViewById(R.id.btn_yes);
                    bttSubmit.setText("Hoàn thành");
                    Button bttHuychuyen  = (Button) dialog.findViewById(R.id.btn_no);
                    TextView txtDriverName = (TextView) dialog.findViewById(R.id.txtDriverName);
                    txtDriverName.setText(bookingList.get(position).getTechnicianName());
                    dialog.show();
                    bttSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ChangeStatus().execute();
                            bookingList.remove(position);
                            notifyDataSetChanged();
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
    private class ChangeStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = Constant.APIURL + Constant.APIUPDATETICKET;
            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("WorkOrderId", workerID);
                jsonRequest.put("StatusName", "Đã hoàn thành");
                jsonRequest.put("Technician",driverCurrent);
                String response = HttpClient.getInstance().post(getContext(),getticketurl, jsonRequest.toString());
                if(response.trim().equals("null")){
//                    Toast.makeText(getContext(), "Do not have data !",  Toast.LENGTH_LONG) .show();
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("LISTDRIVER", "response : "+reader.toString());
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

}

class BookingHolderInProcess {

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

