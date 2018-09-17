package com.example.mahmoudfcih.simpleblogapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;

import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.TextView;


import com.example.mahmoudfcih.simpleblogapp.Constant.consts;
import com.example.mahmoudfcih.simpleblogapp.adminui.IdentificationActivity;
import com.example.mahmoudfcih.simpleblogapp.helper.ImageHelper;
import com.example.mahmoudfcih.simpleblogapp.helper.StorageHelper;

import com.example.mahmoudfcih.simpleblogapp.ui.SelectImageActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.TrainingStatus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class Tab3 extends Fragment{
    private FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentuser;
    private static final int REQUEST_SELECT_IMAGE = 0;
    private Bitmap mBitmap;
    boolean detected;
    private ImageView mImage;
    private TextView mlocation;
    private String location_val;
    int PLACE_PICKER_REQUEST = 1;
    ContentResolver musicResolver;
    //   String mPersonGroupId="7ae30f27-918c-4937-ac58-fc629c3e3e2e";
   // String mPersonGroupId = "00bfdf0a-c506-4692-8e43-42f3b7b04a9b";
    String mPersonGroupId=consts.Person_group_id;
    FaceListAdapter mFaceListAdapter;
    ProgressDialog progressDialog;
    private Button identifybtn;
    Uri imageUri;
    String Adress;
    String NewsData="";
    TextView txtv;
    private String url=consts.url_add;
    public LocationManager locationManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_tab3, container, false);
        mAuth = FirebaseAuth.getInstance();
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        mImage = (ImageView) v.findViewById(R.id.imageView2);
        txtv = (TextView) v.findViewById(R.id.resultnotify);
        musicResolver = getActivity().getContentResolver();
        identifybtn = (Button) v.findViewById(R.id.button2);
        mlocation = (TextView) v.findViewById(R.id.location_person);
        mStorage = FirebaseStorage.getInstance().getReference().child("identified_image");
        mDatabase = FirebaseDatabase.getInstance().getReference();



        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
        if (!mAuth.getCurrentUser().getUid().equals("pZTxCRpGSqZYtFgnXpCOxSrXNc13")) {
            fab.setVisibility(View.INVISIBLE);
        }

        identifybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SelectImageActivity.class);
                startActivityForResult(intent, REQUEST_SELECT_IMAGE);

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setupIntent = new Intent(getActivity(), IdentificationActivity.class);
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(setupIntent);
            }
        });
        mlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            /*    PlacePicker.IntentBuilder builder=new PlacePicker.IntentBuilder();
                Intent intent;
                try {
                    intent=builder.build(getActivity());
                    startActivityForResult(intent,PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }*/
            }
        });

        return v;
    }

 /*   public void selectImage(View view) {
        Intent intent = new Intent(getActivity(), SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    detected = false;

                    // If image is selected successfully, set the image URI and bitmap.
                    imageUri = data.getData();
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            imageUri, musicResolver);
                    if (mBitmap != null) {
                        // Show the image on screen.
                        mImage.setImageBitmap(mBitmap);
                    }

                    // Clear the identification result.
                  /*  IdentificationActivity.FaceListAdapter faceListAdapter = new IdentificationActivity.FaceListAdapter(null);
                    ListView listView = (ListView) findViewById(R.id.list_identified_faces);
                    listView.setAdapter(faceListAdapter);

                    // Clear the information panel.
                    setInfo("");*/

                    // Start detecting in image.
                    detect(mBitmap);

                }
                break;
            case 1:
                if(resultCode == RESULT_OK)
                {
                    Place place=PlacePicker.getPlace(getActivity(),data);
                    Adress=String.format(""+place.getAddress());
                    mlocation.setText(Adress);
                }
            default:
                break;
        }

    }



    // Background task of face identification.
    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        private boolean mSucceed = true;
        String mPersonGroupId;
        IdentificationTask(String personGroupId) {
            this.mPersonGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            String logString = "Request: Identifying faces ";
            for (UUID faceId: params) {
                logString += faceId.toString() + ", ";
            }
     //       logString += " in group " + mPersonGroupId;
          //  addLog(logString);

            // Get an instance of face service client to detect faces in image.
            //    FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            FaceServiceClient faceServiceClient=new FaceServiceRestClient("c16ccb9c529d482e90b9acead3aae75d");
            try{
                publishProgress("Getting person group status...");

                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(
                        this.mPersonGroupId);     /* personGroupId */

                if (trainingStatus.status != TrainingStatus.Status.Succeeded) {
                    publishProgress("Person group training status is " + trainingStatus.status);
                    mSucceed = false;
                    return null;
                }

                publishProgress("Identifying...");

                // Start identification.
                return faceServiceClient.identity(
                        this.mPersonGroupId,   /* personGroupId */
                        params,                  /* faceIds */
                        1);  /* maxNumOfCandidatesReturned */

            }  catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
            //    addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.a
            setUiDuringBackgroundTask(values[0]);
        }

        @Override
        protected void onPostExecute(IdentifyResult[] result) {
            // Show the result on screen when detection is done.
            setUiAfterIdentification(result, mSucceed);
        }
    }


    // Show the result on screen when detection is done.
    private void setUiAfterIdentification(IdentifyResult[] result, boolean succeed) {
        progressDialog.dismiss();

     //   setAllButtonsEnabledStatus(true);
       // setIdentifyButtonEnabledStatus(false);


        if (succeed) {
            // Set the information about the detection result.
     //       setInfo("Identification is done");

            if (result != null) {
                mFaceListAdapter.setIdentificationResult(result);

                String logString = "Response: Success. ";
                for (IdentifyResult identifyResult: result) {
                    if(identifyResult.candidates.size() != 0) {
                        SavaDataResult(identifyResult.candidates.get(0).personId.toString(), identifyResult.candidates.get(0).confidence);
                    }
                    logString += "Face " + identifyResult.faceId.toString() + " is identified as "
                            + (identifyResult.candidates.size() > 0
                            ? identifyResult.candidates.get(0).personId.toString()
                            : "Unknown Person")
                            + ". ";
                }
//                addLog(logString);

                // Show the detailed list of detected faces.
                ListView listView = (ListView) getView().findViewById(R.id.list_identified_faces);
                listView.setAdapter(mFaceListAdapter);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ImgLocation",Adress);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(Adress !=null) {
            Adress = savedInstanceState.getString("ImgLocation");
        }
    }

    private void SavaDataResult(final String personId, double confidence) {
        final String confiden= String.valueOf(confidence);
        final Map<String,String> Values=new HashMap<>();
        final String[] userid = new String[1];
        final String[] childName = new String[1];
        if(personId !=null && confiden != null && imageUri !=null){
            Query mUserperson=mDatabase.child("personsIds").orderByChild("personId").equalTo(personId);
            mUserperson.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator= dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot=(DataSnapshot)iterator.next();
                        userid[0] = (String) postSnapshot.child("userId").getValue();
                        childName[0] = (String) postSnapshot.child("personName").getValue();

                    }
                    StorageReference filepath=mStorage.child(imageUri.getLastPathSegment());
                    filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String DownloadUri= String.valueOf(taskSnapshot.getDownloadUrl());
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                            location_val=mlocation.getText().toString().trim();
                            location_val=Adress;
                            final String formattedDate = df.format(c.getTime());
                            Values.put("personId",personId);
                            Values.put("userId", userid[0]);
                            Values.put("image",DownloadUri);
                            Values.put("childName", childName[0]);
                            Values.put("date",formattedDate);
                            Values.put("confidence",confiden);
                            Values.put("userfc",mAuth.getCurrentUser().getUid());
                            Values.put("location",location_val);
                            DatabaseReference myref=mDatabase.child("identifiedResult").push();
                            myref.setValue(Values);
                        }
                    });
                    SaveNotify(userid[0],childName[0]);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

         /*   StorageReference filepath=mStorage.child(imageUri.getLastPathSegment());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDatabase.child("identifyResult").
                }
            });*/
        }




    }

    private void SaveNotify(String UserID, String PersonName) {
       // String myurl = "http://expressapp920170628103240.azurewebsites.net/add?PersonName="+UserID+"&comment=" +PersonName;
        String myurl = url+UserID+"&comment=" +PersonName;
        new  MyAsyncTaskgetNews().execute(myurl);

    }
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            NewsData="";

        }
        @Override
        protected String  doInBackground(String... params) {

            publishProgress("open connection" );
            try
            {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                publishProgress("start  read buffer" );
                NewsData=Stream2String(in);
                in.close();



            } catch (Exception e) {
                publishProgress("cannot connect to server" );
            }

            return null;

        }
        protected void onProgressUpdate(String... progress) {

            txtv.setText(progress[0]);

        }
        protected void onPostExecute(String  result2){

            txtv.setText(NewsData);
        }
    }
    public String Stream2String(InputStream inputStream) {
        BufferedReader bureader=new BufferedReader( new InputStreamReader(inputStream));
        String line ;
        String Text="";
        try{
            while((line=bureader.readLine())!=null) {
                Text+=line;
            }
            inputStream.close();
        }catch (Exception ex){}
        return Text;
    }



    // Called when the "Detect" button is clicked.
    public void identify(View view) {
        // Start detection task only if the image to detect is selected.
        if (detected && mPersonGroupId != null) {
            // Start a background task to identify faces in the image.
            List<UUID> faceIds = new ArrayList<>();
            for (Face face:  mFaceListAdapter.faces) {
                faceIds.add(face.faceId);
            }

          //  setAllButtonsEnabledStatus(false);

            new IdentificationTask(mPersonGroupId).execute(
                    faceIds.toArray(new UUID[faceIds.size()]));
        } else {
            // Not detected or person group exists.
          //  setInfo("Please select an image and create a person group first.");
        }
    }


    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

     //   setAllButtonsEnabledStatus(false);

        // Start a background task to detect faces in the image.
        new DetectionTask().execute(inputStream);
    }
    // Show the status of background detection task on screen.
    private void setUiDuringBackgroundTask(String progress) {
        progressDialog.setMessage(progress);

      //  setInfo(progress);
    }

    private void setUiBeforeBackgroundTask() {
        progressDialog.show();
    }
    // Background task of face detection.
    public class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            //  FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            FaceServiceClient faceServiceClient=new FaceServiceRestClient("c16ccb9c529d482e90b9acead3aae75d");
            try{
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.
            setUiDuringBackgroundTask(values[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            progressDialog.dismiss();

           // setAllButtonsEnabledStatus(true);

            if (result != null) {
                // Set the adapter of the ListView which contains the details of detected faces.
                mFaceListAdapter = new FaceListAdapter(result);
                ListView listView = (ListView) getView().findViewById(R.id.list_identified_faces);
                listView.setAdapter(mFaceListAdapter);

                if (result.length == 0) {
                    detected = false;
                //    setInfo("No faces detected!");
                } else {
                    detected = true;
                    identify(getView());
                  //  setInfo("Click on the \"Identify\" button to identify the faces in image.");
                }
            } else {
                detected = false;
            }

          //  refreshIdentifyButtonEnabledStatus();
        }
    }

    // The adapter of the GridView which contains the details of the detected faces.
    private class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        List<Face> faces;

        List<IdentifyResult> mIdentifyResults;

        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;

        // Initialize with detection result.
        FaceListAdapter(Face[] detectionResult) {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();
            mIdentifyResults = new ArrayList<>();

            if (detectionResult != null) {
                faces = Arrays.asList(detectionResult);
                for (Face face: faces) {
                    try {
                        // Crop face thumbnail with five main landmarks drawn from original image.
                        faceThumbnails.add(ImageHelper.generateFaceThumbnail(
                                mBitmap, face.faceRectangle));
                    } catch (IOException e) {
                        // Show the exception when generating face thumbnail fails.
                     //   setInfo(e.getMessage());
                    }
                }
            }
        }

        public void setIdentificationResult(IdentifyResult[] identifyResults) {
            mIdentifyResults = Arrays.asList(identifyResults);
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {

                LayoutInflater layoutInflater =
                        (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(
                        R.layout.item_face_with_description, parent, false);
            }
            convertView.setId(position);

            // Show the face thumbnail.
            ((ImageView)convertView.findViewById(R.id.face_thumbnail)).setImageBitmap(
                    faceThumbnails.get(position));

            if (mIdentifyResults.size() == faces.size()) {
                // Show the face details.
                DecimalFormat formatter = new DecimalFormat("#0.00");
                if (mIdentifyResults.get(position).candidates.size() > 0) {
                    String personId =
                            mIdentifyResults.get(position).candidates.get(0).personId.toString();
                    String personName = StorageHelper.getPersonName(
                            personId, mPersonGroupId, getActivity());
                    String identity = "Person: " + personName + "\n"
                            + "Confidence: " + formatter.format(
                            mIdentifyResults.get(position).candidates.get(0).confidence);
                    ((TextView) convertView.findViewById(R.id.text_detected_face)).setText(
                            identity);
                } else {
                    ((TextView) convertView.findViewById(R.id.text_detected_face)).setText(
                            R.string.face_cannot_be_identified);
                }
            }

            return convertView;
        }
    }


}
