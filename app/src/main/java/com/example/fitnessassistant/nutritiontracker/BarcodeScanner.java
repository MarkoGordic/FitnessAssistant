package com.example.fitnessassistant.nutritiontracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class BarcodeScanner extends Fragment {
    // launcher for the Camera Permission
    public final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (!result){
            // creates an alert dialog with rationale shown
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.exclamation);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.camera_access_denied);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> dialog.dismiss());

            // showing messages (one case if user selected don't ask again, other if user just selected deny)
            if(!shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION))
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.camera_access_message_denied_forever);
            else
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.camera_access_message_denied);
        }
    });

    public CameraSource cameraSource;
    private ToneGenerator toneGenerator;
    private boolean performingSearch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barcodescanner_screen, container, false);
        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
        startRealtimeDetection(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        super.onCreate(savedInstanceState);
    }

    private void startRealtimeDetection(View view){
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(requireContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(requireContext(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        ((SurfaceView)view.findViewById(R.id.surface_view)).getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                        cameraSource.start(holder);
                    else
                        PermissionFunctional.checkCameraPermission(requireContext(), cameraPermissionLauncher);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() { }

            @SuppressLint("SetTextI18n")
            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() != 0){
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);

                    if(!performingSearch) {
                        performingSearch = true;
                        cameraSource.stop();
                        APISearch.getInstance().searchAPI(barcodes.valueAt(0).displayValue, requireContext(), true, false, 1);
                    }
                }
            }
        });
    }
}
