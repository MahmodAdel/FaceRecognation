package com.example.mahmoudfcih.simpleblogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;



import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {
    private ImageButton mSetupImageBtn;
    private EditText mNamefield;
    private Button mSubmitBtn;
    private Uri mImageuri =null;
    private static final int GALLARY_REQUEST=1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser;
    private StorageReference mStoreImage;
    private ProgressDialog mProgress;
    private FirebaseUser mCurrentUser;
    private EditText mcountry;
    private EditText mcity;
    private EditText mbirthday;
    private Spinner mgender;
    private static final String genderArray[] = {"Male", "Female"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mgender=(Spinner)findViewById(R.id.gender);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, genderArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mgender.setAdapter(spinnerArrayAdapter);
        mgender.setPrompt("Select Gender");

        mAuth=FirebaseAuth.getInstance();
        mStoreImage=FirebaseStorage.getInstance().getReference().child("Profile_image");
        mDatabaseUser= FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress=new ProgressDialog(this);

        mSetupImageBtn=(ImageButton)findViewById(R.id.user_image);
        mNamefield=(EditText)findViewById(R.id.user_name);
        mSubmitBtn=(Button)findViewById(R.id.savedata);
        mcity=(EditText)findViewById(R.id.city);
        mcountry=(EditText)findViewById(R.id.country);
        mbirthday=(EditText) findViewById(R.id.birthday);

        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent,GALLARY_REQUEST);
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();

            }
        });


    }

    private void startSetupAccount() {
        final String name=mNamefield.getText().toString().trim();
        final String user_id=mAuth.getCurrentUser().getUid();
        final String city=mcity.getText().toString().trim();
        final String country=mcountry.getText().toString().trim();
        final String birthday=mbirthday.getText().toString().trim();
        final String gender=mgender.getSelectedItem().toString();
        if(!TextUtils.isEmpty(name) && mImageuri !=null)
        {
            mProgress.setMessage("Finishing SetUp ...");
            mProgress.show();
            StorageReference filepath=mStoreImage.child(mImageuri.getLastPathSegment());
            filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FireBaseHelper.Users USER = new FireBaseHelper.Users();
                    USER.Name = name;
                    USER.City=city;
                    USER.Country=country;
                    USER.Birthday=birthday;
                    USER.Gender=gender;
                    String downloadUri= String.valueOf(taskSnapshot.getDownloadUrl());
                    USER.Image = downloadUri;
                    USER.AddUser(user_id);
//                    mDatabaseUser.child(user_id).child("name").setValue(name);
//                    mDatabaseUser.child(user_id).child("image").setValue(downloadUri);
                    mProgress.dismiss();
                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }


            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLARY_REQUEST && resultCode == RESULT_OK)
        {
            mImageuri= data.getData();
            //mSetupImageBtn.setImageURI(mImageuri);

            CropImage.activity(mImageuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageuri = result.getUri();
                mSetupImageBtn.setImageURI(mImageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
       //         Exception error = result.getError();
            }
        }

    }
}
