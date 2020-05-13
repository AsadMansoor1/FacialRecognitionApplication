package com.asad.smartattendanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.ACCURATE;


public class FaceRecognitionActivity extends AppCompatActivity {

    private static final String TAG = "ROTATION";
    private CameraKitView cameraKitView;
    private Button cameraButton;
    private GraphicOverlay graphicOverlay;
    Map<String, List<double[]>> contours;
    List<double[]> myContour;
    private String filename = "SampleFile.txt";
    private String filepath = "MyFileStorage";
    private String dataToStoreInFile = "";
    File myExternalFile;

    private String[] allFacialContour = {"allPoints", "face", "leftEye", "leftEyebrowBottom", "leftEyebrowTop", "lowerLipBottom", "lowerLipTop",
            "noseBottom", "noseBridge", "rightEye", "rightEyebrowBottom", "rightEyebrowTop", "upperLipBottom", "upperLipTop"};
    private String[] currentDetectedArr = new String[14];
    private float[] allConfidences = new float[14];
    private String[] detectedContours = {"209.0,201.0222.0,200.0245.0,200.0263.0,204.0276.0,213.0288.0,223.0294.0,236.0299.0,250.0301.0,266.0302.0,282.0302.0,298.0298.0,315.0291.0,330.0282.0,340.0272.0,350.0263.0,357.0253.0,363.0241.0,367.0232.0,368.0223.0,368.0213.0,365.0205.0,360.0197.0,355.0188.0,346.0180.0,338.0172.0,326.0165.0,310.0161.0,295.0158.0,279.0157.0,264.0155.0,250.0156.0,237.0160.0,225.0167.0,215.0178.0,208.0198.0,203.0161.0,247.0165.0,241.0174.0,238.0185.0,238.0198.0,238.0165.0,250.0169.0,246.0176.0,243.0187.0,243.0202.0,247.0276.0,235.0267.0,230.0256.0,229.0242.0,232.0227.0,235.0272.0,239.0264.0,236.0254.0,235.0242.0,237.0227.0,244.0174.0,261.0175.0,260.0177.0,259.0180.0,257.0185.0,256.0190.0,256.0195.0,257.0199.0,259.0201.0,260.0200.0,261.0197.0,262.0192.0,263.0187.0,264.0182.0,264.0179.0,263.0176.0,263.0237.0,257.0238.0,255.0242.0,252.0247.0,250.0252.0,249.0258.0,249.0262.0,250.0264.0,251.0266.0,252.0264.0,254.0261.0,255.0257.0,257.0251.0,258.0246.0,258.0241.0,257.0238.0,257.0198.0,322.0200.0,321.0203.0,320.0207.0,318.0214.0,316.0222.0,317.0231.0,315.0240.0,315.0247.0,316.0252.0,317.0255.0,318.0202.0,322.0209.0,322.0213.0,322.0218.0,322.0224.0,322.0231.0,321.0238.0,320.0243.0,319.0251.0,318.0247.0,319.0243.0,320.0238.0,321.0231.0,322.0224.0,323.0218.0,323.0213.0,323.0209.0,323.0206.0,322.0252.0,321.0247.0,324.0241.0,328.0234.0,331.0226.0,332.0218.0,332.0212.0,330.0206.0,327.0202.0,325.0215.0,253.0217.0,298.0204.0,302.0220.0,303.0239.0,298.0",
    "209.0,201.0222.0,200.0245.0,200.0263.0,204.0276.0,213.0288.0,223.0294.0,236.0299.0,250.0301.0,266.0302.0,282.0302.0,298.0298.0,315.0291.0,330.0282.0,340.0272.0,350.0263.0,357.0253.0,363.0241.0,367.0232.0,368.0223.0,368.0213.0,365.0205.0,360.0197.0,355.0188.0,346.0180.0,338.0172.0,326.0165.0,310.0161.0,295.0158.0,279.0157.0,264.0155.0,250.0156.0,237.0160.0,225.0167.0,215.0178.0,208.0198.0,203.0",
    "174.0,261.0175.0,260.0177.0,259.0180.0,257.0185.0,256.0190.0,256.0195.0,257.0199.0,259.0201.0,260.0200.0,261.0197.0,262.0192.0,263.0187.0,264.0182.0,264.0179.0,263.0176.0,263.0",
            "165.0,250.0169.0,246.0176.0,243.0187.0,243.0202.0,247.0",
            "161.0,247.0165.0,241.0174.0,238.0185.0,238.0198.0,238.0",
            "252.0,321.0247.0,324.0241.0,328.0234.0,331.0226.0,332.0218.0,332.0212.0,330.0206.0,327.0202.0,325.0",
            "247.0,319.0243.0,320.0238.0,321.0231.0,322.0224.0,323.0218.0,323.0213.0,323.0209.0,323.0206.0,322.0",
            "204.0,302.0220.0,303.0239.0,298.0",
            "215.0,253.0217.0,298.0",
            "237.0,257.0238.0,255.0242.0,252.0247.0,250.0252.0,249.0258.0,249.0262.0,250.0264.0,251.0266.0,252.0264.0,254.0261.0,255.0257.0,257.0251.0,258.0246.0,258.0241.0,257.0238.0,257.0",
            "272.0,239.0264.0,236.0254.0,235.0242.0,237.0227.0,244.0",
            "276.0,235.0267.0,230.0256.0,229.0242.0,232.0227.0,235.0",
            "202.0,322.0209.0,322.0213.0,322.0218.0,322.0224.0,322.0231.0,321.0238.0,320.0243.0,319.0251.0,318.0",
            "198.0,322.0200.0,321.0203.0,320.0207.0,318.0214.0,316.0222.0,317.0231.0,315.0240.0,315.0247.0,316.0252.0,317.0255.0,318.0"
    };

    ///////// Media Files Read Write.
    private static final int WRITE_REQUEST_CODE = 101;
    private static final int PICK_TEXT_FILE = 102;
    private String []arrInFile = new String[14];
    //////// Media Files Read Write.
    private static String allLines = "";
    private static String[] myData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        cameraKitView = findViewById(R.id.camera_view);
        cameraButton = findViewById(R.id.btn_detect);
        cameraButton.setText("Capture Image");
        graphicOverlay = findViewById(R.id.graphic_overlay);
        contours = new HashMap<>();

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                String models = String.join(",", supportedPhones);

                if (cameraButton.getText() == "Capture Image") {
                    cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                        @Override
                        public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                            cameraKitView.onStop();

                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (bitmap != null) {
                                bitmap = Bitmap.createScaledBitmap(bitmap, cameraKitView.getWidth(), cameraKitView.getHeight(), false);
                                runDetector(bitmap);
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Bitmap Contain nul value", Toast.LENGTH_SHORT);
                                toast.setMargin(50, 50);
                                toast.show();
                            }
                        }
                    });
                } else if (cameraButton.getText() == "Save Image") {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "Save image button", Toast.LENGTH_SHORT);
                    toast1.setMargin(50, 50);
                    toast1.show();
                    try {
                        getDataAndSave(contours);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast toast = Toast.makeText(getApplicationContext(), "myContour list stored to external storage", Toast.LENGTH_SHORT);
                    toast.setMargin(50, 50);
                    toast.show();
                    cameraButton.setText("Capture Image");
                    graphicOverlay.clear();
                    cameraKitView.onStart();
                }
            }
        });
    }

    private void runDetector(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions realTimeOpts = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(ACCURATE)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS).build();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(realTimeOpts);

        final Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        contours = getContourData(faces.get(0));
                                        processFaceResult(faces.get(0));
                                    }
                                })

                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                });
    }

    private void getDataAndSave(Map<String, List<double[]>> contours) throws IOException {
        int i=-1;
        allLines = "";
        for (String allContours : allFacialContour) {
            i++;
            String oneLine = "";
            myContour = contours.get(allContours);
            for (double[] resultContour : myContour) {
                oneLine = oneLine +  StringUtils.join(ArrayUtils.toObject(resultContour), ",");

            }
            currentDetectedArr[i] = oneLine;
            allLines = allLines + oneLine;
            allLines = allLines + System.getProperty("line.separator");
            }
        //filepath = getDirectoryType();
        //myExternalFile = new File(filepath, filename);
        //readFromFileAndRecognize(myExternalFile);
        readFile();
        //writeToFile(allLines);
        //createFile();
        /*for(int j=0; i<allConfidences.length; j++){
            Toast toast2 = Toast.makeText(getApplicationContext(), "Data Written to file", Toast.LENGTH_SHORT);
            toast2.setMargin(50, 50);
            toast2.show();
            String arr[] = StringUtils.join(ArrayUtils.toObject(a), ",");
            allConfidences[j] = comparisonConfidence(myData, allLines);
        }*/
//        float avgConfidence = calculateAverage(allConfidences);
//        if(avgConfidence >= 50){
//            Toast toast1 = Toast.makeText(getApplicationContext(), "Same Person", Toast.LENGTH_SHORT);
//            toast1.setMargin(50, 50);
//            toast1.show();
//        }else{
//            Toast toast1 = Toast.makeText(getApplicationContext(), "Person didn't matched", Toast.LENGTH_SHORT);
//            toast1.setMargin(50, 50);
//            toast1.show();
//        }
    }

    private Map<String, List<double[]>> getContourData(FirebaseVisionFace face) {
        Map<String, List<double[]>> contours = new HashMap<>();
        if (face != null) {
            contours.put("allPoints", contourPosition(face, FirebaseVisionFaceContour.ALL_POINTS));
            contours.put("face", contourPosition(face, FirebaseVisionFaceContour.FACE));
            contours.put("leftEye", contourPosition(face, FirebaseVisionFaceContour.LEFT_EYE));
            contours.put(
                    "leftEyebrowBottom", contourPosition(face, FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM));
            contours.put(
                    "leftEyebrowTop", contourPosition(face, FirebaseVisionFaceContour.LEFT_EYEBROW_TOP));
            contours.put(
                    "lowerLipBottom", contourPosition(face, FirebaseVisionFaceContour.LOWER_LIP_BOTTOM));
            contours.put("lowerLipTop", contourPosition(face, FirebaseVisionFaceContour.LOWER_LIP_TOP));
            contours.put("noseBottom", contourPosition(face, FirebaseVisionFaceContour.NOSE_BOTTOM));
            contours.put("noseBridge", contourPosition(face, FirebaseVisionFaceContour.NOSE_BRIDGE));
            contours.put("rightEye", contourPosition(face, FirebaseVisionFaceContour.RIGHT_EYE));
            contours.put("rightEyebrowBottom", contourPosition(face, FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM));
            contours.put(
                    "rightEyebrowTop", contourPosition(face, FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP));
            contours.put(
                    "upperLipBottom", contourPosition(face, FirebaseVisionFaceContour.UPPER_LIP_BOTTOM));
            contours.put("upperLipTop", contourPosition(face, FirebaseVisionFaceContour.UPPER_LIP_TOP));

            return contours;
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Detected Face is empty in getContourData Method", Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
            return contours;
        }
    }

    private List<double[]> contourPosition(FirebaseVisionFace face, int contourInt) {
        FirebaseVisionFaceContour contour = face.getContour(contourInt);
        if (contour != null) {
            List<FirebaseVisionPoint> contourPoints = contour.getPoints();
            List<double[]> result = new ArrayList<double[]>();

            for (int i = 0; i < contourPoints.size(); i++) {
                result.add(new double[]{contourPoints.get(i).getX(), contourPoints.get(i).getY()});
            }

            return result;
        }

        return null;
    }

    private void processFaceResult(FirebaseVisionFace face) {

            Rect bound = face.getBoundingBox();
            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, bound);
            graphicOverlay.add(rectOverlay);
        cameraButton.setText("Save Image");
    }

    protected boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    protected boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    protected String getDirectoryType() {
        if (Build.VERSION.SDK_INT >= 19) {
            return getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        } else {
            return Environment.getExternalStorageDirectory() + "/Documents";
        }
    }
    protected float readFromFileAndRecognize(File file){
        FileReader fr= null;   //reads the file
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream
        try{
            String line = "";
            int i =-1;
            while((line=br.readLine())!=null)
            {
                i++;
                String []arrInFile = line.split(",");
                String []arrInOnCameraDetected = currentDetectedArr[i].split(",");
                allConfidences[i] = comparisonConfidence(arrInFile, arrInOnCameraDetected);

//                double number []= new double[length];
//                // 2 > convert the String into  int  and save it in int array.
//                for(int i=0; i<stringArr.length;i++){
//                    number[i]=Double.parseDouble(stringArr[i]);
//                }
//                for(int i=0; i<stringArr.length;i++){
//                    number[i]=Double.parseDouble(stringArr[i]);
//                }
//                String data ="";
//                for(int i=0; i<number.length;i++) {
//                    data = data + Double.toString(number[i]) + ",";
//                }
                int a = arrInFile.length;
                int b = arrInOnCameraDetected.length;
                String strA = Integer.toString(a);
                String strB = Integer.toString(b);
                Toast toast5 = Toast.makeText(getApplicationContext(),"Length Of String A is "+strA +", and Length of String B is "+strB, Toast.LENGTH_LONG);
                toast5.setMargin(50, 50);
                toast5.show();
            }
            float avgConfidence = calculateAverage(allConfidences);
            return avgConfidence;
        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private float comparisonConfidence(String[] arr1, String[] arr2){
        float confidence = 0;
        int countEquals = 0;
        int totalContours = arr1.length;
        for(int i=0; i<arr1.length ; i++){
            if(arr1[i] == arr2[i])
                countEquals = countEquals + 1;
        }
        countEquals = countEquals * 100;
        confidence =countEquals/totalContours;
        return confidence;
    }
    private float calculateAverage(float arr[]){
        float sum = 0;
        for(int i=0; i< arr.length; i++){
            sum = sum + arr[i];
        }
        return sum/arr.length;
    }
    protected void writeToFile(String dataWithNewLine) {
        filepath = getDirectoryType();
        myExternalFile = new File(filepath, filename);
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            fr = new FileWriter(myExternalFile, true);
            br = new BufferedWriter(fr);
            br.write(dataWithNewLine);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected File getTargetFolder() {
        return getExternalFilesDir(getDirectoryType());
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    ///////////////MediaStore File Read & Write
    // read existing text file
    private void readFile() {
        // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, PICK_TEXT_FILE);
    }

    // create text file
    private void createFile() {
        // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // filter to only show openable items.
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested Mime type
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "SampleFile.txt");

        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null
                            && data.getData() != null) {
                        writeInFile(data.getData(),allLines);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }if (requestCode == PICK_TEXT_FILE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null
                            && data.getData() != null) {
                        myData = readFromFile(data.getData());
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private void writeInFile(@NonNull Uri uri, @NonNull String text) {
        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(text);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private String[] readFromFile(@NonNull Uri uri) {
        InputStream inputStream;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            int i =-1;
            String arr1[] ;
            String arr2[] ;
            while((line=br.readLine())!=null)
            {
                i++;
                arr1 = currentDetectedArr[i].split(",");
                arr2 = line.split(",");
                ///////// For the purpose of showing that how many values are comma separated in file that is got read
                arrInFile[i] = line;
                allConfidences[i] = comparisonConfidence(arr1, arr2);
                //float avg = calculateAverage(allConfidences);

                Toast toast8 = Toast.makeText(getApplicationContext(),"Confidence level of "+ i +"th Contour is "+allConfidences[i], Toast.LENGTH_SHORT);
                toast8.setMargin(50, 50);
                toast8.show();

                ///////// For the purpose of showing that how many values are comma separated in file that is got read
            }
            br.close();
            return arrInFile;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /////////////////////// MediaStore File Read & Write

}
