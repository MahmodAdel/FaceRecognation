package com.example.mahmoudfcih.simpleblogapp;


import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


public class UsersList extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthlistner;
    private FirebaseAuth mAuth;
    private FirebaseListAdapter<User> adapter;
    private TextView username;
    private ImageView userimage;
    private FirebaseUser mCurrentuser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent loginIntent = new Intent(UsersList.this, Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }else {
            mAuth=FirebaseAuth.getInstance();
            mCurrentuser=mAuth.getCurrentUser();
            final String user_id=mCurrentuser.getUid();
            displayUserList(user_id);

        }


        }

    private void displayUserList(final String user_id) {
        ListView listofUsers=(ListView)findViewById(R.id.lvUsers);
        adapter=new FirebaseListAdapter<User>(
             this,
                User.class,
                R.layout.users_raw,
                FirebaseDatabase.getInstance().getReference().child("Users")
        ) {
            @Override
            protected void populateView(View v, final User model, int position) {
                username=(TextView) v.findViewById(R.id.lvv_user);
                userimage=(ImageView)v.findViewById(R.id.lvv_image);
                username.setText(model.getName());
                Picasso.with(UsersList.this).load(model.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(userimage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.with(UsersList.this).load(model.getImage()).into(userimage);
                    }

                    @Override
                    public void onError() {

                    }
                });


            }

        };
        listofUsers.setAdapter(adapter);
        listofUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String person_id= String.valueOf(adapter.getRef(position).getKey());
                if(person_id.equals(user_id)){
                    Toast.makeText(UsersList.this, "That is YourSelf", Toast.LENGTH_SHORT).show();
                }else {
                    Intent MsgIntent = new Intent(getApplicationContext(), SingleMessage.class);
                    MsgIntent.putExtra("User_id", user_id);
                    MsgIntent.putExtra("person_id",person_id);
                    startActivity(MsgIntent);
                }

            }
        });
    }

}
