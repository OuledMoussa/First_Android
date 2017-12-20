package com.example.android.androboom_hamzouz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

public class UserActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 42;
    private static final int SELECT_PICTURE = 124;
    private Profil user = new Profil();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Button bouton = (Button) findViewById(R.id.button2);
        bouton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                lancerListUser();
            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            Log.v("AndroBoum_Hamzouz", "je suis déjà connecté sous l'email :"
                    +auth.getCurrentUser());
            TextView textView = (TextView) findViewById(R.id.email);
            textView.setText( auth.getCurrentUser().getEmail());
            downloadImage();
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
    }

    public void lancerListUser() {
        Intent intent = new Intent(this, UserListActivity.class);
        startActivity(intent);
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
                downloadImage();
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
                    uploadImage();
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


    private StorageReference getCloudStorageReference() {
// on va chercher l'email de l'utilisateur connecté
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth ==null)
            return null;
        String email = auth.getCurrentUser().getEmail();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
// on crée l'objet dans le sous-dossier de nom l'email
        StorageReference photoRef = storageRef.child(email +"/photo.jpg");
        return photoRef;
    }

    private void downloadImage() {
        StorageReference photoRef = getCloudStorageReference();
        if(photoRef ==null) return;
        ImageView imageView = (ImageView) findViewById(R.id.imageUser);
// Load the image using Glide
        Glide.with(this /* context */).using(new FirebaseImageLoader())
                .load(photoRef)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(imageView);
    }

    private void uploadImage() {
        StorageReference photoRef = getCloudStorageReference();
        if(photoRef == null)return;
// on va chercher les données binaires de l'image de profil
        ImageView imageView = (ImageView) findViewById(R.id.imageUser);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte[] data = baos.toByteArray();
// on lance l'upload
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(
            new OnFailureListener() {
                @Override
                public void onFailure( @NonNull Exception exception) {
// si on est là, échec de l'upload
                }
                }).addOnSuccessListener(new
                OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void
                    onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
    // ok, l'image est uploadée
    // on fait pop un toast d'information
                        Toast toast = Toast.makeText(getApplicationContext(),
                                getString(R.string.imageUploaded),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
    }



    private void setUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser fuser = auth.getCurrentUser();
        if(fuser != null) {
            user.setUid(fuser.getUid());
            user.setEmail(fuser.getEmail());
            user.setConnected(true);
        }
    }

    private void
    updateProfil(Profil user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child("Users").child(user.getUid());
        ref.child("connected").setValue(true);
        ref.child("email").setValue(user.getEmail());
        ref.child("uid").setValue(user.getUid());
    }

    @Override
    protected void onDestroy() {
        user.setConnected(false);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth !=null) {
            FirebaseUser fuser = auth.getCurrentUser();
            if(fuser !=null) {
                final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                DatabaseReference mreference =mDatabase.getReference().child("Users").child(fuser.getUid());
                mreference.child("connected").setValue(false);
            }
        }
        super.onDestroy();
    }
}

