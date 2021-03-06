package com.example.jdavid004.projetandroids6;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;



/**
 * class Save : contains all the necessary methods to save an image
 * @author Benjamin Darmet, Amandine Chauveau, Remi Barbosa, Johan David
 */
public class Save {
    private Context theThis;    //gets the current context
    private String nameOfFolder = "ApplicationImage";   //name of the folder that will contain all the pictures saved by the application
    private String nameOfFile = "MyImage";  //prefix name common to all the pictures

    /**
     * saveImage : main method, saves the image given by imageToSave in the gallery
     * @param context : current context of the application
     * @param imageToSave : bitmap of the image to save
     */
    public void saveImage(Context context, Bitmap imageToSave ){
        theThis = context;

        String currentDateAndTime = getCurrentDateAndTime();
        String pictureName = nameOfFile+currentDateAndTime+".png"; //complete name of the picture to save

        try {
            //root : file representing the path in where to save the image
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + nameOfFolder + File.separator);
            root.mkdirs(); //creates the folder if necessary
            File imageFile = new File(root, pictureName); //file representing the image
            OutputStream fOut = new FileOutputStream(imageFile); //file output stream in where to write the bitmap
            imageToSave.compress(Bitmap.CompressFormat.PNG, 100, fOut); //compress the bitmap and writes it into the output stream
            fOut.flush();  //empty the buffer
            fOut.close();  //close the file output stream
            fileCreatedAndAvailable(imageFile);
            ableToSave();

        } catch (Exception e) {
            e.printStackTrace();
            unableToSave();
        }
    }

    /**
     * getCurrentDateAndTime : gets the current date and time and converts it into a String
     * @return String : formattedDate
     */
    private String getCurrentDateAndTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return df.format(c.getTime()); //formatted date
    }

    /**
     * fileCreatedAndAvailable : makes sure the file has been created and makes it available for all the applications
     * @param file : file to test
     */
    private void fileCreatedAndAvailable(File file){
        MediaScannerConnection.scanFile(theThis, new String[]{file.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.e("ExternalStorage","Scanned" + path + ":");
                Log.e("ExternalStorage", "->uri="+uri);
            }
        });
    }

    /**
     * unableToSave : displays an error message
     */
    private void unableToSave() {
        Toast.makeText(theThis, "Failed to access to gallery", Toast.LENGTH_SHORT).show();
    }

    /**
     * ableToSave : displays a message to confirm that the picture has been saved
     */
    private void ableToSave() {
        Toast.makeText(theThis, "Picture saved in gallery", Toast.LENGTH_SHORT).show();
    }
}
