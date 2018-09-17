package com.example.mahmoudfcih.simpleblogapp.NotifyService;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.mahmoudfcih.simpleblogapp.Constant.consts;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceNotification extends IntentService {
    public static boolean ServiceIsRun=false;
    private FirebaseAuth mAuth;
    private String url= consts.url_get;



    public ServiceNotification() {
        super("MyWebRequestService");
    }

    protected void onHandleIntent(Intent workIntent) {

        // continue sending the messages
        while ( ServiceIsRun) {
            //* get new news

            SharedPreferences prefs = getSharedPreferences("NotificationId", MODE_PRIVATE);
            String restoredText = prefs.getString("text", null);
            int ID=prefs.getInt("id",0);
            int Id;
            if(ID == 0){
                Id=ID;
            }else {
                Id=ID-1;
            }
        //    int Id=ID-1;

          //  String myurl = "http://expressapp920170628103240.azurewebsites.net/restaurants?id="+Id;
            String myurl = url+Id;
            try
            {
                URL url = new URL(myurl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                 NewsData=Stream2String(in);
                in.close();
                String msg="";
                mAuth=FirebaseAuth.getInstance();

                // read jsn data
                JSONArray json = new JSONArray(NewsData);
                for (int i = 0; i < json.length(); i++) {
                    JSONObject newDataItem = json.getJSONObject(i);
                    if(newDataItem.getString("PersonName").equals(mAuth.getCurrentUser().getUid().toString())){
                        msg+=  "Your Person , "+ newDataItem.getString("comment") +" has found Similar!";
                        SharedPreferences.Editor editor = getSharedPreferences("NotificationId", MODE_PRIVATE).edit();
                        editor.putInt("id", ID+1);
                        editor.commit();
                    }else {
                        msg=null;
                    }
                    ID=  newDataItem.getInt("id");
                    // MY_PREFS_NAME - a static String variable like:
//public static final String MY_PREFS_NAME = "MyPrefsFile";

                }



                // creat new intent
                Intent intent = new Intent();
                //set the action that will receive our broadcast
                intent.setAction("com.example.Broadcast");
                // add data to the bundle
                intent.putExtra("msg", msg);
                // send the data to broadcast
                sendBroadcast(intent);
                //delay for 50000ms

            } catch (Exception e) {

             }

            try{
                Thread.sleep(20000);
            }catch (Exception ex){}


        }
    }


    String NewsData="";

    public String Stream2String(InputStream inputStream) {
        BufferedReader bureader=new BufferedReader( new InputStreamReader(inputStream));
        String line ;
        String Text="";
        try{
            while((line=bureader.readLine())!=null) {
                Text+=line;
            }
            inputStream.close();
        }catch (Exception ex){}
        return Text;
    }
}
