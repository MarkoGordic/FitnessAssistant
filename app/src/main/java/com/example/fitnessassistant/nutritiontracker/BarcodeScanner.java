package com.example.fitnessassistant.nutritiontracker;

import static com.example.fitnessassistant.nutritiontracker.APISearch.barcodeDetected;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.diary.DiaryPageFragment;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class BarcodeScanner extends Fragment {
    public com.example.fitnessassistant.nutritiontracker.CameraSource cameraSource;
    private ToneGenerator toneGenerator;
    private boolean performingSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barcodescanner_screen, container, false);
        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);

        view.findViewById(R.id.flashSwitch).setOnClickListener(new View.OnClickListener() {
            boolean flashOn = false;
            @Override
            public void onClick(View v) {
                if(flashOn){
                    flashOn = false;
                    ((AppCompatImageButton) v).setImageResource(R.drawable.flash_off);
                    cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else{
                    flashOn = true;
                    ((AppCompatImageButton) v).setImageResource(R.drawable.flash_on);
                    cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    }
                }
        });

        performingSearch = false;

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

        cameraSource = new CameraSource.Builder(requireActivity(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .build();

        ((SurfaceView)view.findViewById(R.id.surface_view)).getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                requireActivity().runOnUiThread(() -> {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            cameraSource.start(holder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        requireActivity();
                });
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                requireActivity().runOnUiThread(() -> cameraSource.stop());
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
                        requireActivity().runOnUiThread(() -> {
                            cameraSource.stop();

                            DiaryPageFragment.shouldReceiveProducts.set(true);
                            barcodeDetected = barcodes.valueAt(0).displayValue;
                            APISearch.getInstance().searchAPI(barcodes.valueAt(0).displayValue, requireContext(), true, false, 1);

                            requireActivity().onBackPressed();
                        });
                    }
                }
            }
        });
    }
}
