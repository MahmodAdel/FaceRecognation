package com.example.mahmoudfcih.simpleblogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Tab1 extends Fragment {
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthlistner;
    final boolean[] mProcessLike = {false};
    final DatabaseReference[] mDatabase = {FirebaseDatabase.getInstance().getReference().child("Blog")};
    DatabaseReference mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
    final DatabaseReference mDatabaselike=FirebaseDatabase.getInstance().getReference().child("Likes");
    final DatabaseReference mUserprofile=FirebaseDatabase.getInstance().getReference().child("Users");
    final DatabaseReference mUserprofileblog=FirebaseDatabase.getInstance().getReference().child("Blog");
    FirebaseRecyclerAdapter<Blog,MainActivity.BlogViewHolder> firebaseRecyclerAdapter = null;

/*
    public static Tab1 getInstance(int position){

        Tab1 myFragment=new Tab1();
        Bundle args=new Bundle();
        args.putInt("position",position);
        myFragment.setArguments(args);

        return myFragment;

    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.activity_tab1,container,false);


        mAuth=FirebaseAuth.getInstance();
        mAuthlistner=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent= new Intent(getActivity(),Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };
        mUserprofile.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabase[0].keepSynced(true);
        mDatabaselike.keepSynced(true);
        mUserprofileblog.keepSynced(true);
        RecyclerView mBloglist=(RecyclerView)layout.findViewById(R.id.Blog_list);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mBloglist.setHasFixedSize(true);
        mBloglist.setLayoutManager(new LinearLayoutManager(getActivity()));



        firebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Blog, MainActivity.BlogViewHolder>(
                Blog.class,
                R.layout.blog_raw,
                MainActivity.BlogViewHolder.class,
                mDatabase[0]

        ) {

            @Override
            protected void populateViewHolder(final MainActivity.BlogViewHolder viewHolder, Blog model, int position) {
                final String post_key= getRef(position).getKey();
              //  viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());
                if(model.getDate()!= null) {
                    viewHolder.setDate(model.getDate());
                }
                viewHolder.setLikebtn(post_key);
                viewHolder.numberofComments(post_key);
                viewHolder.setLocation(model.getAddress());
               // if(mUserprofileblog.child(post_key).)
                //    mQueryCurrentUser=mUserprofileblog.orderByChild("uid")
                mUserprofileblog.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userid= (String) dataSnapshot.child("uid").getValue();
                        if(userid != null) {
                            mUserprofile.child(userid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String user_profile = (String) dataSnapshot.child("image").getValue();
                                    //   setUserprofile(user_profile);
                                    viewHolder.setUserPorfile(getContext(), user_profile);
                                    //  Picasso.with(getContext()).load(user_profile).into(imageprofile);

                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.mcommenttxtview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent CommentIntent=new Intent(getActivity(),CommentActivity.class);
                        CommentIntent.putExtra("Blog_id",post_key);
                        startActivity(CommentIntent);
                    }
                });
                viewHolder.ViewAllComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent CommentIntent=new Intent(getActivity(),CommentActivity.class);
                        CommentIntent.putExtra("Blog_id",post_key);
                        startActivity(CommentIntent);
                    }
                });
                String UserID=model.getUid();
                if(mAuth.getCurrentUser() != null) {


                    if (UserID.equals(mAuth.getCurrentUser().getUid().toString())) {
                        viewHolder.mImageButton.setVisibility(View.VISIBLE);
                        viewHolder.mImageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PopupMenu popupMenu = new PopupMenu(getContext(), viewHolder.mImageButton);
                                MenuInflater inflater1 = popupMenu.getMenuInflater();
                                inflater1.inflate(R.menu.pop_menu, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        int id = item.getItemId();
                                        if (id == R.id.item_delete) {

                                            mDatabase[0].child(post_key).removeValue();
                                            firebaseRecyclerAdapter.notifyDataSetChanged();
                                        }
                                        return true;
                                    }
                                });
                                popupMenu.show();
                            }
                        });
                    } else {
                        viewHolder.mImageButton.setVisibility(View.INVISIBLE);

                    }
                }



                viewHolder.mlikebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mProcessLike[0] =true;
                        mDatabaselike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mProcessLike[0]) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaselike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike[0] = false;

                                    } else {
                                        mDatabaselike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RendomValue");
                                        mProcessLike[0] = false;

                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }


                });

            }

        };
       // mBloglist.setLayoutManager(new LinearLayoutManager(getContext()));
        mBloglist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
        return layout;
    }


}
