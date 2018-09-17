package com.example.mahmoudfcih.simpleblogapp;


import android.content.Intent;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.ScrollView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;

import java.util.Map;


public class SingleMessage extends AppCompatActivity {
   LinearLayout layout;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    DatabaseReference reference1;
    DatabaseReference reference2;
    String muser_id=null;
    String mperson_id=null;
    User user=new User();
    private String chatWithUser;
    DatabaseReference reference3;
    TextView User_name;
    TextView User_Location;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_message);
        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        User_name=(TextView)findViewById(R.id.user_name);
        User_Location=(TextView)findViewById(R.id.user_location);


        muser_id=getIntent().getExtras().getString("User_id");
        mperson_id=getIntent().getExtras().getString("person_id");

        reference1=FirebaseDatabase.getInstance().getReference().child("SingleMsg").child(muser_id + "_" + mperson_id);
        reference2=FirebaseDatabase.getInstance().getReference().child("SingleMsg").child(mperson_id + "_" + muser_id);
        reference3=FirebaseDatabase.getInstance().getReference().child("Users").child(mperson_id);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageArea.getText().toString();
                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<>();
                    map.put("message", messageText);
                    map.put("user", muser_id);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    }
            }
        });
        reference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String UName= (String) dataSnapshot.child("name").getValue();
                String UCity= (String) dataSnapshot.child("city").getValue();
                String UCountry= (String) dataSnapshot.child("country").getValue();
                User_name.setText(UName);
                User_Location.setText(UCountry+","+UCity);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String,String> map = (Map<String,String>) dataSnapshot.getValue();
                String message = map.get("message");
                String userid = map.get("user");
                if(userid.equals(muser_id)){
                   // addMessageBox("You:-\n" + message, 1);
                    addMessageBox(message, 1);
                    }
                else{
                 /*   final DatabaseReference chatWith=FirebaseDatabase.getInstance().getReference().child("Users").child(mperson_id);
                    chatWith.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            chatWithUser= (String) dataSnapshot.child("name").getValue();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });*/
                 //   addMessageBox(mperson_id + ":-\n" + message, 2);
                    addMessageBox(message, 2);
                    }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent loginIntent = new Intent(SingleMessage.this, Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }

    }

    private void addMessageBox(String message, int type) {

        TextView textView = new TextView(SingleMessage.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);

            }
        else{
            textView.setBackgroundResource(R.drawable.rounded_corner2);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
            }
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);

    }

}
