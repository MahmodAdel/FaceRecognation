package com.example.mahmoudfcih.simpleblogapp;

/**
 * Created by mahmoud on 3/30/2017.
 */
import android.app.Activity;

import android.content.Intent;

import android.graphics.PixelFormat;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;

import android.view.animation.Animation;

import android.view.animation.AnimationUtils;



import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;


public class Splashscreen extends Activity {
    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthlistner;


    public void onAttachedToWindow() {

        super.onAttachedToWindow();

        Window window = getWindow();

        window.setFormat(PixelFormat.RGBA_8888);

    }

    /** Called when the activity is first created. */

    Thread splashTread;

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        StartAnimations();
        mAuth = FirebaseAuth.getInstance();
        mAuthlistner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(Splashscreen.this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();

                }
            }
        };


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    private void StartAnimations() {

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);

        anim.reset();

        RelativeLayout l=(RelativeLayout) findViewById(R.id.lin_lay);

        l.clearAnimation();

        l.startAnimation(anim);



        anim = AnimationUtils.loadAnimation(this, R.anim.translate);

        anim.reset();

        LinearLayout iv = (LinearLayout) findViewById(R.id.splash);

        iv.clearAnimation();

        iv.startAnimation(anim);



        splashTread = new Thread() {

            @Override

            public void run() {

                try {

                    int waited = 0;

                    // Splash screen pause time

                    while (waited < 3500) {

                        sleep(100);

                        waited += 100;

                    }

                    Intent intent = new Intent(Splashscreen.this,

                            MainActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    startActivity(intent);

                    Splashscreen.this.finish();

                } catch (InterruptedException e) {

                    // do nothing

                } finally {

                    Splashscreen.this.finish();

                }



            }

        };

        splashTread.start();



    }



}