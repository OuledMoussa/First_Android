package com.example.android.androboom_hamzouz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

public class UserActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 42;
    private static final int SELECT_PICTURE = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            Log.v("AndroBoum_Hamzouz", "je suis déjà connecté sous l'email :"
                    +auth.getCurrentUser());
            TextView textView = (TextView) findViewById(R.id.email);
            textView.setText( auth.getCurrentUser().getEmail());
        } else{
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()/*,
                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()*/
            )).build(), 42);
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageUser);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setAction(Intent.ACTION_PICK);
                Intent chooserIntent = Intent.createChooser(intent, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[] { captureIntent });
                startActivityForResult(chooserIntent, SELECT_PICTURE);
                return false;
                }
            }
        );
        /*TextView textView = (TextView) findViewById(R.id.email);
        textView.setText( auth.getCurrentUser().toString());*/
    }

    protected  void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(requestCode == RC_SIGN_IN) {
            IdpResponse rep = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                Log.v("AndroBoum", "je me suis connecte et mon email est : "+
                rep.getEmail());
                TextView textView = (TextView) findViewById(R.id.email);
                textView.setText( auth.getCurrentUser().getEmail());
                return;
            } else{
                if (rep == null){
                    Log.v("Androboum", "back button On");
                    finish();
                    return;
                }
                if (rep.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.v("Androboum", "Erreur reseau");
                    finish();
                    return;
                }
                if (rep.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.v("Androboum", "Erreur inconnue");
                    finish();
                    return;
                }
            }
            Log.v("AndroBoum", "Réponse inconnue");
        }

        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                try {
                    ImageView imageView = (ImageView) findViewById(R.id.imageUser);
                    boolean isCamera = (data.getData() == null);
                    final Bitmap selectedImage;
                    if (!isCamera) {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                    }
                    else {
                        selectedImage = (Bitmap) data.getExtras().get("data");
                    }
                    Bitmap finalBitmap = Bitmap.createScaledBitmap(selectedImage, 500,
                            (selectedImage.getHeight()*500) / selectedImage.getWidth(), false);
                    imageView.setImageBitmap(finalBitmap);
                }
                catch (Exception e) {
                    Log.v("AndroBoum", e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                //AndroBoumApp.setIsConnected(false);
                AuthUI.getInstance().signOut(this);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }
}
