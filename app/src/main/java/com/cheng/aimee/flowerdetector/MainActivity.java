package com.cheng.aimee.flowerdetector;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.aimee.flowerdetector.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.io.File;


public class MainActivity extends ActionBarActivity {
    TextView flowerName;
    private File imageFile;
    Uri imgUri;
    ProgressDialog dialog = null;
    int serverResponseCode = 0;
    String imagePath;
    Button cameraButton;
    Button fileButton;
    Button uploadButton;
    Button confirmButton;
    Button cancelButton;
    String ba1;
    String responseStr;
    Boolean responseFlag;
    Boolean cameraFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraButton = (Button)findViewById(R.id.camera);
        fileButton = (Button)findViewById(R.id.file);
        uploadButton = (Button)findViewById(R.id.uploadImage);
        uploadButton.setEnabled(false);
        confirmButton = (Button)findViewById(R.id.confirm);
        confirmButton.setVisibility(View.GONE);
        cancelButton = (Button)findViewById(R.id.cancel);
        cancelButton.setVisibility(View.GONE);
        flowerName= (TextView)findViewById(R.id.textView);
        responseFlag = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class UploadConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            doPostRequest();
            return null;
        }
    }

    private class ConfirmConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            doGetRequest();
            return null;
        }
    }

    private void doPostRequest(){
        String urlString = "https://flower-aimeechengdev.c9.io/flowerPhone";
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urlString);
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("image",ba1));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            responseStr = EntityUtils.toString(response.getEntity());
            responseFlag = true;
        }
        catch (Exception ex){
            Log.e("Debug", "error1: " + ex.getMessage(), ex);
        }
    }

    private void doGetRequest(){
        String urlString = "https://flower-aimeechengdev.c9.io/confirm";
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(urlString);
            HttpResponse response = client.execute(get);
        }
        catch (Exception ex){
            Log.e("Debug", "error1: " + ex.getMessage(), ex);
        }
    }
    public void openCamera(View view){
        cameraFlag = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "flower.jpg");
        imgUri = Uri.fromFile(imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImageUri;
        if(requestCode==0) {
            selectedImageUri = imgUri;
        }else {
            selectedImageUri = data.getData();
        }
        imagePath = selectedImageUri.getPath();
        getContentResolver().notifyChange(selectedImageUri, null);
        ImageView imageView = (ImageView) findViewById(R.id.photo);
        ContentResolver cr = getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImageUri);
            imageView.setImageBitmap(bitmap);
            uploadButton.setEnabled(true);
            Toast.makeText(this, "Image ready to upload", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e("Debug", "error1: " + ex.getMessage(), ex);
        }
    }

    public void openFile(View view){
        cameraFlag = false;
        Toast.makeText(this, "openFile", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), 1);
    }

    public void uploadImage(View view){
        Toast.makeText(this, "uploadImage", Toast.LENGTH_SHORT).show();
//        dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);
//        dialog.setCancelable(true);
//        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
//        layout.invalidate();
        Uri selectedImageUri;
        if(cameraFlag){
            selectedImageUri = Uri.parse("file://"+imagePath);
        }else{
            selectedImageUri = Uri.parse(imagePath);
        }
        getContentResolver().notifyChange(selectedImageUri, null);
        ContentResolver cr = getContentResolver();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(cr, selectedImageUri);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
            byte[] ba = bao.toByteArray();
            int arrayLength = ba.length;
            ba1 = Base64.encodeToString(ba, 0, arrayLength, Base64.DEFAULT);
        }catch(Exception ex){
            Log.e("Debug", "error1: " + ex.getMessage(), ex);
        }

        new UploadConnection().execute();
        for (int i = 0; i < 1000; i++) {
            android.os.SystemClock.sleep(1000);
            if(responseFlag){
                flowerName.setText("This might be " + responseStr + ". If you are sure it is " + responseStr + ". Please press CONFIRM button, otherwise press CANCEL button.");
                break;
            }
        }
        //    dialog.dismiss();
        cameraButton.setVisibility(View.GONE);
        fileButton.setVisibility(View.GONE);
        uploadButton.setVisibility(View.GONE);
        confirmButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        Toast.makeText(this, "uploadImage finished", Toast.LENGTH_SHORT).show();
    }
    public void confirm(View view){
        new ConfirmConnection().execute();
        flowerName.setText("Please use your camera to capture a flower or find a photo from your library. And then upload the photo to get result back.");
        cameraButton.setVisibility(View.VISIBLE);
        fileButton.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

    public void cancel(View view){
        flowerName.setText("Please use your camera to capture a flower or find a photo from your library. And then upload the photo to get result back.");
        cameraButton.setVisibility(View.VISIBLE);
        fileButton.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }
}
