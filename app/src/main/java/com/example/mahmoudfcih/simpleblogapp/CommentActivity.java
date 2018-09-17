package com.example.mahmoudfcih.simpleblogapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CommentActivity extends AppCompatActivity {
    private EditText mcommentedittext;
    private ImageView mcommentbtn;
    private String mpost_key = null;
    private DatabaseReference mDatabaseComment;
    private FirebaseAuth mAuth;
    ProgressDialog mProgress;
    private DatabaseReference mDatabaseUser;
    private FirebaseUser mCurrentuser;
    private RelativeLayout activity_comment;

    ////////////////////////////////////////////////////////
    private RecyclerView mCommentlist;
    // private RecyclerView mBlog_list;
    private FirebaseAuth.AuthStateListener mAuthlistner;
    private DatabaseReference mDatabaseCurrentBlog;
    private Query mQueryCurrentBlog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        mAuthlistner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(CommentActivity.this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };
        mcommentedittext = (EditText) findViewById(R.id.comment);
        mcommentbtn = (ImageView) findViewById(R.id.addcomment);
        mAuth = FirebaseAuth.getInstance();
        mpost_key = getIntent().getExtras().getString("Blog_id");
        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Comments");
        mCurrentuser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentuser.getUid());

        //////////////////////////////////////////////////////////////
        //mBlog_list=(RecyclerView)findViewById(R.id.Single_Blog_list);
        // mBlog_list.setHasFixedSize(true);
        // mBlog_list.setLayoutManager(new LinearLayoutManager(this));
        mCommentlist = (RecyclerView) findViewById(R.id.Comment_list);

        mDatabaseCurrentBlog = FirebaseDatabase.getInstance().getReference().child("Comments");
        mQueryCurrentBlog = mDatabaseCurrentBlog.orderByChild("bid").equalTo(mpost_key);

        mCommentlist.setLayoutManager(new LinearLayoutManager(this));
        mCommentlist.setHasFixedSize(true);
        mProgress = new ProgressDialog(this);

        mcommentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startComment();
            }
        });
    }


    private void startComment() {
        mProgress.setMessage("Posting Comment ...");
        final String comment = mcommentedittext.getText().toString().trim();
        activity_comment = (RelativeLayout) findViewById(R.id.activity_comment);

        if (!TextUtils.isEmpty(comment)) {
            mProgress.show();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            final String formattedDate = df.format(c.getTime());
            final DatabaseReference newComment = mDatabaseComment.push();

            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newComment.child("comment").setValue(comment);
                    newComment.child("time").setValue(formattedDate);
                    newComment.child("bid").setValue(mpost_key);
                    newComment.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // startActivity(new Intent(CommentActivity.this,MainActivity.class));
                                //   mCommentlist.notify();

                            } else {
                                Snackbar.make(activity_comment, "Connection Failed", Snackbar.LENGTH_SHORT).show();

                            }
                        }
                    });
                    newComment.child("userimage").setValue(dataSnapshot.child("image").getValue());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mProgress.dismiss();


        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthlistner);
        FirebaseRecyclerAdapter<Comment, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(
                Comment.class,
                R.layout.comment_raw,
                CommentViewHolder.class,
                mQueryCurrentBlog

        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setImage(getApplicationContext(), model.getUserprofile());
                viewHolder.setTime(model.getTime());

            }
        };
        mCommentlist.setAdapter(firebaseRecyclerAdapter);


    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username) {
            TextView comment_username = (TextView) mView.findViewById(R.id.listview_username);
            comment_username.setText(username);

        }

        public void setComment(String comment) {
            TextView comment_comment = (TextView) mView.findViewById(R.id.listview_comment);
            comment_comment.setText(comment);
        }

        public void setImage(Context ctx, String userprofile) {
            ImageView comment_imageprofile = (ImageView) mView.findViewById(R.id.listview_image);
            Picasso.with(ctx).load(userprofile).into(comment_imageprofile);

        }

        public void setTime(String time) {
            TextView comment_time = (TextView) mView.findViewById(R.id.listview_time);
            comment_time.setText(time);
        }
    }
}
