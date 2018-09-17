package com.example.mahmoudfcih.simpleblogapp.adminpersongroupmanagement;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;


import com.example.mahmoudfcih.simpleblogapp.R;
import com.example.mahmoudfcih.simpleblogapp.helper.LogHelper;
import com.example.mahmoudfcih.simpleblogapp.helper.StorageHelper;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

import java.util.ArrayList;
import java.util.List;

import java.util.Set;
import java.util.UUID;

public class AdminPersonGroupListActivity extends AppCompatActivity {
    private static Instrumentation instrumentation;
    // Background task of deleting a person group.
    class DeletePersonGroupTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            // Get an instance of face service client.
            // FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            FaceServiceClient faceServiceClient=new FaceServiceRestClient("c16ccb9c529d482e90b9acead3aae75d");
            try{
                publishProgress("Deleting selected person groups...");
                addLog("Request: Delete Group " + params[0]);

                faceServiceClient.deletePersonGroup(params[0]);
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
                setInfo("Person group " + result + " successfully deleted");
                addLog("Response: Success. Deleting Group " + result + " succeed");
            }
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
    PersonGroupsListAdapter personGroupsListAdapter;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_person_group_list);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));

        initializeListView();
    }
    private void initializeListView() {
        ListView listView = (ListView) findViewById(R.id.list_person_groups);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                personGroupsListAdapter.personGroupChecked.set(position, checked);

                ListView listView = (ListView) findViewById(R.id.list_person_groups);
                listView.setAdapter(personGroupsListAdapter);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_delete_items, menu);

                personGroupsListAdapter.longPressed = true;

                ListView listView = (ListView) findViewById(R.id.list_person_groups);
                listView.setAdapter(personGroupsListAdapter);

                Button addNewItem = (Button)findViewById(R.id.add_person_group);
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
                personGroupsListAdapter.longPressed = false;

                for (int i = 0; i < personGroupsListAdapter.personGroupChecked.size(); ++i) {
                    personGroupsListAdapter.personGroupChecked.set(i, false);
                }

                ListView listView = (ListView) findViewById(R.id.list_person_groups);
                listView.setAdapter(personGroupsListAdapter);

                Button addNewItem = (Button)findViewById(R.id.add_person_group);
                addNewItem.setEnabled(true);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!personGroupsListAdapter.longPressed) {
                    final String personGroupId = personGroupsListAdapter.personGroupIdList.get(position);
                    String personGroupName = StorageHelper.getPersonGroupName(
                            personGroupId, AdminPersonGroupListActivity.this);
                    Intent intent = new Intent(AdminPersonGroupListActivity.this, AdminPersonGroupActivity.class);
                    intent.putExtra("AddNewPersonGroup", false);
                    intent.putExtra("PersonGroupName", personGroupName);
                    intent.putExtra("PersonGroupId", personGroupId);
                    startActivity(intent);
                  /*  final FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
                    algorithm.getPersonGroupName(personGroupId, new FireBaseHelper.OnGetDataListener() {
                        @Override
                        public void onSuccess(Object GroupName) {

                            Intent intent = new Intent(AdminPersonGroupListActivity.this, AdminPersonGroupActivity.class);
                            intent.putExtra("AddNewPersonGroup", false);
                            intent.putExtra("PersonGroupName", GroupName.toString());
                            intent.putExtra("PersonGroupId", personGroupId);
                            startActivity(intent);
                        }
                    });*/



                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView listView = (ListView) findViewById(R.id.list_person_groups);
        personGroupsListAdapter = new PersonGroupsListAdapter();
        listView.setAdapter(personGroupsListAdapter);
    }

    public void addPersonGroup(View view) {
        String personGroupId = UUID.randomUUID().toString();

        Intent intent = new Intent(AdminPersonGroupListActivity.this, AdminPersonGroupActivity.class);
        intent.putExtra("AddNewPersonGroup", true);
        intent.putExtra("PersonGroupName", "");
        intent.putExtra("PersonGroupId", personGroupId);
        startActivity(intent);
    }

    public void doneAndSave(View view) {
        finish();
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

    private class PersonGroupsListAdapter extends BaseAdapter {

        List<String> personGroupIdList;
        List<Boolean> personGroupChecked;
        boolean longPressed;

        PersonGroupsListAdapter() {
            longPressed = false;
            personGroupIdList = new ArrayList<>();
            personGroupChecked = new ArrayList<>();

            Set<String> personGroupIds = StorageHelper.getAllPersonGroupIds(AdminPersonGroupListActivity.this);
          /*  FireBaseHelper.Algorithm algorithm=new FireBaseHelper.Algorithm();
            algorithm.getAllPersonGroupIds(new FireBaseHelper.OnGetDataListener() {
                @Override
                public void onSuccess(Object Data) {
                //    FireBaseHelper.Algorithm us = (FireBaseHelper.Algorithm) Data;
                    List<FireBaseHelper.Algorithm> persons= (List<FireBaseHelper.Algorithm>) Data;
                    for(FireBaseHelper.Algorithm personId :persons) {
                        personGroupIdList.add(personId.personGroupId);
                       personGroupChecked.add(false);
                    }

                }
            });*/

            for (String personGroupId: personGroupIds) {
                personGroupIdList.add(personGroupId);
                personGroupChecked.add(false);
            }
        }

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
            // set the item view
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_person_group_with_checkbox, parent, false);
            }
            convertView.setId(position);

            // set the text of the item
            String personGroupName = StorageHelper.getPersonGroupName(
                    personGroupIdList.get(position), AdminPersonGroupListActivity.this);
            final View finalConvertView = convertView;
         /*   algorithm.getPersonGroupName(personGroupIdList.get(position), new FireBaseHelper.OnGetDataListener() {
                @Override
                public void onSuccess(Object GroupName) {
                //    FireBaseHelper.Algorithm us = (FireBaseHelper.Algorithm) Data;
                   /

                     //   int personNumberInGroup=0;

                    ((TextView) finalConvertView.findViewById(R.id.text_person_group)).setText(
                            String.format(GroupName.toString()));
                }
            });*/
             int personNumberInGroup = StorageHelper.getAllPersonIds(
                        personGroupIdList.get(position), AdminPersonGroupListActivity.this).size();
              ((TextView) finalConvertView.findViewById(R.id.text_person_group)).setText(
                    String.format("%s (Person count: %d)",personGroupName, personNumberInGroup));


            // set the checked status of the item
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_person_group);
            if (longPressed) {
                checkBox.setVisibility(View.VISIBLE);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        personGroupChecked.set(position, isChecked);
                    }
                });
                checkBox.setChecked(personGroupChecked.get(position));
            } else {
                checkBox.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }
    private void deleteSelectedItems() {
        List<String> newPersonGroupIdList = new ArrayList<>();
        List<Boolean> newPersonGroupChecked = new ArrayList<>();
        List<String> personGroupIdsToDelete = new ArrayList<>();
        for (int i = 0; i < personGroupsListAdapter.personGroupChecked.size(); ++i) {
            if (personGroupsListAdapter.personGroupChecked.get(i)) {
                String personGroupId = personGroupsListAdapter.personGroupIdList.get(i);
                personGroupIdsToDelete.add(personGroupId);
                new DeletePersonGroupTask().execute(personGroupId);
            } else {
                newPersonGroupIdList.add(personGroupsListAdapter.personGroupIdList.get(i));
                newPersonGroupChecked.add(false);
            }
        }

        StorageHelper.deletePersonGroups(personGroupIdsToDelete, this);

        personGroupsListAdapter.personGroupIdList = newPersonGroupIdList;
        personGroupsListAdapter.personGroupChecked = newPersonGroupChecked;
        personGroupsListAdapter.notifyDataSetChanged();
    }

}
