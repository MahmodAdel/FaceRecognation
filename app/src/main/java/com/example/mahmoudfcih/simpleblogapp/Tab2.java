package com.example.mahmoudfcih.simpleblogapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class Tab2 extends Fragment {
    private Query mquery;
    public MainActivity activity;
    private DatabaseReference mdatabase;
    private FirebaseRecyclerAdapter<Identify, IdentifyViewHolder> mAdapter;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    public DatabaseReference muserfc;
    public FirebaseAuth.AuthStateListener mAuthlistner;
    String Userid;




    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.activity= (MainActivity) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v=inflater.inflate(R.layout.activity_tab2,container,false);
        RecyclerView mPost = (RecyclerView) v.findViewById(R.id.notify_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mPost.setHasFixedSize(true);
        mPost.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAuth =FirebaseAuth.getInstance();
        mAuthlistner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    if(getActivity() == null){
                        return;
                    }else {
                        Intent loginIntent = new Intent(getActivity(), Login.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        getActivity().finish();
                    }
                }
            }
        };
        if(mAuth.getCurrentUser() == null){
            getActivity().finish();
        }else {
            Userid=mAuth.getCurrentUser().getUid();
        }

        mdatabase = FirebaseDatabase.getInstance().getReference().child("identifiedResult");
        muserfc = FirebaseDatabase.getInstance().getReference().child("Users");
        mdatabase.keepSynced(true);
        mquery = mdatabase.orderByChild("userId").equalTo(Userid);
        mAdapter = new FirebaseRecyclerAdapter<Identify, IdentifyViewHolder>(
                        Identify.class,
                        R.layout.notify_row,
                        IdentifyViewHolder.class,
                        mquery
                ) {
                    @Override
                    protected void populateViewHolder(final IdentifyViewHolder viewHolder, final Identify model, int position) {
                        viewHolder.setChildName(model.getChildName());
                        viewHolder.setConfedince(model.getConfidence());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setImagef(getContext(), model.getImage());
                        viewHolder.setLocation(model.getLocation());
                        viewHolder.setUserfc(model.getUserfc());
                        muserfc.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (model.getUserfc() != null) {
                                    String userfcname = dataSnapshot.child(model.getUserfc()).child("name").getValue().toString();
                                    viewHolder.setUserfc(userfcname);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                };


        mPost.setAdapter(mAdapter);
        return v;
    }




    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthlistner);
    }


    public static class IdentifyViewHolder extends RecyclerView.ViewHolder{
        public TextView userfcc,childNamee,confedincee,locationn,datee;
        public ImageView imageff;
        public View mView;

        public Query query;

        public IdentifyViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            userfcc=(TextView)mView.findViewById(R.id.userfc);
            childNamee=(TextView)mView.findViewById(R.id.childName);
            confedincee=(TextView)mView.findViewById(R.id.confedence);
            locationn=(TextView)mView.findViewById(R.id.childlocation);
            imageff=(ImageView)mView.findViewById(R.id.image_uk);
            datee=(TextView)mView.findViewById(R.id.datef);


        }

        public void setChildName(String childName){
            childNamee.setText(childName);

        }
        public void setConfedince(String confedince){
            double confedin= Double.parseDouble(confedince) *100;
            String con= String.valueOf(confedin);
            confedincee.setText(con);

        }
        public void setDate(String date){
            datee.setText(date);
        }
        public void setImagef(final Context ctx, final String imagef){
            Picasso.with(ctx).load(imagef).networkPolicy(NetworkPolicy.OFFLINE).into(imageff, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(imagef).into(imageff);
                }
            });

        }
        public void setLocation(String location){
            locationn.setText(location);
        }
        public void setUserfc(String userfc){

            userfcc.setText(userfc);
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            getActivity().finish();
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("loading");
    }

}
