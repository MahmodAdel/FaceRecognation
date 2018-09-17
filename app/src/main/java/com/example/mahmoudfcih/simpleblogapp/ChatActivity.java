package com.example.mahmoudfcih.simpleblogapp;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.github.library.bubbleview.BubbleTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {
    private static int SIGN_IN_REQUEST_CODE=1;
    private FirebaseListAdapter<ChatMessage> adapter;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input=(EditText)findViewById(R.id.input);
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(new ChatMessage(input.getText().toString()
                        , FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                input.setText("");
                //clear message when send
           //     BubbleTextView bubbleTextView=(BubbleTextView)findViewById(R.id.message_text);
             //   bubbleTextView.setText("");

            }
        });
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent loginIntent = new Intent(ChatActivity.this, Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }else {
            displayChatMessage();

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE)
        {
            if(resultCode ==RESULT_OK){
                displayChatMessage();
            }else {
                finish();


            }
        }


    }

    private void displayChatMessage() {
        ListView listofMessage=(ListView)findViewById(R.id.list_of_message);
        adapter=new FirebaseListAdapter<ChatMessage>(
                this,
                ChatMessage.class,
                R.layout.list_item,
                FirebaseDatabase.getInstance().getReference().child("Chats")
        ) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText,messageUser,messageTime;
                messageText=(BubbleTextView)v.findViewById(R.id.message_text);
                messageUser=(TextView)v.findViewById(R.id.message_user);
                messageTime=(TextView)v.findViewById(R.id.message_time);
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",model.getMessageTime()));

            }
        };
        listofMessage.setAdapter(adapter);
    }
}
