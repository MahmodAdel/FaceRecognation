package com.example.mahmoudfcih.simpleblogapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.SearchView;


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
import java.util.concurrent.TimeUnit;

import com.example.mahmoudfcih.simpleblogapp.NotifyService.ServiceNotification;
import tabs.SlidingTabLayout;




public class MainActivity extends AppCompatActivity {
    public TextView UName;
    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthlistner;
    private DatabaseReference mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
    private Toolbar toolbar;
    private static final String USERIDKEY = "useridkey";
    public Typeface tf1,tf2;
    public TextView welcome;

    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Home","Notification","TakeImage"};

    int Numboftabs =3;
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth =FirebaseAuth.getInstance();
        mAuthlistner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();

                }
            }
        };
        UName=(TextView)findViewById(R.id.usernamee);
        welcome=(TextView)findViewById(R.id.welcome);
        tf1=Typeface.createFromAsset(getAssets(),"fonts/ChiselMark.ttf");
        tf2=Typeface.createFromAsset(getAssets(),"fonts/Sketch.ttf");
        welcome.setTypeface(tf2);


        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser() == null){
                    finish();
                }else {
                    String USERNAME= dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("name").getValue().toString();
                    UName.setText(USERNAME);
                    UName.setTypeface(tf1);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ////////////////////////////////////////////////////////////////////////////////
        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);
        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorPrimary);
                }
            });
        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
//////////////////////////////////////////////////////////
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        drawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
///////////////////////////////////////////////////////////////////////////////////////////////////
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        checkUserExit();



    }



    public void checkUserExit() {

        if(mAuth.getCurrentUser() !=null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(setupIntent);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthlistner);
        if (ServiceNotification.ServiceIsRun == false) {
            ServiceNotification.ServiceIsRun = true;
            //register the services to run in background
            Intent intent = new Intent(getApplicationContext(), ServiceNotification.class);
            // start the services
            startService(intent);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        moveTaskToBack(true);
                    }
                }).create().show();

    }

    private boolean checkWriteExternalPermission()
    {

        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton mlikebtn;
        DatabaseReference mDatabaselike;
        DatabaseReference mDatabase;
        FirebaseAuth mAuth;
        TextView numberoflikes;
        TextView numberofComments;
        TextView ViewallComments;
        Button mcommentbtn;
        TextView mcommenttxtview;
        ImageView imageProfile;
        LinearLayout ViewAllComments;
        ProgressBar progressbar;
        ImageButton mImageButton;
        Typeface tf2;




        public BlogViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            mlikebtn=(ImageButton)mView.findViewById(R.id.likebtn);
            mDatabaselike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mDatabase=FirebaseDatabase.getInstance().getReference().child("Blog");
            mAuth=FirebaseAuth.getInstance();
            mDatabaselike.keepSynced(true);
            mDatabase.keepSynced(true);
            numberoflikes=(TextView)mView.findViewById(R.id.numberoflike);
            numberofComments=(TextView)mView.findViewById(R.id.numberofcomment);
            ViewallComments=(TextView)mView.findViewById(R.id.numberofcomments);
            mcommenttxtview=(TextView)mView.findViewById(R.id.comment);
            ViewAllComments=(LinearLayout)mView.findViewById(R.id.comments_layout);
            imageProfile=(ImageView)mView.findViewById(R.id.user_profile);
            mImageButton = (ImageButton) mView.findViewById(R.id.item_menu);





        }
        public void numberofComments(String mpost_key) {
            DatabaseReference DatabaseNum=FirebaseDatabase.getInstance().getReference().child("Comments");
            Query mQueryCurrentUser=DatabaseNum.orderByChild("bid").equalTo(mpost_key);
            mQueryCurrentUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long num=dataSnapshot.getChildrenCount();
                    int nums =(int)num;
                    numberofComments.setText(Integer.toString(nums));
                    ViewallComments.setText(Integer.toString(nums));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setLikebtn(final String post_key){
            mDatabaselike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(mAuth.getCurrentUser() !=null) {

                        if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())  ){
                            mlikebtn.setImageResource(R.drawable.ic_thumb_up_purple_24dp);
                            DatabaseReference postlikes=mDatabaselike.child(post_key);
                            postlikes.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long num=dataSnapshot.getChildrenCount();
                                    int nums=(int)num;
                                    numberoflikes.setText(Integer.toString(nums));

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else {
                            mlikebtn.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                            DatabaseReference postlikes=mDatabaselike.child(post_key);
                            postlikes.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long num=dataSnapshot.getChildrenCount();
                                    int nums=(int)num;
                                    numberoflikes.setText(Integer.toString(nums));

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

      /*  public void setTitle(String title)
        {
            TextView post_title=(TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);

        }*/

        public void setLocation(String location)
        {
            TextView post_location=(TextView)mView.findViewById(R.id.post_location);
            post_location.setText(location);

        }
        public void setDate(String Date)
        {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            try {
                Date date = df.parse(Date);
                int diff = (int) getDateDiff(date, new Date(), TimeUnit.SECONDS);
                int dif=diff;
            //    int year = diff / 365;
             //   int Month = (diff % 365) / 30;
                int hour=(dif %86400) / 3600;
                int day = (dif / 86400);
                int min=dif %60;
                String hours="";
                String mins="";
                String Days = day == 0 ? "" : day + " d";
                if(day != 0){
                    hours="";
                    mins="";
                }else {
                    hours = hour == 0 ? "" : hour + " h";
                    mins = min == 0 ? "" : min + " min";
                }


                TextView post_date=(TextView)mView.findViewById(R.id.post_date);
                post_date.setText(Days+hours+mins);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        public void setDesc(String desc)
        {
            TextView post_desc=(TextView)mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setUsername(String username)
        {
            TextView post_username=(TextView)mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }
        public void setImage(final Context ctx, final String image){
            progressbar=(ProgressBar)mView.findViewById(R.id.progressBar);
            final ImageView post_image=(ImageView)mView.findViewById(R.id.post_image);
            // Picasso.with(ctx).load(image).into(post_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {
                    progressbar.setVisibility(View.GONE);

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).into(post_image);


                }
            });


        }


        public void setUserPorfile(final Context ctx, final String user_profile) {
            Picasso.with(ctx).load(user_profile).networkPolicy(NetworkPolicy.OFFLINE).into(imageProfile, new Callback() {
                @Override
                public void onSuccess() {
                    progressbar.setVisibility(View.GONE);

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(user_profile).into(imageProfile);


                }
            });

        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
       searchSetup(menu);

        return true;
    }
    public  void searchSetup(Menu menu){
        SearchManager searchmanager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchview=(SearchView)menu.findItem(R.id.action_search).getActionView();
        searchview.setSearchableInfo(searchmanager.getSearchableInfo(getComponentName()));
       // final Context co=this;

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new FireBaseHelper.Users().Where(FireBaseHelper.Users.Table.Name, query, new FireBaseHelper.OnGetDataListListener<FireBaseHelper.Users>() {
                    @Override
                    public void onSuccess(List<FireBaseHelper.Users> Data) {
                        if(Data.size() == 0){
                            Toast.makeText(getApplicationContext(), "User Not Found", Toast.LENGTH_LONG).show();
                        }else {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra(USERIDKEY, Data.get(0).Key);
                            startActivity(intent);
                        }
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
             //   Toast.makeText(co,newText,Toast.LENGTH_LONG).show();
                return false;
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        if(item.getItemId() == R.id.action_group){
            startActivity(new Intent(MainActivity.this,ChatActivity.class));

        }
        if(item.getItemId() == R.id.action_logout)
        {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }



    protected void logout() {
        FirebaseAuth.getInstance().signOut();
     //   int check=FirebaseAuth.getInstance().signOut();
      //  startActivity(new Intent(MainActivity.this,Login.class));
    }



}
