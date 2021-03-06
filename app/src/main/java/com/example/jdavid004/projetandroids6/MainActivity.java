package com.example.jdavid004.projetandroids6;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import yuku.ambilwarna.AmbilWarnaDialog;




public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    /* color picker variables */
    private int mDefaultColor = 0;
    private int colorPickerOption = 0;

    private ZoomageView imageViewCentral;      //View of the image of type ZoomageView which extends type ImageView
    private Preview[] tablePreview;         //Table of the différent Imageview used to the different previews
    private Picture originalPictureUse; //The original picture we are using
    private Picture currentPictureUse;  //The current picture we are using
    private Picture copycurrentPictureUse;  //A copy of the current picture we are using
    private SeekBar seekbarlum;         //cursor bar to modify luminosity
    private TextView textLumi;          //Text indication for luminosity
    private SeekBar seekBarContrast;    //cursor bar to modify contrast
    private SeekBar seekbarBlur;         //cursor bar to modify Blur
    private String currentPhotoPath;
    private int previewId;
    private static final int GALLERY_REQUEST = 1314;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private int STORAGE_PERMISSION_CODE = 1;
    private static final int NB_PICTURE_PREVIEW = 7;

    /* seekbar macro */
    private int SEEKBAR_OPTION_NULL = 0;
    private int SEEKBAR_OPTION_LUMINOSITY = 1;
    private int SEEKBAR_OPTION_CONTRAST_DYN = 2; //contrastDynamicExten
    private int SEEKBAR_OPTION_BLUR = 3;
    private int seekBarOption = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewCentral = findViewById(R.id.vegetables);
        originalPictureUse = new Picture(getResources());
        currentPictureUse = new Picture(originalPictureUse.getBmp());
        imageViewCentral.setImageBitmap(currentPictureUse.getBmp());

        seekbarlum = (SeekBar)findViewById(R.id.seekbarlum);
        seekbarlum.setVisibility(View.GONE);
        seekbarlum.setOnSeekBarChangeListener(this);
        seekbarlum.setMax(200);

        textLumi = (TextView) findViewById(R.id.textLumi);
        textLumi.setVisibility(View.GONE);

        seekBarContrast = (SeekBar)findViewById(R.id.seekBarContrast);
        seekBarContrast.setVisibility(View.GONE);
        seekBarContrast.setOnSeekBarChangeListener(this);
        seekBarContrast.setMax(200);

        seekbarBlur = (SeekBar)findViewById(R.id.seekbarBlur);
        seekbarBlur.setVisibility(View.GONE);
        seekbarBlur.setOnSeekBarChangeListener(this);
        seekbarBlur.setMax(15);

        mDefaultColor = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);

        initialisePreview();
    }

    @Override
    /**
     * Set up the menu to choose the different image processing options.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    /**
     * Organise the different menu use by the application. It's a link between the method use for a picture and the  xml file : example_menu.xml
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        seekbarlum.setVisibility(View.GONE);
        textLumi.setVisibility(View.GONE);

        seekBarContrast.setVisibility(View.GONE);

        seekbarBlur.setVisibility(View.GONE);

        seekBarOption = SEEKBAR_OPTION_NULL; // seekBarOption = 0

        switch(item.getItemId()){
            // click on the camera icon to access the camera of the phone
            case R.id.camera:
                dispatchTakePictureIntent(); // Take a photo with a camera app
                return true;

            case R.id.save:
                //check if the permission to write in the storage is already granted
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }
                Save saveFile = new Save();
                saveFile.saveImage(this,currentPictureUse.getBmp());
                return true;


            // click on the arrow to cancel effects
            case R.id.reset:
                currentPictureUse = new Picture(originalPictureUse.getBmp());
                imageViewCentral.setImageBitmap(currentPictureUse.getBmp()); // On oublie pas de réafficher l'image
                return true;

            case R.id.toGrey:
                //currentPictureUse.toGrey();
                currentPictureUse.toGreyRS(getApplicationContext());
                return true;

            case R.id.colorize:
                colorPickerOption = 1;
                openColorPicker();
                return true;

            case R.id.colorOnly:
                colorPickerOption = 2;
                openColorPicker();
                return true;

            case R.id.sepia:
                //currentPictureUse.sepia();
                currentPictureUse.sepiaRS(getApplicationContext());
                return true;

            case R.id.thresholding:
                //currentPictureUse.thresholding();
                currentPictureUse.thresholdingRS(getApplicationContext());
                return true;

            case R.id.invert:
                //currentPictureUse.invert();
                currentPictureUse.invertRS(getApplicationContext());
                return true;

            case R.id.contrastDynamicExten:
                seekBarOption = SEEKBAR_OPTION_CONTRAST_DYN;
                seekBarContrast.setProgress(100);
                seekBarContrast.setVisibility(View.VISIBLE);
                copycurrentPictureUse = new Picture(currentPictureUse.getBmp());


                return true;

            case R.id.contrastEqualHisto:
                currentPictureUse.contrastHistogramEqualizationYuvRS(getApplicationContext());
                return true;

            case R.id.moyenneur:
                int mWidthMoy = 3;
                int mHeightMoy = mWidthMoy;
                int[][] matrixMoy = new int[mWidthMoy][mHeightMoy];
                for(int i = 0; i < mWidthMoy; i++){
                    for(int j = 0; j < mHeightMoy; j++){
                        matrixMoy[i][j]=1;
                    }
                }
                currentPictureUse.modifyConvolutionAttributes(matrixMoy, mWidthMoy, mHeightMoy, false, true);
                //currentPictureUse.compute();
                //currentPictureUse.computeRS(getApplicationContext());
                currentPictureUse.computeIntrinsicConvolve(getApplicationContext());
                return true;

            case R.id.gaussien:
                /*int mWidthGauss = 3;
                int mHeightGauss = mWidthGauss;
                int [][] matrixGauss = { {1,2,1},
                                         {2,4,2},
                                         {1,2,1} };
                currentPictureUse.modifyConvolutionAttributes(matrixGauss, mWidthGauss, mHeightGauss, false, true);
                //currentPictureUse.compute();
                //currentPictureUse.computeRS(getApplicationContext());
                currentPictureUse.computeIntrinsicGaussianBlur(getApplicationContext(), 3, null);*/

                seekBarOption = SEEKBAR_OPTION_BLUR;
                seekbarBlur.setProgress(0);
                seekbarBlur.setVisibility(View.VISIBLE);
                copycurrentPictureUse = new Picture(currentPictureUse.getBmp());
                return true;

            case R.id.prewitt:
                int mWidthPrewitt = 3;
                int mHeightPrewitt = mWidthPrewitt;
                int[][] matrixPrewitt = { {-1,0,1},
                                          {-1,0,1},
                                          {-1,0,1} };
                currentPictureUse.modifyConvolutionAttributes(matrixPrewitt, mWidthPrewitt, mHeightPrewitt,true, false);
                //currentPictureUse.compute();
                //currentPictureUse.computeRS(getApplicationContext());
                currentPictureUse.computeIntrinsicConvolve(getApplicationContext());
                return true;

            case R.id.sobel:
                int mWidthSobel = 3;
                int mHeightSobel = mWidthSobel;
                int[][] matrixSobel = { {-1,0,1},
                                        {-2,0,2},
                                        {-1,0,1} };
                currentPictureUse.modifyConvolutionAttributes(matrixSobel, mWidthSobel, mHeightSobel,true, false);
                //currentPictureUse.compute();
                //currentPictureUse.computeRS(getApplicationContext());
                currentPictureUse.computeIntrinsicConvolve(getApplicationContext());
                return true;

            case R.id.laplacien:
                int mWidthLaplacien = 3;
                int mHeightLaplacien = mWidthLaplacien;
                int [][] matrixLaplacien = { {0,1,0},
                                             {1,-4,1},
                                             {0,1,0} };
                currentPictureUse.modifyConvolutionAttributes(matrixLaplacien, mWidthLaplacien, mHeightLaplacien, false, false);
                //currentPictureUse.compute();
                //currentPictureUse.computeRS(getApplicationContext());
                currentPictureUse.computeIntrinsicConvolve(getApplicationContext());
                return true;

            case R.id.luminosity:
                seekBarOption = SEEKBAR_OPTION_LUMINOSITY;
                seekbarlum.setProgress(100);
                seekbarlum.setVisibility(View.VISIBLE);
                textLumi.setVisibility(View.VISIBLE);
                copycurrentPictureUse = new Picture(currentPictureUse.getBmp());
                return true;

            //click on the import icon to access to the gallery
            case R.id.importFromGallery:
                getImageFromGallery();
                return true;

            case R.id.pixelisation:
                currentPictureUse.pixelisation();
                return true;

            case R.id.median:
                currentPictureUse.median();
                return true;

            case R.id.drawing:
                currentPictureUse.drawing(getApplicationContext());
                return true;

        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    /**
     * Treats the result of the activity depending on the request
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQUEST_TAKE_PHOTO: { //to take a photo using the camera
                if(resultCode == RESULT_OK){
                    File file = new File(currentPhotoPath);
                    Bitmap imageBitmap = null;
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),Uri.fromFile(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(imageBitmap != null){
                        originalPictureUse = new Picture(imageBitmap);
                        currentPictureUse = new Picture(imageBitmap);
                        imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                        initialisePreview();
                    }
                }
                break;
            }
            case(GALLERY_REQUEST): { //to import a photo from the gallery
                if(resultCode == Activity.RESULT_OK){
                    onSelectFromGalleryResult(data);
                }
            }
        }
    }

    /**
     * Create an image file
     * @return the file
     * @throws IOException
     */
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    /**
     * take a photo and save it
     * @return the name of photo created
     */

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
                currentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                ex.printStackTrace();// Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.jdavid004.projetandroids6.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.i("Photo", "Photo prise et sauvegardé dans un fichier");
            }
        }
    }

    /**
     * create the intent and launch the activity
     */

    protected void getImageFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent,GALLERY_REQUEST);
    }

    /**
     * load an image from the gallery
     * @param data
     */

    public void onSelectFromGalleryResult(Intent data){
        Bitmap bmp = currentPictureUse.getBmp();
        if(data != null){
            try{
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
            }catch (IOException e ){
                Toast.makeText(this, "Failed to access to gallery", Toast.LENGTH_SHORT).show();
            }
            currentPictureUse = new Picture(bmp);
            originalPictureUse = new Picture(bmp);
            imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
        }
        initialisePreview();
    }

    /**
     * requestStoragePermission : if the permission to access to the gallery is not already granted, asks permission
     */
    public void requestStoragePermission(){
        //if the permission has already been asked and denied, create a dialog to explain why we need the permission
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)  //creates a dialog with the user
                    .setTitle("Permission needed")
                    .setMessage("Permission to access to the storage is needed to save the image")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        //if the user clicks on "OK", asks permission again
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        //if the user clicks on "CANCEL", shut the dialog
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }else{
            //asks permission to write in the storage
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    /**
     * Callback for the result from requesting permissions
     * @param requestCode : code of the permission
     * @param permissions : requested permissions
     * @param grantResults : grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){ //if the permission requested is to access to the storage
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //if the permission is granted
                Toast.makeText(this, "Permission granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openColorPicker(){
        final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            public void onCancel(AmbilWarnaDialog dialog) {

            }
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = color;
                if(colorPickerOption == 1){
                    currentPictureUse.colorizeRS(getApplicationContext(), mDefaultColor);
                }
                if(colorPickerOption == 2){
                    currentPictureUse.colorOnlyHsvRS(getApplicationContext(), mDefaultColor);
                }
            }
        });
        colorPicker.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBarOption == SEEKBAR_OPTION_LUMINOSITY){
            //currentPictureUse.AdjustLuminosity(seekBar.getProgress(),copycurrentPictureUse);
            currentPictureUse.adjustLuminosityRS(getApplicationContext(),seekBar.getProgress()+25,copycurrentPictureUse);
        }

        if(seekBarOption == SEEKBAR_OPTION_CONTRAST_DYN){
            currentPictureUse.contrastDynamicExtensionRGBAverage(seekBar.getProgress()+10, copycurrentPictureUse);
            //currentPictureUse.contrastDynamicExtensionRS(getApplicationContext());
        }

        if(seekBarOption == SEEKBAR_OPTION_BLUR){
            int seekbarValue = seekBar.getProgress()+3;
            if(seekbarValue % 2 == 0){
                seekbarValue++;
            }
            currentPictureUse.computeIntrinsicGaussianBlur(getApplicationContext(), seekbarValue, copycurrentPictureUse);
        }
    }

    /**
     * Initialize the different previews at the bottom of the application.
     */
    private void initialisePreview() {
        this.tablePreview = new Preview[NB_PICTURE_PREVIEW]; // We initialize the array witch stock the differents ImageView for the preview

        Bitmap original = originalPictureUse.getBmp(); // We resize the bitmap in a small bitmap because the traitment need to be fast
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.compress(Bitmap.CompressFormat.JPEG, 0, out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        for (int i = 0; i < NB_PICTURE_PREVIEW; i++) {
            Picture picturePreview = new Picture(decoded);
            final String namePreview = "preview" + i;
            this.previewId = getResources().getIdentifier(namePreview, "id", getPackageName());
            ImageView view = findViewById(previewId);
            switch (i) {
                case 0: // first preview use the function toGreyRS
                    picturePreview.toGreyRS(getApplicationContext());
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.TOGREY);
                    break;
                case 1: // second preview use the function sepiaRS
                    picturePreview.sepiaRS(getApplicationContext());
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.SEPIA);
                    break;
                case 2: //third preview use the function pixelisation
                    picturePreview.pixelisation();
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.PIXELISATION);
                    break;
                case 3: // fourth preview use the convolution with a average filter
                    int mWidthMoy = 3;
                    int mHeightMoy = mWidthMoy;
                    int[][] matrixMoy = new int[mWidthMoy][mHeightMoy];
                    for (int k = 0; k < mWidthMoy; k++) {
                        for (int j = 0; j < mHeightMoy; j++) {
                            matrixMoy[k][j] = 1;
                        }
                    }
                    picturePreview.modifyConvolutionAttributes(matrixMoy, mWidthMoy, mHeightMoy, false, true);
                    picturePreview.computeIntrinsicConvolve(getApplicationContext());
                    picturePreview.computeIntrinsicConvolve(getApplicationContext());
                    picturePreview.computeIntrinsicConvolve(getApplicationContext());
                    picturePreview.computeIntrinsicConvolve(getApplicationContext());
                    picturePreview.computeIntrinsicConvolve(getApplicationContext());
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.BLUR);
                    break;
                case 4: // fifth preview use the thresholding function.
                    picturePreview.thresholdingRS(getApplicationContext());
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.THRESHOLDING);
                    break;

                case 5: // Sixth preview use the median function
                    picturePreview.colorOnlyHsvRS(getApplicationContext(),Color.rgb(255,0,0));
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.COLORONLY);
                    break;

                case 6:
                    picturePreview.invertRS(getApplicationContext());
                    view.setImageBitmap(picturePreview.getBmp());
                    tablePreview[i] = new Preview(view,Treatment.NEGATIF);
                    break;
            }
            this.tablePreview[i].getImagePreview().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int j = 0; j < NB_PICTURE_PREVIEW ; j++){
                        if(view == tablePreview[j].getImagePreview()){
                            switch (tablePreview[j].getTreatmentUse()){
                                case TOGREY:
                                    currentPictureUse.toGreyRS(getApplicationContext());
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;
                                case SEPIA:
                                    currentPictureUse.sepiaRS(getApplicationContext());
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;
                                case PIXELISATION:
                                    currentPictureUse.pixelisation();
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;
                                case THRESHOLDING:
                                    currentPictureUse.thresholdingRS(getApplicationContext());
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;
                                case BLUR:
                                    int mWidthMoy = 5;
                                    int mHeightMoy = mWidthMoy;
                                    int[][] matrixMoy = new int[mWidthMoy][mHeightMoy];
                                    for (int k = 0; k < mWidthMoy; k++) {
                                        for (int l = 0; l < mHeightMoy; l++) {
                                            matrixMoy[k][l] = 1;
                                        }
                                    }
                                    currentPictureUse.modifyConvolutionAttributes(matrixMoy, mWidthMoy, mHeightMoy, false, true);
                                    currentPictureUse.computeIntrinsicConvolve(getApplicationContext());
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;
                                case COLORONLY:
                                    currentPictureUse.colorOnlyHsvRS(getApplicationContext(), Color.rgb(255,0,0));
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;

                                case NEGATIF:
                                    currentPictureUse.invertRS(getApplicationContext());
                                    imageViewCentral.setImageBitmap(currentPictureUse.getBmp());
                                    break;
                            }
                        }
                    }
                    Log.i("TOUCHEVENT", " On a touché : " + namePreview);
                }
            });
        }
    }

}