package com.hbatalha.facedetector;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_IMAGE_CAPTURE = 1;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);
                    detectFace(bitmap);
                }
            }
        });

        Button button = findViewById(R.id.camera_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                try {
                    activityResultLauncher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void detectFace(Bitmap bitmap) {
        // Face Detector options
        FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL).build();
        FaceDetector faceDetector = FaceDetection.getClient(faceDetectorOptions);

        InputImage inputImage = InputImage.fromBitmap(bitmap, /*degrees*/ 0);
        Task<List<Face>> task = faceDetector.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(@NonNull List<Face> faces) {

                Paint paint = new Paint();
                paint.setStrokeWidth(2);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);

                Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(tempBitmap);
                canvas.drawBitmap(bitmap,0,0,null);

                for (Face face : faces) {
                    Rect bounds = face.getBoundingBox();
                    canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, paint);
                }
                if(! faces.isEmpty()) {
                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

                    Toast.makeText(MainActivity.this, String.valueOf(faces.size()) +" Face(s) Detected",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(MainActivity.this,"No Faces Detected",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Error processing the image: " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}