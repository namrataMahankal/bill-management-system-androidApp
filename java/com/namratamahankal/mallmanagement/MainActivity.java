package com.namratamahankal.mallmanagement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int BARCODE_REQ_CODE = 200;
    private LinearLayout parentLinearLayout;
    TextView tv1, tv2, tv3, totalPrice;
    TextView tvName,tvQty,tvPrice;
    double total = 0, tempPrice = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parentLinearLayout = (LinearLayout) findViewById(R.id.parent_linear_layout);
        totalPrice = (TextView) findViewById(R.id.totalPrice);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BARCODE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                barcodeRecognition(photo);
            }
        }
    }

    private void barcodeRecognition(Bitmap photo) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        for (FirebaseVisionBarcode barcode : barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            Toast.makeText(MainActivity.this, rawValue, Toast.LENGTH_SHORT).show();
                            String[] arr = rawValue.split("@");
                            onAddField(arr[0], arr[1], arr[2]);
                           /* switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                    String ssid = barcode.getWifi().getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    break;
                            }*/
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void barcodeReco(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, BARCODE_REQ_CODE);
    }

    public void onAddField(String a, String b, String c) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.field, null);
        tv1 = (TextView) ((View) rowView).findViewById(R.id.name);
        tv1.setText(a);
        tv2 = (TextView) ((View) rowView).findViewById(R.id.qty);
        tv2.setText(b);
        tv3 = (TextView) ((View) rowView).findViewById(R.id.price);
        tv3.setText(c);
        // Add the new row before the add field button.
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 2);
        total = Double.parseDouble(totalPrice.getText().toString());
        total = total + Double.parseDouble(c);
        totalPrice.setText(String.valueOf(total));

    }

    public void onDelete(View v) {
        tv3 = (TextView) ((View) v.getParent()).findViewById(R.id.price);
        tempPrice = Double.parseDouble(tv3.getText().toString());
        parentLinearLayout.removeView((View) v.getParent());
        total = Double.parseDouble(totalPrice.getText().toString());
        total = total - tempPrice;
        totalPrice.setText(String.valueOf(total));

    }

    public void sendEmail(View view)
    {
        String message = "Qty  Price Product\n";

        for(int i=2;i<parentLinearLayout.getChildCount()-2;i++)
        {
            View v=parentLinearLayout.getChildAt(i);
            tvName=v.findViewById(R.id.name);
            tvQty=v.findViewById(R.id.qty);
            tvPrice=v.findViewById(R.id.price);
            message=message+tvQty.getText().toString()+"     "+tvPrice.getText().toString()+"     "+tvName.getText().toString()+"\n";
        }

        View v=parentLinearLayout.getChildAt(parentLinearLayout.getChildCount()-2);
        tvName=v.findViewById(R.id.name);
        tvQty=v.findViewById(R.id.qty);
        tvPrice=v.findViewById(R.id.totalPrice);
        message=message+tvName.getText().toString()+"    "+tvPrice.getText().toString()+"  "+tvQty.getText().toString()+"\n";




        // Example of Implicit Intent

        // Checkout complete MimeType list
        // https://www.freeformatter.com/mime-types-list.html

        Intent anotherAppIntent = new Intent(Intent.ACTION_SEND);
        // setType(...) indicates the type of data to send.
        // in the form of MIME Type
        anotherAppIntent.setType("text/plain");
        anotherAppIntent.setPackage("com.google.android.gm");
        anotherAppIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(anotherAppIntent);
    }

}
