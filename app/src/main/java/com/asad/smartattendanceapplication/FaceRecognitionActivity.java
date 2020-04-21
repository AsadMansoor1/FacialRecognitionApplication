package com.asad.smartattendanceapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.ACCURATE;

public class FaceRecognitionActivity extends AppCompatActivity {

    private static final String TAG = "ROTATION";
    private CameraKitView cameraKitView;
    private Button cameraButton;
    private AlertDialog.Builder alertDialog;
    private GraphicOverlay graphicOverlay;
    Map<String, List<double[]>> contours;
    List<double[]> myContour;
    private String filename = "SampleFile.txt";
    private String filepath = "MyFileStorage";
    private String dataToStoreInFile = "";
    File myExternalFile;

    public enum allFacialContour {
        allPoints,
        face,
        leftEye,
        leftEyebrowBottom,
        leftEyebrowTop,
        lowerLipBottom,
        lowerLipTop,
        noseBottom,
        noseBridge,
        rightEye,
        rightEyebrowBottom,
        rightEyebrowTop,
        upperLipBottom,
        upperLipTop
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        cameraKitView = findViewById(R.id.camera_view);
        cameraButton = findViewById(R.id.btn_detect);
        cameraButton.setText("Capture Image");
        graphicOverlay = findViewById(R.id.graphic_overlay);
        contours = new HashMap<>();
        alertDialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("Camera")
                .setMessage("Image Captured!")

                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    getDataAndSave(contours);
                    Toast toast = Toast.makeText(getApplicationContext(), "myContour list stored to external storage", Toast.LENGTH_SHORT);
                    toast.setMargin(50, 50);
                    toast.show();
                    cameraButton.setText("Capture Image");
                    graphicOverlay.clear();
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
                                        processFaceResult(faces);
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

    private void getDataAndSave(Map<String, List<double[]>> contours) {
        String data = "";
        for (allFacialContour c : allFacialContour.values()) {

            myContour = contours.get(c);
            data =data + c ;
            for (double[] resultContour : myContour) {
                for (double singleVal : resultContour) {
                    String stringContour = Double.toString(singleVal);
                    data = data + ","+ stringContour;
                }
            }
            data = data + System.getProperty("line.separator");
            writeToFile(data);
            Toast toast = Toast.makeText(getApplicationContext(),c + " Values written Successfully" , Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
        }
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

    private void     processFaceResult(List<FirebaseVisionFace> faces) {

        for (FirebaseVisionFace face : faces) {
            Rect bound = face.getBoundingBox();
            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, bound);
            graphicOverlay.add(rectOverlay);
        }
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

    protected void writeToFile(String dataWithNewLine) {
        filepath = getDirectoryType();
        myExternalFile = new File(filepath, filename);
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            fr = new FileWriter(myExternalFile, true);
            br = new BufferedWriter(fr);
            br.write(dataWithNewLine);
            Toast toast = Toast.makeText(getApplicationContext(), "Successfully data written to file", Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
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

}
