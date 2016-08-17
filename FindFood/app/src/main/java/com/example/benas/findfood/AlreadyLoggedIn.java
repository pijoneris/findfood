package com.example.benas.findfood;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class AlreadyLoggedIn extends android.support.v4.app.Fragment {


    //Profile widgets
    private EditText profile_mail;
    private EditText profile_phone_number;
    private EditText profile_description;
    private EditText profile_truck_names;
    private EditText profile_monday;
    private EditText profile_tuesday;
    private EditText profile_wednesday;
    private EditText profile_thursday;
    private EditText profile_friday;
    private EditText profile_saturday;
    private EditText profile_sunday;
    private EditText profile_menu;
    private EditText profile_slogan;
    private ImageView profile_background_photo;
    private int whichImage;
    private EditText profile_special_offers;
    private SharedPreferences sharedPreferences;
    private Context context;
    private TabActivityLoader loader;
    private ImageView save_button;
    private ScrollView profile_scrollview;

    //Background photos
    private JSONArray jsonArrayExtBackground;
    private JSONObject jsonObjectExtBackground;

    private JSONArray jsonArrayExtMenu;
    private JSONObject jsonObjectExtMenu;



    //Markers pictures
    private ImageView butcher_marker;
    private ImageView sandwich_marker;
    private ImageView candy_marker;
    private ImageView drink_marker;
    private ImageView burger_marker;
    private int whichMarker;

    //Crediantials from sharedPreferences
    private String username;
    private String password;






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_already_logged_in, container, false);

        loader = new TabActivityLoader();


        //Initializing widgets
        profile_special_offers = (EditText) rootView.findViewById(R.id.profile_special_offers);
        profile_menu = (EditText) rootView.findViewById(R.id.profile_menu);
        profile_mail = (EditText) rootView.findViewById(R.id.profile_mail);
        profile_description = (EditText) rootView.findViewById(R.id.profile_description);
        profile_phone_number = (EditText) rootView.findViewById(R.id.number);
        profile_truck_names = (EditText) rootView.findViewById(R.id.truck_name);
        profile_slogan = (EditText) rootView.findViewById(R.id.profile_slogan);
        profile_monday = (EditText) rootView.findViewById(R.id.profile_monday);
        profile_tuesday = (EditText) rootView.findViewById(R.id.profile_tuesday);
        profile_wednesday = (EditText) rootView.findViewById(R.id.profile_wednesday);
        profile_thursday = (EditText) rootView.findViewById(R.id.profile_thursday);
        profile_friday = (EditText) rootView.findViewById(R.id.profile_friday);
        profile_saturday = (EditText) rootView.findViewById(R.id.profile_saturday);
        profile_sunday = (EditText) rootView.findViewById(R.id.profile_sunday);
        profile_background_photo = (ImageView) rootView.findViewById(R.id.background_picture);
        save_button = (ImageView) rootView.findViewById(R.id.save_button);
        profile_scrollview = (ScrollView) rootView.findViewById(R.id.profile_scrollview);

        //Markers
        butcher_marker = (ImageView) rootView.findViewById(R.id.butcher_marker);
        burger_marker = (ImageView) rootView.findViewById(R.id.burger_marker);
        candy_marker = (ImageView) rootView.findViewById(R.id.candy_marker);
        drink_marker = (ImageView) rootView.findViewById(R.id.drink_marker);
        sandwich_marker = (ImageView) rootView.findViewById(R.id.sandwitch_marker);

        //Marker listeners
        butcher_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                butcher_marker();
            }
        });
        burger_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                burger_marker();
            }
        });
        candy_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                candy_marker();
            }
        });
        drink_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drink_marker();
            }
        });
        sandwich_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sandwich_marker();
            }
        });

        //Save button onclick
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        //Getting login crediantials from sharedPrefs.
        sharedPreferences = getActivity().getSharedPreferences("DataPrefs", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        password = sharedPreferences.getString("password", null);

             new FetchUserData(username).execute();

        //Starting background photo picker intent
        profile_background_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!CheckingUtils.isNetworkConnected(getActivity())) {
                    CheckingUtils.createErrorBox("You need internet connection to do that", getActivity());
                    return;
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                }


            }
        });


        profile_scrollview.setOnTouchListener(new View.OnTouchListener() {
            float startingY;
            float dy;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(dy>20 ){
                    TabActivityLoader.tabLayout.setVisibility(View.INVISIBLE);
                    startingY = motionEvent.getY();
                }if(dy<-5){

                    startingY = motionEvent.getY();
                    TabActivityLoader.tabLayout.setVisibility(View.VISIBLE);
                }
                dy = motionEvent.getY() - startingY;

                Log.i("TEST", "Starting Y: " + String.valueOf(startingY) + ", " + "dY: " + dy);
                return false;
            }
        });
        return rootView;
    }


    //Getting intent picker request code and starting Server Manager(Setting background and menu photos)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                String filePath = getPath(selectedImage);
                String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);

                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("png")) {
                    final String username = sharedPreferences.getString("username", null);
                    final String password = sharedPreferences.getString("password", null);
                    new ServerManager(getActivity(),AlreadyLoggedIn.this,"UPDATE_PICTURE").execute("UPDATE_PICTURE", username, password, filePath);

                } else {
                    //NOT IN REQUIRED FORMAT
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }



    //Updating already_logged_in info and inserting new info into user table
    public void save() {
        //User table info
        String offers = profile_special_offers.getText().toString();
        String mail = profile_mail.getText().toString();
        String menu = profile_menu.getText().toString();
        String description = profile_description.getText().toString();
        String number = profile_phone_number.getText().toString();
        String truck_name = profile_truck_names.getText().toString();
        String slogan = profile_slogan.getText().toString();
        final String username = sharedPreferences.getString("username", null);
        final String password = sharedPreferences.getString("password", null);

        //Schedule table info
        String monday = profile_monday.getText().toString();
        String tuesday = profile_tuesday.getText().toString();
        String wednesday = profile_wednesday.getText().toString();
        String thursday = profile_thursday.getText().toString();
        String friday = profile_friday.getText().toString();
        String saturday = profile_saturday.getText().toString();
        String sunday = profile_sunday.getText().toString();


        if (description.isEmpty()) {
            profile_description.setError("Fill this field to save");
            return;
        }
        if (slogan.isEmpty()) {
            profile_slogan.setError("Fill this field to save");
            return;
        }
        if (truck_name.isEmpty()) {
            profile_truck_names.setError("Fill this field to save");
            return;
        }


        new ServerManager(getActivity()).execute("UPDATE PROFILE", mail, username, password, "1" /*TODO: handle 1 and 0*/,
                description,menu, number, offers, slogan, truck_name, monday, tuesday, wednesday, thursday, friday, saturday, sunday, String.valueOf(whichImage));

    }


    //Method to use in Server Manager
    public void startBackgroundfetching() {
        new fetchBackgroundPhoto(username).execute();

    }


    //Setting markers
    public void butcher_marker() {
        candy_marker.setBackground(null);
        burger_marker.setBackground(null);
        drink_marker.setBackground(null);
        sandwich_marker.setBackground(null);
        butcher_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
        whichImage = 0;
    }

    public void sandwich_marker() {
        candy_marker.setBackground(null);
        burger_marker.setBackground(null);
        drink_marker.setBackground(null);
        sandwich_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
        butcher_marker.setBackground(null);
        whichImage = 1;
    }

    public void burger_marker() {
        candy_marker.setBackground(null);
        burger_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
        drink_marker.setBackground(null);
        sandwich_marker.setBackground(null);
        butcher_marker.setBackground(null);
        whichImage = 2;
    }

    public void candy_marker() {
        candy_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
        burger_marker.setBackground(null);
        drink_marker.setBackground(null);
        sandwich_marker.setBackground(null);
        butcher_marker.setBackground(null);
        whichImage = 3;
    }

    public void drink_marker() {
        candy_marker.setBackground(null);
        burger_marker.setBackground(null);
        drink_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
        sandwich_marker.setBackground(null);
        butcher_marker.setBackground(null);
        whichImage = 4;

    }
    //-------------------------------------------//

    class FetchUserData extends AsyncTask<Void, Void, Void> {

        private Bitmap profile_pic;
        private ProgressDialog progressDialog;
        private String username;
        private JSONObject userInfo;
        private JSONArray jsonArray;
        private JSONObject userSchedule;

        public FetchUserData(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Getting information...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                //Connect to mysql.
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://64.137.182.232/fetchProfileInfo.php");


                //JSON object.
                JSONObject jsonObject = new JSONObject();
                jsonObject.putOpt("username", username);


                EntityBuilder entity = EntityBuilder.create();
                entity.setText(jsonObject.toString());
                httpPost.setEntity(entity.build());

                //Getting response
                HttpResponse response = httpClient.execute(httpPost);
                String responseBody = EntityUtils.toString(response.getEntity());

                jsonArray = new JSONArray(responseBody);

                userInfo = jsonArray.getJSONObject(0);
                userSchedule = jsonArray.getJSONObject(1);

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DataPrefs", Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);

                String link_background_photo = "http://64.137.182.232/pictures/" + username + "." + userInfo.getString("ext_profile");


                Log.i("TEST", link_background_photo);


                profile_pic = getBitmapFromURL(link_background_photo);


                Log.i("TEST", "Username" + username + " background_photo " + link_background_photo + " menu photo");


            } catch (Exception e) {

            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try {
                //GENERAL info
                profile_mail.setText(userInfo.getString("mail"));
                profile_phone_number.setText(userInfo.getString("phone_number"));
                profile_description.setText(userInfo.getString("description"));
                profile_slogan.setText(userInfo.getString("slogan"));
                profile_truck_names.setText(userInfo.getString("truck_name"));
                profile_special_offers.setText(userInfo.getString("special_offer"));
                profile_menu.setText(userInfo.getString("menu"));
                profile_background_photo.setImageBitmap(profile_pic);
                TabActivityLoader.isChecked = userInfo.getString("is_working").equals("1") ?  true : false;
                whichImage = Integer.parseInt(userInfo.getString("marker_icon"));


                switch (whichImage) {
                    case 0:
                        candy_marker.setBackground(null);
                        burger_marker.setBackground(null);
                        drink_marker.setBackground(null);
                        sandwich_marker.setBackground(null);
                        butcher_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
                        break;
                    case 1:
                        candy_marker.setBackground(null);
                        burger_marker.setBackground(null);
                        drink_marker.setBackground(null);
                        sandwich_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
                        butcher_marker.setBackground(null);
                        break;
                    case 2:
                        candy_marker.setBackground(null);
                        burger_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
                        drink_marker.setBackground(null);
                        sandwich_marker.setBackground(null);
                        butcher_marker.setBackground(null);
                        break;
                    case 3:
                        candy_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
                        burger_marker.setBackground(null);
                        drink_marker.setBackground(null);
                        sandwich_marker.setBackground(null);
                        butcher_marker.setBackground(null);
                        break;
                    case 4:
                        candy_marker.setBackground(null);
                        burger_marker.setBackground(null);
                        drink_marker.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_borders_green));
                        sandwich_marker.setBackground(null);
                        butcher_marker.setBackground(null);
                        break;
                }

                //SCHEDULE info
                profile_monday.setText(userSchedule.getString("monday"));
                profile_tuesday.setText(userSchedule.getString("tuesday"));
                profile_wednesday.setText(userSchedule.getString("wednesday"));
                profile_thursday.setText(userSchedule.getString("thursday"));
                profile_friday.setText(userSchedule.getString("friday"));
                profile_saturday.setText(userSchedule.getString("saturday"));
                profile_sunday.setText(userSchedule.getString("sunday"));


            } catch (JSONException e) {
                e.printStackTrace();
            }


            super.onPostExecute(aVoid);
        }
    }

    class fetchBackgroundPhoto extends AsyncTask<Void, Void, Void> {

        private String username;
        private Bitmap profile_pic;
        private ProgressDialog progressDialog;

        public fetchBackgroundPhoto(String username) {
            this.username = username;
        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return CheckingUtils.scaleBitmap(myBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                //Connect to mysql.
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://64.137.182.232/ext_profile_background.php");


                //JSON object.
                JSONObject jsonObject = new JSONObject();
                jsonObject.putOpt("username", username);


                EntityBuilder entity = EntityBuilder.create();
                entity.setText(jsonObject.toString());
                httpPost.setEntity(entity.build());

                //Getting response
                HttpResponse response = httpClient.execute(httpPost);
                String responseBody = EntityUtils.toString(response.getEntity());

                jsonArrayExtBackground = new JSONArray(responseBody);

                jsonObjectExtBackground = jsonArrayExtBackground.getJSONObject(0);


                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DataPrefs", Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);

                String link_background_photo = "http://64.137.182.232/pictures/" + username + "." + jsonObjectExtBackground.getString("ext_profile");
                Log.i("TEST", link_background_photo);


                profile_pic = getBitmapFromURL(link_background_photo);


            } catch (Exception e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            profile_background_photo.setImageBitmap(profile_pic);

            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }
    }

}