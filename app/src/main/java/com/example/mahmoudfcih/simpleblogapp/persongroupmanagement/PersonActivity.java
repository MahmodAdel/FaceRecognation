package com.example.mahmoudfcih.simpleblogapp.persongroupmanagement;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.mahmoudfcih.simpleblogapp.FireBaseHelper;
import com.example.mahmoudfcih.simpleblogapp.MainActivity;
import com.example.mahmoudfcih.simpleblogapp.R;
import com.example.mahmoudfcih.simpleblogapp.helper.LogHelper;
import com.example.mahmoudfcih.simpleblogapp.helper.StorageHelper;
import com.example.mahmoudfcih.simpleblogapp.ui.SelectImageActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class PersonActivity extends AppCompatActivity {
    private DatabaseReference mDataUser;
    private FirebaseUser mCurrentuser;
    private FirebaseAuth mAuth;

    public String imgeUri;
    public String title_val;
    public String desc_val;
    public String location_Val;
    private TextView addlocation;
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS=2;


    // Background task of adding a person to person group.
    class AddPersonTask extends AsyncTask<String, String, String> {
        // Indicate the next step is to add face in this person, or finish editing this person.
        boolean mAddFace;

        AddPersonTask (boolean addFace) {
            mAddFace = addFace;
        }

        @Override
        protected String doInBackground(String... params) {
            // Get an instance of face service client.
            //    FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            FaceServiceClient faceServiceClient=new FaceServiceRestClient("c16ccb9c529d482e90b9acead3aae75d");
            try{
                publishProgress("Syncing with server to add person...");
                addLog("Request: Creating Person in person group" + params[0]);

                // Start the request to creating person.
                CreatePersonResult createPersonResult = faceServiceClient.createPerson(
                        params[0],
                        getString(R.string.user_provided_person_name),
                        getString(R.string.user_provided_description_data));

                return createPersonResult.personId.toString();
            } catch (Exception e) {
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            setUiDuringBackgroundTask(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result != null) {
                addLog("Response: Success. Person " + result + " created.");
                personId = result;
                setInfo("Successfully Synchronized!");

                if (mAddFace) {
                    addFace();
                } else {
                    doneAndSave(true);
                }
            }
        }
    }


    class TrainPersonGroupTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            //     addLog("Request: Training group " + params[0]);

            // Get an instance of face service client.
            FaceServiceClient faceServiceClient = new FaceServiceRestClient("c16ccb9c529d482e90b9acead3aae75d");
            try{
                publishProgress("Training person group...");

                faceServiceClient.trainPersonGroup(params[0]);
                return params[0];
            } catch (Exception e) {
                publishProgress(e.getMessage());
                //  addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            setUiDuringBackgroundTask(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
//            progressDialog.dismiss();

            if (result != null) {
                //   addLog("Response: Success. Group " + result + " training completed");

                finish();
            }
        }
    }
    class DeleteFaceTask extends AsyncTask<String, String, String> {
        String mPersonGroupId;
        UUID mPersonId;

        DeleteFaceTask(String personGroupId, String personId) {
            mPersonGroupId = personGroupId;
            mPersonId = UUID.fromString(personId);
        }

        @Override
        protected String doInBackground(String... params) {
            // Get an instance of face service client.
            //  FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            FaceServiceClient faceServiceClient=new FaceServiceRestClient("c16ccb9c529d482e90b9acead3aae75d");
            try{
                publishProgress("Deleting selected faces...");
                addLog("Request: Deleting face " + params[0]);

                UUID faceId = UUID.fromString(params[0]);
                faceServiceClient.deletePersonFace(personGroupId, mPersonId, faceId);
                return params[0];
            } catch (Exception e) {
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            setUiDuringBackgroundTask(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result != null) {
                setInfo("Face " + result + " successfully deleted");
                addLog("Response: Success. Deleting face " + result + " succeed");
            }
        }
    }
    private void setUiBeforeBackgroundTask() {
//        progressDialog.show();
    }

    // Show the status of background detection task on screen.
    private void setUiDuringBackgroundTask(String progress) {
        progressDialog.setMessage(progress);
        setInfo(progress);
    }

    boolean addNewPerson;
    String personId;
    String personGroupId;
    String oldPersonName;
    String mImageUriStr;
    Uri uriImagePicked;
    private StorageReference mStorage;
    Bitmap mBitmap;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabase;
    private static final int REQUEST_SELECT_IMAGE = 0;
    int PLACE_PICKER_REQUEST=1;

    FaceGridViewAdapter faceGridViewAdapter;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            addNewPerson = bundle.getBoolean("AddNewPerson");
            personGroupId = bundle.getString("PersonGroupId");
            oldPersonName = bundle.getString("PersonName");
            mImageUriStr = bundle.getString("ImageUriStr");

            if (!addNewPerson) {
                personId = bundle.getString("PersonId");
            }
        }
        mStorage= FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        addlocation=(TextView)findViewById(R.id.location_post);
        addlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder=new PlacePicker.IntentBuilder();
                Intent intent;
                try {
                    intent=builder.build(PersonActivity.this);
                    startActivityForResult(intent,PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

             // Should we show an explanation?
             if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)) {

                 // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                 } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                 }
        }

        initializeGridView();


        EditText editTextPersonName = (EditText)findViewById(R.id.edit_person_name);

        editTextPersonName.setText(oldPersonName);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));

/*
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getPersonGroupName(personGroupId, new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                FireBaseHelper.Algorithm alg= (FireBaseHelper.Algorithm) Data;
                StorageHelper.setPersonGroupName(alg.personGroupId,alg.personGroupName,PersonActivity.this);
            }
        });
        ///////////////////////////////////
        algorithm.getAllPersonIds(personGroupId, new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                List<FireBaseHelper.Algorithm> persons= (List<FireBaseHelper.Algorithm>) Data;
                for(int i=0 ; i< persons.size(); i++){
                    StorageHelper.setPersonName(persons.get(i).personId, persons.get(i).personName, personGroupId, PersonActivity.this);
                }

            }
        });
        */

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                     } else {

                     // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
                }

             // other 'case' lines to check for other
             // permissions this app might request
            }

    }

    private void initializeGridView() {
        GridView gridView = (GridView) findViewById(R.id.gridView_faces);

        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(
                    ActionMode mode, int position, long id, boolean checked) {
                faceGridViewAdapter.faceChecked.set(position, checked);

                GridView gridView = (GridView) findViewById(R.id.gridView_faces);
                gridView.setAdapter(faceGridViewAdapter);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu, menu);

                faceGridViewAdapter.longPressed = true;

                GridView gridView = (GridView) findViewById(R.id.gridView_faces);
                gridView.setAdapter(faceGridViewAdapter);

                Button addNewItem = (Button) findViewById(R.id.add_face);
                addNewItem.setEnabled(false);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete_items:
                        deleteSelectedItems();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                faceGridViewAdapter.longPressed = false;

                for (int i = 0; i < faceGridViewAdapter.faceChecked.size(); ++i) {
                    faceGridViewAdapter.faceChecked.set(i, false);
                }

                GridView gridView = (GridView) findViewById(R.id.gridView_faces);
                gridView.setAdapter(faceGridViewAdapter);

                Button addNewItem = (Button) findViewById(R.id.add_face);
                addNewItem.setEnabled(true);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        faceGridViewAdapter = new FaceGridViewAdapter();
        GridView gridView = (GridView) findViewById(R.id.gridView_faces);
        gridView.setAdapter(faceGridViewAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("AddNewPerson", addNewPerson);
        outState.putString("PersonId", personId);
        outState.putString("PersonGroupId", personGroupId);
        outState.putString("OldPersonName", oldPersonName);
        if(uriImagePicked !=null) {
            outState.putString("Imgstr", uriImagePicked.toString());
        }
    }



    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        addNewPerson = savedInstanceState.getBoolean("AddNewPerson");
        personId = savedInstanceState.getString("PersonId");
        personGroupId = savedInstanceState.getString("PersonGroupId");
        oldPersonName = savedInstanceState.getString("OldPersonName");
        imgeUri=savedInstanceState.getString("Imgstr");
    }
    public void doneAndSave(View view) {
        if (personId == null) {
            new AddPersonTask(false).execute(personGroupId);
        } else {
            doneAndSave(true);
        }
    }

    public void addFace(View view) {
        if (personId == null) {
            new AddPersonTask(true).execute(personGroupId);
        } else {
            addFace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupHistory();
        personsHistory();
        facesHistory();
    }

    public void groupHistory(){
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getPersonGroupName("00bfdf0a-c506-4692-8e43-42f3b7b04a9b", new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                FireBaseHelper.Algorithm alg= (FireBaseHelper.Algorithm) Data;
                StorageHelper.setPersonGroupName(alg.personGroupId,alg.personGroupName,PersonActivity.this);
            }
        });

    }
    public void personsHistory(){
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getAllPersonIds("00bfdf0a-c506-4692-8e43-42f3b7b04a9b", new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                List<FireBaseHelper.Algorithm> persons= (List<FireBaseHelper.Algorithm>) Data;
                for(int i=0 ; i< persons.size(); i++){
                    StorageHelper.setPersonName(persons.get(i).personId, persons.get(i).personName, "00bfdf0a-c506-4692-8e43-42f3b7b04a9b", PersonActivity.this);
                }

            }
        });

    }
    public void facesHistory(){
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getAllPersonIds("00bfdf0a-c506-4692-8e43-42f3b7b04a9b", new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                final List<FireBaseHelper.Algorithm> persons= (List<FireBaseHelper.Algorithm>) Data;
                for(int i=0 ; i< persons.size(); i++){
                    final int finalI = i;
                    algorithm.getAllFaceIds(persons.get(i).personId, new FireBaseHelper.OnGetDataListener() {
                        @Override
                        public void onSuccess(Object Data) {
                            final List<FireBaseHelper.Algorithm> Faceid= (List<FireBaseHelper.Algorithm>) Data;
                            for (int j=0;j<Faceid.size();j++){
                                final int finalJ = j;
                                algorithm.getFaceUri(Faceid.get(j).personFaceId, new FireBaseHelper.OnGetDataListener() {
                                    @Override
                                    public void onSuccess(Object Data) {
                                        // List<FireBaseHelper.Algorithm> faceUri= (List<FireBaseHelper.Algorithm>) Data;
                                        String FaceUri= (String) Data;
                                        //  URL url=new URL(FaceUri);
                                        final ImageView secretimg=(ImageView)findViewById(R.id.sercretimg);
                                        Picasso.with(PersonActivity.this).load(FaceUri).into(secretimg, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                Bitmap image=((BitmapDrawable)secretimg.getDrawable()).getBitmap();
                       //                         Uri uri=Uri.parse(String.valueOf(image));
                                                FileOutputStream fileOutputStream = null;
                                                try {
                                                    File file = new File(getApplicationContext().getFilesDir(), Faceid.get(finalJ).personFaceId);
                                                    fileOutputStream = new FileOutputStream(file);
                                                    image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                                    fileOutputStream.flush();

                                                    Uri uri1 = Uri.fromFile(file);
                                                    StorageHelper.setFaceUri(
                                                            Faceid.get(finalJ).personFaceId, uri1.toString(), persons.get(finalI).personId, PersonActivity.this);
                                                    //  FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
                                                    // algorithm.setFaceUri(faceId,Face,mPersonId);

                                                } catch (IOException e) {
                                                    setInfo(e.getMessage());
                                                } finally {
                                                    if (fileOutputStream != null) {
                                                        try {
                                                            fileOutputStream.close();
                                                        } catch (IOException e) {
                                                            setInfo(e.getMessage());
                                                        }
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        });




                                    }
                                });

                            }
                        }
                    });
                }
            }
        });

    }



    private void doneAndSave(boolean trainPersonGroup) {
        TextView textWarning = (TextView)findViewById(R.id.info);
        EditText editTextPersonName = (EditText)findViewById(R.id.edit_person_name);
        String newPersonName = editTextPersonName.getText().toString();
        if (newPersonName.equals("")) {
            textWarning.setText(R.string.person_name_empty_warning_message);
            return;
        }
        // get Group from Firebase

        FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.setPersonName(personId,newPersonName,personGroupId);
       // StorageHelper.setPersonName(personId, newPersonName, personGroupId, PersonActivity.this);
       // StorageHelper.setPersonGroupName(personGroupId,"persons", PersonActivity.this);
        mAuth=FirebaseAuth.getInstance();
        mCurrentuser=mAuth.getCurrentUser();
        mDataUser= FirebaseDatabase.getInstance().getReference().child("personsIds").push();
        mDataUser.child("personId").setValue(personId);
        mDataUser.child("userId").setValue(mCurrentuser.getUid());

        mDataUser.child("personGroupId").setValue(personGroupId);
        mDataUser.child("personName").setValue(newPersonName);
        startPosting();
        startActivity(new Intent(PersonActivity.this,MainActivity.class));
        if(trainPersonGroup) {
            new PersonActivity.TrainPersonGroupTask().execute(personGroupId);
        }else {

            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PersonActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startPosting() {
        mAuth=FirebaseAuth.getInstance();
        mCurrentuser=mAuth.getCurrentUser();
        EditText mPostDes=(EditText)findViewById(R.id.description_post);
        EditText mPostTitle=(EditText)findViewById(R.id.title_post);
        mDatabaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentuser.getUid().toString());
        // mProgress.setMessage("Posting to Blog ...");
        title_val=mPostTitle.getText().toString().trim();
        desc_val=mPostDes.getText().toString().trim();
        location_Val=addlocation.getText().toString().trim();
      //  Calendar c = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = new Date();
        final String formattedDate = dateFormat.format(date);

        Uri real = null;
        if(imgeUri==null){
             real=uriImagePicked;
        }else if(uriImagePicked == null){
             real= Uri.parse(imgeUri);
        }
        
        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) &&  real!= null && !TextUtils.isEmpty(location_Val)){

            StorageReference filepath= mStorage.child("Blog_Images").child(real.getLastPathSegment());
            filepath.putFile(real).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  //  final Uri downloadUri= Uri.parse(taskSnapshot.getDownloadUrl().toString());
                    final String DownloadUri= String.valueOf(taskSnapshot.getDownloadUrl());
                    final DatabaseReference newPost=mDatabase.push();
                    mDatabaseUser.addValueEventListener(new ValueEventListener() { // to retrive user name
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(title_val);
                            newPost.child("desc").setValue(desc_val);
                            newPost.child("image").setValue(DownloadUri);
                            newPost.child("uid").setValue(mCurrentuser.getUid().toString());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                 /*   if(task.isSuccessful()){
                                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                                    }*/
                                }
                            }); //datasnapshot retrive all data
                            newPost.child("date").setValue(formattedDate);
                            newPost.child("address").setValue(location_Val);



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });





        }
    }

    private void addFace() {
        setInfo("");
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    uriImagePicked = data.getData();

                    Intent intent = new Intent(this, AddFaceToPersonActivity.class);
                    intent.putExtra("PersonId", personId);
                    intent.putExtra("PersonGroupId", personGroupId);
                    intent.putExtra("ImageUriStr", uriImagePicked.toString());
                    intent.putExtra("post_desc",desc_val);
                    intent.putExtra("post_title",title_val);
                    intent.putExtra("post_location",location_Val);
                    startActivity(intent);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK)
                {
                    Place place=PlacePicker.getPlace(getApplicationContext(),data);
                    String address=String.format(""+place.getAddress());
                    addlocation.setText(address);
                }

            default:
                break;
        }
    }

    private void deleteSelectedItems() {
        List<String> newFaceIdList = new ArrayList<>();
        List<Boolean> newFaceChecked = new ArrayList<>();
        List<String> faceIdsToDelete = new ArrayList<>();
        for (int i = 0; i < faceGridViewAdapter.faceChecked.size(); ++i) {
            boolean checked = faceGridViewAdapter.faceChecked.get(i);
            if (checked) {
                String faceId = faceGridViewAdapter.faceIdList.get(i);
                faceIdsToDelete.add(faceId);
                new DeleteFaceTask(personGroupId, personId).execute(faceId);
            } else {
                newFaceIdList.add(faceGridViewAdapter.faceIdList.get(i));
                newFaceChecked.add(false);
            }
        }

        StorageHelper.deleteFaces(faceIdsToDelete, personId, this);

        faceGridViewAdapter.faceIdList = newFaceIdList;
        faceGridViewAdapter.faceChecked = newFaceChecked;
        faceGridViewAdapter.notifyDataSetChanged();
    }
    // Add a log item.
    private void addLog(String log) {
        LogHelper.addIdentificationLog(log);
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    private class FaceGridViewAdapter extends BaseAdapter {
        List<String> faceIdList;
        List<Boolean> faceChecked;
        boolean longPressed;

        FaceGridViewAdapter() {
            longPressed = false;
            faceIdList = new ArrayList<>();
            faceChecked = new ArrayList<>();

            Set<String> faceIdSet = StorageHelper.getAllFaceIds(personId, PersonActivity.this);
            for (String faceId: faceIdSet) {
                faceIdList.add(faceId);
                faceChecked.add(false);
            }
        }

        @Override
        public int getCount() {
            return faceIdList.size();
        }

        @Override
        public Object getItem(int position) {
            return faceIdList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // set the item view
            if (convertView == null) {
                LayoutInflater layoutInflater
                        = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(
                        R.layout.item_face_with_checkbox, parent, false);
            }
            convertView.setId(position);

            Uri uri = Uri.parse(StorageHelper.getFaceUri(
                    faceIdList.get(position), PersonActivity.this));
            ((ImageView)convertView.findViewById(R.id.image_face)).setImageURI(uri);

            // set the checked status of the item
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_face);
            if (longPressed) {
                checkBox.setVisibility(View.VISIBLE);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        faceChecked.set(position, isChecked);
                    }
                });
                checkBox.setChecked(faceChecked.get(position));
            } else {
                checkBox.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);


    }

}
