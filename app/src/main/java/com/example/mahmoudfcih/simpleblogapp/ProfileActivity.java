package com.example.mahmoudfcih.simpleblogapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ProfileActivity extends AppCompatActivity {
    private RecyclerView mBlog_list;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCurrentUser;
    private FirebaseAuth mAuth;
    private Query mQueryCurrentUser;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth.AuthStateListener mAuthlistner;
    private String UserId;
    private static final String USERIDKEY = "useridkey";
    private Button editbutton;
    private ImageView User_profile;
    private TextView User_Name;
    private TextView User_email;
    private TextView User_location;
    private ScrollView editview;
    String userProfile;
    private Button canceledit;
    private TextView hidenUri;
    private static final String genderArray[] = {"Male", "Female"};

    private TextView account_password;
    private EditText account_username;
    private EditText account_city;
    private EditText account_country;
    private EditText account_birthdate;
    private Spinner account_gender;
    private Button saveedit;
    private String ImageUri;

    private ProgressDialog progressDialog;

    //region Validation

    private boolean isUsernameValid(EditText usernameview) {
        usernameview.setError(null);
        String username = usernameview.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameview.setError(getString(R.string.error_field_required));
        } else if (username.length() < 6) {
            usernameview.setError(getString(R.string.error_invalid_username));
        }  else return true;
        usernameview.requestFocus();
        return false;
    }


    private boolean isCityorCountryValid(EditText citynameview) {
        citynameview.setError(null);
        String city = citynameview.getText().toString();
        if (TextUtils.isEmpty(city)) {
            citynameview.setError(getString(R.string.error_field_required));
        } else if (city.length() < 2) {
            citynameview.setError(getString(R.string.error_invalid_cityorcountry));
        }  else return true;
        citynameview.requestFocus();
        return false;
    }

    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("loading");

        editbutton=(Button)findViewById(R.id.editprofile);
        User_Name=(TextView)findViewById(R.id.full_name);
        User_profile=(ImageView)findViewById(R.id.user_image);
        User_email=(TextView)findViewById(R.id.user_email);
        User_location=(TextView)findViewById(R.id.userlocation);
        editview=(ScrollView)findViewById(R.id.scrolledit);
        mBlog_list=(RecyclerView)findViewById(R.id.Single_Blog_list);

        ///////////////////////////////////////////////////////////
        saveedit=(Button)findViewById(R.id.saveedit);
        canceledit=(Button)findViewById(R.id.canceledit);
        account_password = (TextView) findViewById(R.id.password_account);
        account_username = (EditText) findViewById(R.id.username_account);
        account_city = (EditText) findViewById(R.id.city_account);
        account_country = (EditText) findViewById(R.id.country_account);
        account_birthdate = (EditText) findViewById(R.id.birthday_account);
        account_gender = (Spinner) findViewById(R.id.gender_account);
        hidenUri=(TextView)findViewById(R.id.hidenUri);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, genderArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        account_gender.setAdapter(spinnerArrayAdapter);
        account_gender.setPrompt("Select Gender");
        progressDialog.show();
        mAuth=FirebaseAuth.getInstance();
        String UserID=mAuth.getCurrentUser().getUid();
        new FireBaseHelper.Users().getUser(UserID, new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                FireBaseHelper.Users us = (FireBaseHelper.Users) Data;
                account_username.setText(us.Name);
                account_country.setText(us.Country);
                account_birthdate.setText(us.Birthday);
                account_gender.setSelection(us.Gender == genderArray[0] ? 0 : 1);
                account_city.setText(us.City);
                hidenUri.setText(us.Image);

                progressDialog.dismiss();
            }

        });



        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBlog_list.setVisibility(View.INVISIBLE);
                editview.setVisibility(View.VISIBLE);

            }
        });

        canceledit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBlog_list.setVisibility(View.VISIBLE);
                editview.setVisibility(View.INVISIBLE);

            }
        });
        saveedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FireBaseHelper.Users users = new FireBaseHelper.Users();

                if (!isUsernameValid(account_username)) ;
                if (!isCityorCountryValid(account_city)) ;
                if (!isCityorCountryValid(account_country)) ;
                else {
                    progressDialog.show();
                    FireBaseHelper.Users USER = new FireBaseHelper.Users();
                    USER.Name=account_username.getText().toString().trim();
                    USER.Key=mAuth.getCurrentUser().getUid();
                    USER.City=account_city.getText().toString().trim();
                    USER.Country=account_country.getText().toString().trim();
                    USER.Birthday=account_birthdate.getText().toString().trim();
                    USER.Gender=account_gender.getSelectedItem().toString();
                    USER.Image=hidenUri.getText().toString().trim();

                    USER.UpdateUser(USER.Key);
                    progressDialog.dismiss();
                    mBlog_list.setVisibility(View.VISIBLE);
                    editview.setVisibility(View.INVISIBLE);

                }

            }
        });

        mAuth=FirebaseAuth.getInstance();
        mAuthlistner=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent= new Intent(ProfileActivity.this,Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");

        UserId=getIntent().getStringExtra(USERIDKEY);


        if(UserId.equals(mAuth.getCurrentUser().getUid())){
            editbutton.setVisibility(View.VISIBLE);
        }
        mDatabaseUsers.child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userProfile= (String) dataSnapshot.child("image").getValue();
                String Username=(String)dataSnapshot.child("name").getValue();
                String Usercountry=(String)dataSnapshot.child("country").getValue();
                String Usercity=(String)dataSnapshot.child("city").getValue();


                User_Name.setText(Username);
                User_location.setText(Usercity + "," + Usercountry);
                if(UserId.equals(mAuth.getCurrentUser().getUid())) {
                    User_email.setText(mAuth.getCurrentUser().getEmail());
                }
                Picasso.with(getApplicationContext()).load(userProfile).networkPolicy(NetworkPolicy.OFFLINE).into(User_profile, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplicationContext()).load(userProfile).into(User_profile);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDatabaseCurrentUser=FirebaseDatabase.getInstance().getReference().child("Blog");
        mQueryCurrentUser=mDatabaseCurrentUser.orderByChild("uid").equalTo(UserId);
        mBlog_list=(RecyclerView)findViewById(R.id.Single_Blog_list);
        mBlog_list.setHasFixedSize(true);
        mBlog_list.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseUsers.keepSynced(true);

        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_raw,
                BlogViewHolder.class,
                mQueryCurrentUser
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, int position) {
              //  viewHolder.setTitle(model.getTitle());
                viewHolder.setdesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setLocation(model.getAddress());
                viewHolder.setUserprofile(getApplicationContext(),userProfile);



            }
        };
        mBlog_list.setAdapter(firebaseRecyclerAdapter);


    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthlistner);

    }



    public static class BlogViewHolder extends RecyclerView.ViewHolder{
        View mView;
        private ProgressBar progressbar;


        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;


        }

    /*    public void setTitle(String title)
        {
            TextView post_title=(TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }*/
        public void setdesc(String desc)
        {
            TextView post_desc=(TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setUsername(String username)
        {
            TextView post_username=(TextView)mView.findViewById(R.id.post_username);

            post_username.setText(username);
        }
        public void setImage(final Context ctx, final String Image)
        {
            progressbar=(ProgressBar)mView.findViewById(R.id.progressBar);
            final ImageView post_image=(ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(Image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(Image).into(post_image);

                }
            });
        }
        public void setLocation(String location){
            TextView post_location=(TextView)mView.findViewById(R.id.post_location);
            post_location.setText(location);
        }
        public void setUserprofile(final Context ctx, final String image){
            final ImageView userprof=(ImageView)mView.findViewById(R.id.user_profile);

            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(userprof, new Callback() {
                @Override
                public void onSuccess() {
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).into(userprof);

                }
            });

        }



    }


}
