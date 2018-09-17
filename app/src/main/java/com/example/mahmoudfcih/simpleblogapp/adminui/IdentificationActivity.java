package com.example.mahmoudfcih.simpleblogapp.adminui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;


import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.example.mahmoudfcih.simpleblogapp.FireBaseHelper;

import com.example.mahmoudfcih.simpleblogapp.MainActivity;
import com.example.mahmoudfcih.simpleblogapp.R;

import com.example.mahmoudfcih.simpleblogapp.adminpersongroupmanagement.AdminPersonGroupListActivity;
import com.example.mahmoudfcih.simpleblogapp.helper.ImageHelper;
import com.example.mahmoudfcih.simpleblogapp.helper.LogHelper;
import com.example.mahmoudfcih.simpleblogapp.helper.StorageHelper;

import com.example.mahmoudfcih.simpleblogapp.ui.SelectImageActivity;


import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.squareup.picasso.Callback;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.UUID;

public class IdentificationActivity extends AppCompatActivity {


    String PersonGroupIdss="00bfdf0a-c506-4692-8e43-42f3b7b04a9b";


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
            logString += " in group " + mPersonGroupId;
            addLog(logString);

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
                addLog(e.getMessage());
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
    String mPersonGroupId;

    boolean detected;
    FaceListAdapter mFaceListAdapter;

    PersonGroupListAdapter mPersonGroupListAdapter;
    private Button MainBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        detected = false;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
        MainBtn=(Button)findViewById(R.id.logout);
        MainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });


//////////////////////////////////////////////////////////////////


        ///////////////////////////////////


        ///////////////////////////////////////////
/////////////////////////////////////////////////////////////

        LogHelper.clearIdentificationLog();
    }

    public void groupHistory(){
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getPersonGroupName(PersonGroupIdss, new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                FireBaseHelper.Algorithm alg= (FireBaseHelper.Algorithm) Data;
                StorageHelper.setPersonGroupName(alg.personGroupId,alg.personGroupName,IdentificationActivity.this);
            }
        });

    }

    public void personsHistory(){
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getAllPersonIds(PersonGroupIdss, new FireBaseHelper.OnGetDataListener() {
            @Override
            public void onSuccess(Object Data) {
                List<FireBaseHelper.Algorithm> persons= (List<FireBaseHelper.Algorithm>) Data;
                for(int i=0 ; i< persons.size(); i++){
                    StorageHelper.setPersonName(persons.get(i).personId, persons.get(i).personName, PersonGroupIdss, IdentificationActivity.this);
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupHistory();
        personsHistory();
        facesHistory();

    }

    public void facesHistory(){
        final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
        algorithm.getAllPersonIds(PersonGroupIdss, new FireBaseHelper.OnGetDataListener() {
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
                                            final ImageView imagesecret=(ImageView)findViewById(R.id.imagesecret);
                                            Picasso.with(IdentificationActivity.this).load(FaceUri).into(imagesecret, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    Bitmap image=((BitmapDrawable)imagesecret.getDrawable()).getBitmap();
                                    //                Uri uri=Uri.parse(String.valueOf(image));
                                                    FileOutputStream fileOutputStream = null;
                                                    try {
                                                        File file = new File(getApplicationContext().getFilesDir(), Faceid.get(finalJ).personFaceId);
                                                        fileOutputStream = new FileOutputStream(file);
                                                       image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                                        fileOutputStream.flush();

                                                        Uri uri1 = Uri.fromFile(file);
                                                        StorageHelper.setFaceUri(
                                                                Faceid.get(finalJ).personFaceId, uri1.toString(), persons.get(finalI).personId, IdentificationActivity.this);
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
    @Override
    protected void onResume() {
        super.onResume();

        ListView listView = (ListView) findViewById(R.id.list_person_groups_identify);
        mPersonGroupListAdapter = new PersonGroupListAdapter();
        listView.setAdapter(mPersonGroupListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setPersonGroupSelected(position);
            }
        });

        if (mPersonGroupListAdapter.personGroupIdList.size() != 0) {
            setPersonGroupSelected(0);
        } else {
            setPersonGroupSelected(-1);
        }
    }


    void setPersonGroupSelected(int position) {
        TextView textView = (TextView) findViewById(R.id.text_person_group_selected);
        if (position > 0) {
            String personGroupIdSelected = mPersonGroupListAdapter.personGroupIdList.get(position);
            mPersonGroupListAdapter.personGroupIdList.set(
                    position, mPersonGroupListAdapter.personGroupIdList.get(0));
            mPersonGroupListAdapter.personGroupIdList.set(0, personGroupIdSelected);
            ListView listView = (ListView) findViewById(R.id.list_person_groups_identify);
            listView.setAdapter(mPersonGroupListAdapter);
            setPersonGroupSelected(0);
        } else if (position < 0) {
            setIdentifyButtonEnabledStatus(false);
            textView.setTextColor(Color.RED);
            textView.setText(R.string.no_person_group_selected_for_identification_warning);
        } else {
            mPersonGroupId = mPersonGroupListAdapter.personGroupIdList.get(0);
            String personGroupName = StorageHelper.getPersonGroupName(
                    mPersonGroupId, IdentificationActivity.this);
            refreshIdentifyButtonEnabledStatus();
            textView.setTextColor(Color.BLACK);
            textView.setText(String.format("Person group to use: %s", personGroupName));
        }
    }
    private void setUiBeforeBackgroundTask() {
        progressDialog.show();
    }

    // Show the status of background detection task on screen.
    private void setUiDuringBackgroundTask(String progress) {
        progressDialog.setMessage(progress);

        setInfo(progress);
    }

    // Show the result on screen when detection is done.
    private void setUiAfterIdentification(IdentifyResult[] result, boolean succeed) {
        progressDialog.dismiss();

        setAllButtonsEnabledStatus(true);
        setIdentifyButtonEnabledStatus(false);

        if (succeed) {
            // Set the information about the detection result.
            setInfo("Identification is done");

            if (result != null) {
                mFaceListAdapter.setIdentificationResult(result);

                String logString = "Response: Success. ";
                for (IdentifyResult identifyResult: result) {
                    logString += "Face " + identifyResult.faceId.toString() + " is identified as "
                            + (identifyResult.candidates.size() > 0
                            ? identifyResult.candidates.get(0).personId.toString()
                            : "Unknown Person")
                            + ". ";
                }
                addLog(logString);

                // Show the detailed list of detected faces.
                ListView listView = (ListView) findViewById(R.id.list_identified_faces);
                listView.setAdapter(mFaceListAdapter);
            }
        }
    }


    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
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

            setAllButtonsEnabledStatus(true);

            if (result != null) {
                // Set the adapter of the ListView which contains the details of detected faces.
                mFaceListAdapter = new FaceListAdapter(result);
                ListView listView = (ListView) findViewById(R.id.list_identified_faces);
                listView.setAdapter(mFaceListAdapter);

                if (result.length == 0) {
                    detected = false;
                    setInfo("No faces detected!");
                } else {
                    detected = true;
                    setInfo("Click on the \"Identify\" button to identify the faces in image.");
                }
            } else {
                detected = false;
            }

            refreshIdentifyButtonEnabledStatus();
        }
    }
    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The image selected to detect.
    private Bitmap mBitmap;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    detected = false;

                    // If image is selected successfully, set the image URI and bitmap.
                    Uri imageUri = data.getData();
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            imageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.image);
                        imageView.setImageBitmap(mBitmap);
                    }

                    // Clear the identification result.
                    FaceListAdapter faceListAdapter = new FaceListAdapter(null);
                    ListView listView = (ListView) findViewById(R.id.list_identified_faces);
                    listView.setAdapter(faceListAdapter);

                    // Clear the information panel.
                    setInfo("");

                    // Start detecting in image.
                    detect(mBitmap);
                }
                break;
            default:
                break;
        }
    }

    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        setAllButtonsEnabledStatus(false);

        // Start a background task to detect faces in the image.
        new DetectionTask().execute(inputStream);
    }

    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
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

            setAllButtonsEnabledStatus(false);

            new IdentificationTask(mPersonGroupId).execute(
                    faceIds.toArray(new UUID[faceIds.size()]));
        } else {
            // Not detected or person group exists.
            setInfo("Please select an image and create a person group first.");
        }
    }
    public void managePersonGroups(View view) {
        Intent intent = new Intent(this, AdminPersonGroupListActivity.class);
        startActivity(intent);

        refreshIdentifyButtonEnabledStatus();
    }

    public void viewLog(View view) {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
    }
    // Add a log item.
    private void addLog(String log) {
        LogHelper.addIdentificationLog(log);
    }

    // Set whether the buttons are enabled.
    private void setAllButtonsEnabledStatus(boolean isEnabled) {
        Button selectImageButton = (Button) findViewById(R.id.manage_person_groups);
        selectImageButton.setEnabled(isEnabled);

        Button groupButton = (Button) findViewById(R.id.select_image);
        groupButton.setEnabled(isEnabled);

        Button identifyButton = (Button) findViewById(R.id.identify);
        identifyButton.setEnabled(isEnabled);



    }

    // Set the group button is enabled or not.
    private void setIdentifyButtonEnabledStatus(boolean isEnabled) {
        Button button = (Button) findViewById(R.id.identify);
        button.setEnabled(isEnabled);
    }

    // Set the group button is enabled or not.
    private void refreshIdentifyButtonEnabledStatus() {
        if (detected && mPersonGroupId != null) {
            setIdentifyButtonEnabledStatus(true);
        } else {
            setIdentifyButtonEnabledStatus(false);
        }
    }
    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
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
                        setInfo(e.getMessage());
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
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                            personId, mPersonGroupId, IdentificationActivity.this);
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

    // The adapter of the ListView which contains the person groups.
    private class PersonGroupListAdapter extends BaseAdapter {

        List<String> personGroupIdList;

        // Initialize with detection result.
        PersonGroupListAdapter() {
            personGroupIdList = new ArrayList<>();

            //Set<String> personGroupIds
              //      = StorageHelper.getAllPersonGroupIds(IdentificationActivity.this);

          //  for (String personGroupId: personGroupIds) {

            //    personGroupIdList.add(personGroupId);
            String personGroupId=PersonGroupIdss;
                personGroupIdList.add(personGroupId);
                if (mPersonGroupId != null && personGroupId.equals(mPersonGroupId)) {
                    personGroupIdList.set(
                            personGroupIdList.size() - 1,
                            mPersonGroupListAdapter.personGroupIdList.get(0));
                    mPersonGroupListAdapter.personGroupIdList.set(0, personGroupId);
                }
            }
        //}

        @Override
        public int getCount() {
            return personGroupIdList.size();
        }

        @Override
        public Object getItem(int position) {
            return personGroupIdList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {

                LayoutInflater layoutInflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_person_group, parent, false);
            }
            convertView.setId(position);

            // set the text of the item
            String personGroupName = StorageHelper.getPersonGroupName(
                    personGroupIdList.get(position), IdentificationActivity.this);
            int personNumberInGroup = StorageHelper.getAllPersonIds(
                    personGroupIdList.get(position), IdentificationActivity.this).size();



          //  int personNumberInGroup =StorageHelper.getPersonIds(IdentificationActivity.this).size();

            ((TextView)convertView.findViewById(R.id.text_person_group)).setText(
                    String.format(
                            "%s (Person count: %d)",
                            personGroupName,
                            personNumberInGroup));

            if (position == 0) {
                ((TextView)convertView.findViewById(R.id.text_person_group)).setTextColor(
                        Color.parseColor("#3399FF"));
            }

            return convertView;
        }
    }
}
