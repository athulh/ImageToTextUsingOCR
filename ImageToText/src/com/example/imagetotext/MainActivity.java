package com.example.imagetotext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends Activity {

	private ImageView imageView;
	private TextView textView;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int PICK_IMAGE_FROM_GALLERY_REQUEST_CODE = 200;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
    		if(resultCode == Activity.RESULT_OK){
    			Bitmap bitmap = data.getParcelableExtra("data");
    			Bitmap bitmapARGB = ConvertToARGB(bitmap);
    			Log.e("Camera Height & Width", bitmap.getHeight()+" "+bitmap.getWidth());
    			Log.e("CameraARGB Height & Width", bitmapARGB.getHeight()+" "+bitmapARGB.getWidth());
    			imageView.setImageBitmap(bitmapARGB);
    		    performOCR(bitmapARGB);
    		}
    	}
    	else if (requestCode == PICK_IMAGE_FROM_GALLERY_REQUEST_CODE) {
    		if(resultCode == Activity.RESULT_OK){
    			Uri uri = data.getData();
    			String[] filePathColumn = { MediaStore.Images.Media.DATA };
    			
    	        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
    	        cursor.moveToFirst();
    	 
    	        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
    	        String picturePath = cursor.getString(columnIndex);
    	        cursor.close();
    	        
    	        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
    	        Bitmap bitmapARGB = ConvertToARGB(bitmap);
    	        Log.e("Gallery Height & Width", bitmap.getHeight()+" "+bitmap.getWidth());
    	        Log.e("GalleryARGB Height & Width", bitmapARGB.getHeight()+" "+bitmapARGB.getWidth());
    	        imageView.setImageBitmap(bitmapARGB);
    		    performOCR(bitmapARGB);
    		}
		}
    }
    
    public void onButtonClick(View view) {
    	int id = view.getId();
    	Intent intent;
    	
    	switch (id) {
		case R.id.btn_camera:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		case R.id.btn_gallery:
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY_REQUEST_CODE);
			break;
		}
	}
    
    private void copyFromAssetsToSDCard(File file) {
		try {
			file.createNewFile();   // create new file if it is not in existence
			InputStream is = getAssets().open("eng.traineddata");
			OutputStream write = new FileOutputStream(file);
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				write.write(buffer, 0, length);
			}
			is.close();
			write.close();
			Log.d(getClass().getSimpleName(), "File does not exists & Newly created");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    private Bitmap ConvertToARGB(Bitmap bitmap) {
    	 int width = bitmap.getWidth();
         int height = bitmap.getHeight();
         
      // Rotating Bitmap & convert to ARGB_8888, required by tess
         Bitmap bitmapARGB = Bitmap.createBitmap(bitmap, 0, 0, width, height, null, false);
         return bitmapARGB = bitmapARGB.copy(Bitmap.Config.ARGB_8888, true);
    }
    
    private void performOCR(Bitmap bitmap) {
    	TessBaseAPI baseApi = new TessBaseAPI();
    	// DATA_PATH = Path to the storage
    	// lang for which the language data exists, usually "eng"
    	// Also add eng.traineddata file in sdcard/tessdata/...
    	
    	File file = new File(Environment.getExternalStorageDirectory()+File.separator+"tessdata");
    	if(!file.exists()){
    		file.mkdir();
    		
    		File createLangFile = new File(file.getAbsolutePath()+File.separator+"eng.traineddata"); 
    		copyFromAssetsToSDCard(createLangFile);
    	}
    	
    	baseApi.init(Environment.getExternalStorageDirectory().toString(), "eng"); baseApi.setImage(bitmap);
    	String recognizedText = baseApi.getUTF8Text();
    	System.out.println(recognizedText);
    	textView.setText(recognizedText);
    	baseApi.end();
    }
}
