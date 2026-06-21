package com.example.mobileapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button captureImageFab;
    private ImageView inputImageView;
    private ImageView imgSampleOne;
    private ImageView imgSampleTwo;
    private ImageView imgSampleThree;
    private TextView tvPlaceholder;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageFab = findViewById(R.id.captureImageFab);
        inputImageView = findViewById(R.id.imageView);
        imgSampleOne = findViewById(R.id.imgSampleOne);
        imgSampleTwo = findViewById(R.id.imgSampleTwo);
        imgSampleThree = findViewById(R.id.imgSampleThree);
        tvPlaceholder = findViewById(R.id.tvPlaceholder);

        imgSampleOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setViewAndDetect(getSampleImage(R.drawable.image_test));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        imgSampleTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setViewAndDetect(getSampleImage(R.drawable.img_meal_two));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        imgSampleThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setViewAndDetect(getSampleImage(R.drawable.img_meal_two));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        captureImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Taking a photo", Toast.LENGTH_LONG).show();
            }
        });

    }
    private Bitmap getSampleImage(int resId){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;


        return BitmapFactory.decodeResource(getResources(), resId, options);
    }

    private void setViewAndDetect(Bitmap bitmap) throws IOException {
        // Display capture image
        inputImageView.setImageBitmap(bitmap);
        tvPlaceholder.setVisibility(View.INVISIBLE);

        runObjectDetection(bitmap);
    }

    private void runObjectDetection(Bitmap bitmap) throws IOException {
        TensorImage image = TensorImage.fromBitmap(bitmap);
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(5)
                .setScoreThreshold(0.5f)
                .build();
        ObjectDetector detector = ObjectDetector.createFromFileAndOptions(
                this, // the application context
                "android.tflite", // must be same as the filename in assets folder
                options
        );

        List<Detection> results = detector.detect(image);

        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(outputBitmap);
        Paint pen = new Paint();
        pen.setTextAlign(Paint.Align.LEFT);


        for (Detection result:results){
            pen.setColor(Color.RED);
            pen.setStrokeWidth(8F);
            pen.setStyle(Paint.Style.STROKE);
            RectF box = result.getBoundingBox();
            canvas.drawRect(box, pen);

            Rect tagSize = new Rect(0, 0, 0, 0);

            // calculate the right font size
            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(Color.YELLOW);
            pen.setStrokeWidth(2F);

            Category category = result.getCategories().get(0);
            String text = category.getLabel() + " " + Math.round(category.getScore()*100) + "%";

            pen.setTextSize(96F);
            pen.getTextBounds(text, 0, text.length(), tagSize);

            canvas.drawText(
                    text, box.left,
                    box.top, pen
            );

        }

        inputImageView.setImageBitmap(outputBitmap);

    }


}