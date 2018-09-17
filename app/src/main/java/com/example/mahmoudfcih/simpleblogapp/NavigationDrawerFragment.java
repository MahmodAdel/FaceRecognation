package com.example.mahmoudfcih.simpleblogapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.mahmoudfcih.simpleblogapp.persongroupmanagement.PersonGroupListActivity;
import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;

import ProfileImage.CropCircleTransform;


public class NavigationDrawerFragment extends Fragment  implements VivzAdapter.ClickListener {
    private ActionBarDrawerToggle mDrawerToggle;
    private static final String USERIDKEY = "useridkey";
    private DrawerLayout mDrawerlayout;
    private boolean mUserLearnedDrawer;
    private boolean mFromeSaveInstanceState;
    private static final String PREF_FILE_NAME="testpref";
    public static final String KEY_USER_LEARNED_DRAWER="user_learned_drawer";
    private View  containerview;
    private RecyclerView recyclerView;
    private VivzAdapter adapter;
    private Context mContext;
    private DatabaseReference mUserProfile;
    private FirebaseAuth mAuth;
    public NavigationDrawerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer=Boolean.valueOf(readFromPreference(getActivity(), KEY_USER_LEARNED_DRAWER,"false"));
        if(savedInstanceState !=null)
        {
            mFromeSaveInstanceState=true;
        }




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAuth=FirebaseAuth.getInstance();
        View layout=inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        final TextView textUsername=(TextView)layout.findViewById(R.id.text_username);
        final ImageView imageProfile=(ImageView) layout.findViewById(R.id.image_profile);
        mUserProfile= FirebaseDatabase.getInstance().getReference().child("Users");
        if(mAuth.getCurrentUser() !=null) {
            String currentUserid = mAuth.getCurrentUser().getUid();
            FireBaseHelper.Users USER = new FireBaseHelper.Users();
            USER.getUser(currentUserid, new FireBaseHelper.OnGetDataListener() {
                @Override
                public void onSuccess(Object Data) {
                    FireBaseHelper.Users us = (FireBaseHelper.Users) Data;
                        textUsername.setText(us.Name);
                        Glide.with(getActivity())
                                .load(us.Image)
                                .asBitmap()
                                .transform(new CropCircleTransform(mContext))
                                .into(imageProfile);

                }
            });

        }



        // Inflate the layout for this fragment
        recyclerView= (RecyclerView) layout.findViewById(R.id.drawer_list);
        adapter=new VivzAdapter(getActivity(),getData());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new Clicklistener() {
            @Override
            public void onClick(View view, int position) {
                if(position == 1) {
                    mAuth=FirebaseAuth.getInstance();
                    String Userid=mAuth.getCurrentUser().getUid();
                    Intent profileIntent=new Intent(getActivity(),ProfileActivity.class);
                    profileIntent.putExtra(USERIDKEY,Userid);
                    startActivity(profileIntent);
                }
                else if(position == 2)
                {
                   // Intent addIntent=new Intent(getActivity(),PostActivity.class);
                    //startActivity(addIntent);
                    Intent intent = new Intent(getActivity(), PersonGroupListActivity.class);
                    startActivity(intent);

                }else if(position == 3) {
                 //   Intent profileIntent=new Intent(getActivity(),ProfileActivity.class);
                   // startActivity(profileIntent);
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "http://translate.google.com/");

                    try {
                        startActivity(Intent.createChooser(intent, "Select an action"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        // (handle error)
                    }

                }else if(position == 4){
                    Intent sendIntent=new Intent(getActivity(),UsersList.class);
                    startActivity(sendIntent);

                }else if(position == 0){
                    Intent sendIntent=new Intent(getActivity(),MainActivity.class);
                    startActivity(sendIntent);

                }

            }

        }));

        return layout;
    }
    public  static List<Information> getData()
    {
        List<Information> data=new ArrayList<>();
      //  int [] icons={R.drawable.users,R.drawable.add,R.drawable.shar,R.drawable.emai};
        int [] icons={R.drawable.ic_menu_home,R.drawable.ic_menu_account,R.drawable.ic_menu_camera,R.drawable.ic_menu_notifications,R.drawable.ic_menu_settings};
        String [] title={"Home","Profile","Add Post","Share","Send","Send"};
        for (int i=0;i<title.length && i< icons.length;i++)
        {
            Information current=new Information();
            current.iconId=icons[i];
            current.title=title[i];
            data.add(current);

        }
        return data;

    }
    public void setUp(int fragmentId ,DrawerLayout drawerlayout, Toolbar toolbar) {
        containerview=getActivity().findViewById(fragmentId);

        mDrawerlayout=drawerlayout;
        mDrawerToggle=new ActionBarDrawerToggle(getActivity(),drawerlayout,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {

                if(!mUserLearnedDrawer){
                    super.onDrawerOpened(drawerView);

                    mUserLearnedDrawer=true;
                    saveToPreference(getActivity(),KEY_USER_LEARNED_DRAWER,mUserLearnedDrawer+"");
                }
                getActivity().invalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                getActivity().invalidateOptionsMenu();

            }
        };
        if(!mUserLearnedDrawer && !mFromeSaveInstanceState)
        {
            mDrawerlayout.openDrawer(containerview);

        }

        //  mDrawerlayout.setDrawerListener(mDrawerToggle);
        mDrawerlayout.addDrawerListener(mDrawerToggle);

        mDrawerlayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();

            }
        });
    }
    public static void saveToPreference(Context context,String preferenceName,String preferenceValue){

        SharedPreferences sharedPreference=context.getSharedPreferences(PREF_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreference.edit();
        editor.putString(preferenceName,preferenceValue);
        editor.apply();
    }
    public static String readFromPreference(Context context, String preferenceName, String preferenceValue){

        SharedPreferences sharedPreference=context.getSharedPreferences(PREF_FILE_NAME,Context.MODE_PRIVATE);
        return sharedPreference.getString(preferenceName,preferenceValue);

    }

    @Override
    public void itemClick(View view, int position) {
        //    startActivity(new Intent(getActivity(),SubActivity.class));

    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private GestureDetector gestureDetector;
        private Clicklistener clicklistener;



        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final Clicklistener clicklistener){
            Log.d("VIVZ","Constractor Invoked");
            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    Log.d("VIVZ","noSingletap"+e);


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    Log.d("VIVZ","OnLongPress"+e);
            //        View child= recyclerView.findChildViewUnder(e.getX(),e.getY());
                    super.onLongPress(e);
                }
            });

        }




        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child= rv.findChildViewUnder(e.getX(),e.getY());
            if(child != null && clicklistener !=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));

            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            Log.d("VIVZ","onTouchEvent"+ e);


        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
    public static interface Clicklistener{
        public void onClick(View view,int position);

    }


}
