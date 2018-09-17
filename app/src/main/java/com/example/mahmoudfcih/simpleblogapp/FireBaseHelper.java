package com.example.mahmoudfcih.simpleblogapp;


import android.graphics.Bitmap;
import android.net.Uri;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class FireBaseHelper {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference myRootRef = database.getReference();
    private static final DatabaseReference mUsers = myRootRef.child("Users");
    private static final DatabaseReference mObjects = myRootRef.child("Objects");
    private static final DatabaseReference mCompanies = myRootRef.child("Company");
    private static final DatabaseReference mFeedbacks = myRootRef.child("feedback");
    private static final DatabaseReference mUser_Type = myRootRef.child("User_Type");
    private static final DatabaseReference mCategories = myRootRef.child("Categorys");
    private static final DatabaseReference mpersonGroupSet = myRootRef.child("PersonGroupSet");
    private static final DatabaseReference mpersonGroupMap = myRootRef.child("PersonGroupMap");
    private static final DatabaseReference mFaceIdUriMap=myRootRef.child("FaceIdUriMap");

    private static final DatabaseReference mPersonFaceMap=myRootRef.child("PersonFaceMap");
    private static final StorageReference mFaceStorage= FirebaseStorage.getInstance().getReference().child("AlgorithmFaceUri");

    public interface OnGetDataListener {
        void onSuccess(Object Data);
    }
    public interface OnGetDataListListener<T> {
        void onSuccess(List<T> Data);
    }



    //region Algorithm_Table
    public static class Algorithm{
        public String personGroupId;
        public String personId;
        public String personFaceId;
        public String personGroupName;
        public String personName;
        public String FaceUri;
        public Algorithm(){}

        public Algorithm(String faceUri, String personFaceId, String personGroupId, String personGroupName, String personId, String personName) {
            FaceUri = faceUri;
            this.personFaceId = personFaceId;
            this.personGroupId = personGroupId;
            this.personGroupName = personGroupName;
            this.personId = personId;
            this.personName = personName;
        }

        // Group fun.
        public void setPersonGroupName(final String personGroupId, final String personGroupName){
            final Map<String,String> Values=new HashMap<>();
            Query mPersonName=mpersonGroupSet.orderByChild("PersonGroupId").equalTo(personGroupId);
            mPersonName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator= dataSnapshot.getChildren().iterator();
                    if (!iterator.hasNext()) {
                            Values.put(Table.PersonGroupId.text, personGroupId);
                            Values.put(Table.PersonGroupName.text, personGroupName);
                            DatabaseReference myref = mpersonGroupSet.push();
                            myref.setValue(Values);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
        public void getAllPersonGroupIds(final OnGetDataListener listener){
            final List<Algorithm> Items=new ArrayList<>();
            mpersonGroupSet.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        final Algorithm Item = new Algorithm("","",
                                postSnapshot.child(Table.PersonGroupId.text).getValue().toString(),
                                postSnapshot.child(Table.PersonGroupName.text).getValue().toString(),"","");
                        Items.add(Item);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                    }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });


        }


        public void getPersonGroupName(final String key, final  OnGetDataListener listener){
          Query mPersonName=mpersonGroupSet.orderByChild("PersonGroupId").equalTo(key);
            mPersonName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Algorithm algorithm=new Algorithm();
                        final Iterator iterator= dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {

                        DataSnapshot postSnapshot=(DataSnapshot)iterator.next();
                        algorithm.personGroupName= (String) postSnapshot.child(Table.PersonGroupName.text).getValue();
                        algorithm.personGroupId=postSnapshot.child(Table.PersonGroupId.text).getValue().toString();
                        listener.onSuccess(algorithm);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        // person fun.
        public void getPersonName(String personId, String personGroupId, final OnGetDataListener listener){
            Query persnonName=mpersonGroupMap.child(personGroupId+"PersonIdSet").orderByChild("PersonId").equalTo(personId);
            persnonName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator= dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot=(DataSnapshot)iterator.next();
                        listener.onSuccess(postSnapshot.child(Table.PersonName.text).getValue());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        public void setPersonName(final String personId, final String personName, final String personGroupId){
            Query personExsit=mpersonGroupMap.child(personGroupId+"PersonIdSet").orderByChild("PersonId").equalTo(personId);
            personExsit.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator= dataSnapshot.getChildren().iterator();
                    if(!iterator.hasNext()){
                        Map<String,String> Values=new HashMap<>();
                        Values.put(Table.PersonId.text,personId);
                        Values.put(Table.PersonName.text,personName);
                        DatabaseReference myref=mpersonGroupMap.child(personGroupId+"PersonIdSet").push();
                        myref.setValue(Values);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        public void getAllPersonIds(String personGroupId,final  OnGetDataListener listener){
            final List<Algorithm> Items=new ArrayList<>();

            mpersonGroupMap.child(personGroupId+"PersonIdSet").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        final Algorithm Item = new Algorithm("","","","",
                                postSnapshot.child(Table.PersonId.text).getValue().toString(),
                                postSnapshot.child(Table.PersonName.text).getValue().toString());
                        Items.add(Item);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }

        //Image fun.
        public void getFaceUri(final String FaceId, final OnGetDataListener listener){
          //  Query mFaceUri=mFaceIdUriMap.orderByChild("PersonFaceId").equalTo(FaceId);
            Query mPersonId=mPersonFaceMap.orderByChild("FaceId").equalTo(FaceId);
            mPersonId.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator= dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot=(DataSnapshot)iterator.next();
                        String PersonId=postSnapshot.child("PersonId").getValue().toString();
                       // listener.onSuccess(postSnapshot.child(Table.FaceUri.text).getValue());
                        Query mFaceUri=mFaceIdUriMap.child(PersonId+"FaceIdSet").orderByChild("PersonFaceId").equalTo(FaceId);
                        mFaceUri.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Iterator iterator1=dataSnapshot.getChildren().iterator();
                                while (iterator1.hasNext()){
                                    DataSnapshot postSnapshot1=(DataSnapshot)iterator1.next();
                                    listener.onSuccess(postSnapshot1.child(Table.FaceUri.text).getValue());
                                }
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


        }
        public void getAllFaceIds(String personId, final OnGetDataListener listener){
            final List<Algorithm>Items=new ArrayList<>();
            mFaceIdUriMap.child(personId+"FaceIdSet").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        final Algorithm Item = new Algorithm(
                                postSnapshot.child(Table.FaceUri.text).getValue().toString(),postSnapshot.child(Table.PersonFaceId.text).getValue().toString(),"","","","");
                        Items.add(Item);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        public void setFaceUri(final String personFaceId, final Bitmap Face , final String personId){
            final Map<String,String>Values=new HashMap<>();
            final Map<String,String>Valuess=new HashMap<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Face.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] data = baos.toByteArray();
            Uri FaceUri=Uri.parse(Face.toString());
           // byte[] data=baos.toByteArray();
            StorageReference filepath=mFaceStorage.child(FaceUri.getLastPathSegment());
            filepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String DownloadUri= String.valueOf(taskSnapshot.getDownloadUrl());
                    Values.put(Table.PersonFaceId.text,personFaceId);
                    Values.put(Table.FaceUri.text,DownloadUri);
                    Valuess.put(Table.PersonFaceId.text,personFaceId);
                    Valuess.put(Table.PersonId.text,personId);
                    DatabaseReference myref=mFaceIdUriMap.child(personId+"FaceIdSet").push();
                    DatabaseReference myreff=mPersonFaceMap.push();
                    myreff.setValue(Valuess);
                    myref.setValue(Values);
                }
            });


        }



        public enum Table {
            PersonGroupId("PersonGroupId"),
            PersonGroupName("PersonGroupName"),PersonId("PersonId"),
            PersonName("PersonName"),PersonFaceId("PersonFaceId"),FaceUri("FaceUri");




            private final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }

    }
    //endregion

    //region Users_Table
    public static class Users {
        public String Name;
        public String Image;
        public String Country;
        public String City;
        public String Gender;
        public String Birthday;
        public String Key;
        public static final DatabaseReference Ref = myRootRef.child("Users");


        public Users() {

        }

        /**
         * @param name  UserName
         * @param image UserImage
//         * @param email UserEmail
//         * @param type  UserType 1 = Admin , 2 = Company , 3 = Designer
         */
        public Users(String name, String image,String country,String city,String birthday,String gender) {
            Name = name;
            Image = image;
            Country = country;
            City = city;
           Birthday = birthday;
            Gender=gender;
        }

        public String AddUser() {
            Map<String, String> Values = new HashMap<>();
            Values.put(Table.Name.text, Name);
            Values.put(Table.Image.text, Image);
            DatabaseReference myref = mUsers.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void AddUser(String Key) {
            Map<String, String> Values = new HashMap<>();
            Values.put(Table.Name.text, Name);
            Values.put(Table.Image.text, Image);
            Values.put(Table.City.text,City);
            Values.put(Table.Country.text,Country);
            Values.put(Table.Birthday.text,Birthday);
            Values.put(Table.Gender.text,Gender);
           /* Values.put(Table.Email.text, Email);
            Values.put(Table.Type.text, Type);*/
            DatabaseReference myref = mUsers.child(Key);
            myref.setValue(Values);
        }

        public void UpdateUser(String key) {
            Map<String, String> Values = new HashMap<>();
            Values.put(Table.Name.text, Name);
       //     Values.put(Table.Image.text, Image);
            Values.put(Table.Country.text, Country);
            Values.put(Table.City.text, City);
            Values.put(Table.Birthday.text,Birthday);
            Values.put(Table.Gender.text,Gender);
            Values.put(Table.Image.text,Image);
            DatabaseReference myref = mUsers.child(key);
            myref.setValue(Values);
        }

        public void getUser(String key, final OnGetDataListener listener) {
            mUsers.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    if (iterator.hasNext()) {
                        final Users users = new Users();
                        users.Key = dataSnapshot.getKey();
                            users.Name = dataSnapshot.child(Table.Name.text).getValue().toString();
                            users.Image = dataSnapshot.child(Table.Image.text).getValue().toString();
//                    users.Email = dataSnapshot.child(Table.Email.text).getValue().toString();
//                    users.Type = dataSnapshot.child(Table.Type.text).getValue().toString();
//                    User_Types.getUser_Type(users.Type, new OnGetDataListener() {
//                        @Override
//                        public void onSuccess(Object Data) {
//                            users.User_Types = (FireBaseHelper.User_Types) Data;
                            users.Country=dataSnapshot.child(Table.Country.text).getValue().toString();
                            users.City=dataSnapshot.child(Table.City.text).getValue().toString();
                            users.Birthday=dataSnapshot.child(Table.Birthday.text).getValue().toString();
                            users.Gender=dataSnapshot.child(Table.Gender.text).getValue().toString();


                        listener.onSuccess(users);
//                        }
//                    });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void getUsers(Table table, String Value, final OnGetDataListener listener) {
            final List<Users> Items = new ArrayList<>();
            Query query = mUsers.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        final Users Item = new Users(
                                postSnapshot.child(Table.Name.text).getValue().toString(),
                                postSnapshot.child(Table.Image.text).getValue().toString(),"","","","");
                        Item.Key = postSnapshot.getKey();
                        Items.add(Item);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void getUsers(final OnGetDataListener listener) {
            final List<Users> Items = new ArrayList<>();
            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        final Users Item = new Users(
                                postSnapshot.child(Table.Name.text).getValue().toString(),
                                postSnapshot.child(Table.Image.text).getValue().toString(),"","","","");
                        Item.Key = postSnapshot.getKey();
                        Items.add(Item);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void Where(Table table, String Value, final OnGetDataListListener<Users> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Users> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Users obj = new Users();
                        obj.Key = postSnapshot.getKey();

                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }




        public enum Table {
            Name("name"),
            Image("image"),
            Country("country"),
            City("city"),
            Gender("gender"),
            Birthday("birthday")
            ;
            private final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }


    }
    //endregion



}
