package net.vingroup.ecar.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import net.vingroup.ecar.MainActivity;
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
import java.util.Locale;

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
    String currentFirstName;
    private SearchableSpinner spinnerDriver;
    int CurrentAPICall = 0;
    String pushNote;
    ArrayAdapter spinnerAdapter;
    String statusAccept ;

    public MainAdapter(Context context, int resource, ArrayList<EntityTicket> bookList,String listSiteMain) {
        super(context, resource, bookList);
        this.context = context;
        this.resource = resource;
        this.bookingList = bookList;
        this.listSite = listSiteMain;
        this.arraylist = new ArrayList<EntityTicket>();
        this.arraylist.addAll(bookList);
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
            holder.txtDriver = (TextView) view.findViewById(R.id.txtDriver);
            holder.timestart = (TextView) view.findViewById(R.id.timestart);
            holder.timepending = (TextView) view.findViewById(R.id.timepending);
            holder.timeend = (TextView) view.findViewById(R.id.timeend);
            view.setTag(holder);
        }else
        {
            holder = (BookingHolder) view.getTag();
        }
        //getting the view elements of the list from the view
        TextView bookingRoom = view.findViewById(R.id.txtRoom);
        final TextView bookingAddress = view.findViewById(R.id.txtAddress);
        TextView dateCreate = view.findViewById(R.id.txtCreateDate);
        TextView txtSitename = view.findViewById(R.id.SiteName);
        final Button bttStatus = view.findViewById(R.id.bttStatus);
        TextView txtDriver  = view.findViewById(R.id.txtDriver);
        TextView timePending = view.findViewById(R.id.timepending);
        TextView timeStart = view.findViewById(R.id.timestart);
        TextView timeEnd = view.findViewById(R.id.timeend);

        bttStatus.setTag(position);

        if(bookingList.size() != 0 ) {
            bookingRoom.setText(bookingList.get(position).getPlace() );
            Log.d("LastStatus","respond: " + bookingList.get(position).getStatusName().trim());
            if(bookingList.get(position).getStatusName().trim().equals("Mới tạo")){
                bttStatus.setBackgroundResource(R.drawable.round_button_chuadieu);
                currentStatus = "Đang chờ xử lý";
            }else if(bookingList.get(position).getStatusName().trim().equals("Đang chờ xử lý")){
                bttStatus.setBackgroundResource(R.drawable.round_button_dangden);
                currentStatus = "Đã hoàn thành";
            }else if(bookingList.get(position).getStatusName().trim().equals("Đã hoàn thành")){
                bttStatus.setBackgroundResource(R.drawable.round_button_dadon);
            }

            if(bookingList.get(position).getStatusID().equals("1")){
                String[] txtTimePending = bookingList.get(position).getCreatedTime().split(" ");
                timePending.setText(txtTimePending[1]);
                timeStart.setText("");
                timeEnd.setText("");
            }


            if(bookingList.get(position).getStatusID().equals("2")){
                if(bookingList.get(position).getUpdated_Date().length() >= 2){
                    String[] txtTimeStart = bookingList.get(position).getUpdated_Date().split(" ");
                    Log.d("TimeEnd","respond: " + txtTimeStart[0] + "|" + txtTimeStart[1]);
                    timeStart.setText( txtTimeStart[1]);
                }
                String[] txtTimePending = bookingList.get(position).getCreatedTime().split(" ");
                timePending.setText(txtTimePending[1]);
                timeEnd.setText("");
             }

            if( bookingList.get(position).getStatusID().equals("3")){
                String[] txtTimePending = bookingList.get(position).getCreatedTime().split(" ");
                timePending.setText(txtTimePending[1]);

                if(bookingList.get(position).getUpdated_Date().trim().equals("")){

                } else {
                    String[] txtTimeStart = bookingList.get(position).getUpdated_Date().split(" ");
                    Log.d("TimeEnd","respond: " + txtTimeStart[0] + "|" + txtTimeStart[1]);
                    timeStart.setText(  txtTimeStart[1]);
                }

                if(bookingList.get(position).getCompletedTime().trim().equals("")){

                }else{
                    String[] txtTimeEnd = bookingList.get(position).getCompletedTime().split(" ");
                    Log.d("TimeEnd","respond: " + txtTimeEnd[0] + "|" + txtTimeEnd[1]);
                    timeEnd.setText(  txtTimeEnd[1]);
                }
            }

            bttStatus.setText(bookingList.get(position).getTotalTime());
            String[] txtTime = bookingList.get(position).getCreatedTime().split(" ");
            dateCreate.setText(txtTime[1]);
            txtSitename.setText(bookingList.get(position).getTitle());
            bookingAddress.setText(bookingList.get(position).getSiteName());
            txtDriver.setText("Lái xe: " + bookingList.get(position).getTechnicianName());


            holder.frameevent.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listSite = bookingList.get(position).getSiteID();
                    workerID = String.valueOf(bookingList.get(position).getWorlOrderId());

                    if(bookingList.get(position).getStatusName().trim().equals("Mới tạo")){
                        bttStatus.setBackgroundResource(R.drawable.round_button_chuadieu);
                        CurrentAPICall = 1;
                        statusAccept = "Đang chờ xử lý";
                    }else if(bookingList.get(position).getStatusName().trim().equals("Đang chờ xử lý")){
                        bttStatus.setBackgroundResource(R.drawable.round_button_dangden);
                        CurrentAPICall = 2;
                        statusAccept = "Đã hoàn thành";
                    }else if(bookingList.get(position).getStatusName().trim().equals("Đã hoàn thành")){
                        bttStatus.setBackgroundResource(R.drawable.round_button_dadon);
                        CurrentAPICall = 3;
                    }

                    Log.d("statusAccept","Status: " + bookingList.get(position).getStatusName() + " | "  + CurrentAPICall);

                    final AppCompatDialog dialog = new AppCompatDialog(getContext());
                    dialog.setContentView(R.layout.dialog);
                    dialog.setTitle(bookingList.get(position).getTitle() + " - " + bookingList.get(position).getServiceName());
                    final Button bttSubmit = (Button) dialog.findViewById(R.id.btn_yes);
                    final EditText txtNote = (EditText)dialog.findViewById(R.id.editNote);
                    txtNote.setText(bookingList.get(position).getNotesText());
                    bttSubmit.setEnabled(false);
                    currentFirstName = bookingList.get(position).getRequester().toString();
                    TextView txtTitle = (TextView)dialog.findViewById(R.id.txt_dia);

                    pushNote = txtNote.getText().toString();

                    Log.d("DRIVER SELECTED", ": "  + driverCurrent);

                    Log.d("StatusHERE","respond: " + bookingList.get(position).getStatusName());
                    if(bookingList.get(position).getStatusName().trim().equals("Mới tạo")){
                        bttSubmit.setText("Điều xe");
                        worldlist.clear();
                        Log.d("SiteIDTechNical","SiteID: " + listSite);
                    }else if(bookingList.get(position).getStatusName().trim().equals("Đang chờ xử lý")){
                        bttSubmit.setText("Hoàn Thành");
                    }
                    final Button bttHuychuyen  = (Button) dialog.findViewById(R.id.btn_no);

                    if(CurrentAPICall == 2 || CurrentAPICall == 3){
                        bttHuychuyen.setText(" Sửa ");
                    }else {
                        bttHuychuyen.setText(" Không ");
                    }

                    if(CurrentAPICall == 3){
                        bttSubmit.setVisibility(View.GONE);
                    }


                    dialog.show();

                    if(bookingList.get(position).getTechnicianName().length() > 1){
                        txtTitle.setText("Lái xe: " + bookingList.get(position).getTechnicianName());
                    }
                    spinnerDriver= (SearchableSpinner)dialog.findViewById(R.id.driverspinner);
                    new GetDriverAsyncTask().execute();
                    if(spinnerAdapter != null) {
                        if (bookingList.get(position).getTechnicianName().trim() != null) {
                            int spinnerPosition = spinnerAdapter.getPosition(bookingList.get(position).getTechnicianName().trim());
                            spinnerDriver.setSelection(spinnerPosition);
//                            driverCurrent = spinnerDriver.getSelectedItem().toString();
                        }
                    }

                    spinnerDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view,int i, long l) {
//                                String selectedItemText = (String) adapterView.getItemAtPosition(position);
//                                String text = spinnerDriver.getSelectedItem().toString();
                                driverCurrent = spinnerDriver.getSelectedItem().toString();
                                driverCurrent = worldlist.get(i);
                                Toast.makeText(getContext(), "Đã chọn: " +spinnerDriver.getSelectedItem().toString(),  Toast.LENGTH_SHORT) .show();

                                if(driverCurrent.trim() == "------ Chọn lái xe-----"){
                                    bttSubmit.setEnabled(false);
                                    bttSubmit.setClickable(false);
                                } else {
                                    bttSubmit.setEnabled(true);
                                    bttSubmit.setClickable(true);
                                }

                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });


                    bttSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("DRIVERCURRENTNOW","Response: " + driverCurrent);
                            Log.d("CurrentAPICALL","response: " + CurrentAPICall);
                            bookingList.remove(position);
                            new ChangeStatus().execute();
                            notifyDataSetChanged();
                            pushNote = txtNote.getText().toString();
                            if(pushNote.trim().toString().length() >= 2){
                                new addNoteTask().execute();
                            }
                            Toast.makeText(getContext(), "Đã cập nhật trạng thái !.",  Toast.LENGTH_SHORT) .show();
                            dialog.dismiss();
                        }
                    });


                    bttHuychuyen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(CurrentAPICall == 2 || CurrentAPICall == 3){
                                new UpdateTask().execute();
                                pushNote = txtNote.getText().toString();
                                if(pushNote.trim().toString().length() >= 2){
                                    new addNoteTask().execute();
                                }
                                Toast.makeText(getContext(), "Đã cập nhật thay đổi !.",  Toast.LENGTH_SHORT) .show();
                                dialog.dismiss();
                            }else {
                                pushNote = txtNote.getText().toString();
                                if(pushNote.trim().toString().length() >= 2){
                                    new addNoteTask().execute();
                                }
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Bạn đã bỏ qua gán điều xe.",  Toast.LENGTH_SHORT) .show();
                            }

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
                String response = HttpClient.getInstance().post(getContext(),getticketurl, jsonRequest.toString());
                if(response.trim().equals("null")){

                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("LISTDRIVER", "response : "+reader.toString());
                    if(reader.getString("responseMsg").trim().equals("Success")){
                        myDriver.clear();
                        worldlist.clear();
                        if(CurrentAPICall == 2){
                            worldlist.add("------ Đổi lái xe-----");
                        }else{
                            worldlist.add("------ Chọn lái xe-----");
                        }

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
            try{

                spinnerAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,worldlist);
                spinnerAdapter.notifyDataSetChanged();
                spinnerDriver.setAdapter(spinnerAdapter);


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
            String getticketurl = null;

            getticketurl = Constant.APIURL + Constant.APIASSIGNDRIVER;

            Log.d("CurrentAPICall_SYNC","respond: " + CurrentAPICall);
            Log.d("statusAccept","respond: " + statusAccept);
            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("WorkOrderId", workerID);
                jsonRequest.put("StatusName",statusAccept );
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
            notifyDataSetChanged();
        }
    }


    /**
     * Get Driver Async Task Automatic
     */
    private class UpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = null;
            getticketurl = Constant.APIURL + Constant.APIASSIGNDRIVER;
            Log.d("CurrentAPICall_SYNC","respond: " + CurrentAPICall);

            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("WorkOrderId", workerID);
                jsonRequest.put("StatusName","" );
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
            notifyDataSetChanged();
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

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        bookingList.clear();
        if (charText.length() == 0) {
            bookingList.addAll(arraylist);
        }
        else
        {
            for (EntityTicket wp : arraylist)
            {
                if (wp.getTitle().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    bookingList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Add Note
    private class addNoteTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject jsonRequest = new JSONObject();
            String getticketurl = null;

            getticketurl = Constant.APIURL + Constant.APIADDNOTE;
            Log.e("DRIVERGET", "Response from url: " + getticketurl);
            try {
                jsonRequest.put("workorderid", workerID);
                jsonRequest.put("notestext",pushNote );
                jsonRequest.put("firstname",currentFirstName);
                String response = HttpClient.getInstance().post(getContext(),getticketurl, jsonRequest.toString());
                Log.i("PUT_NOTE", "POST : "+jsonRequest.toString());
                if(response.trim().equals("null")){
//                    Toast.makeText(getContext(), "Do not have data !",  Toast.LENGTH_LONG) .show();
                } else {
                    JSONObject reader = new JSONObject(response);
                    Log.i("CHANGESTATUS", "response : "+reader.toString());
                    String data = reader.getString("data");
                    JSONObject reader2 = new JSONObject(data);
                    JSONArray respond = reader2.getJSONArray("result");
                    String status = null;
                    JSONObject c = respond.getJSONObject(0);
                    status =  c.getString("status");
                    if(status.trim().equals("Success")){
                        Toast.makeText(context.getApplicationContext(), "Cập nhật ghi chú thành công !",  Toast.LENGTH_SHORT) .show();
                    } else {
                        Toast.makeText(context.getApplicationContext(), "Cập nhật ghi chú thất bại !",  Toast.LENGTH_SHORT) .show();
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
            notifyDataSetChanged();
        }
    }

}

