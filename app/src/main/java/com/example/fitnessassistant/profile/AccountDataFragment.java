package com.example.fitnessassistant.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.AuthFunctional;

public class AccountDataFragment extends Fragment {

    public static Bitmap scaleBitmap(Bitmap bmp, int maxBytes){
        Bitmap b = Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.drawBitmap(bmp, 0, 0, null);

        int currentPixels = b.getWidth() * b.getHeight();
        int maxPixels = maxBytes / 4;
        if (currentPixels < maxPixels)
            return b;

        double scaleFactor = Math.sqrt(maxPixels / (double) currentPixels);
        int newWidthPx = (int) Math.floor(b.getWidth() * scaleFactor);
        int newHeightPx = (int) Math.floor(b.getHeight() * scaleFactor);

        return Bitmap.createScaledBitmap(b, newWidthPx, newHeightPx, true);
    }

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // linkAccountsTextView listener - shows LinkAccountsFragment, hides current fragment
        view.findViewById(R.id.linkAccountsTextView).setOnClickListener(view1 -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.linkAccountsFragment).addToBackStack(null).commit());

        // changeEmailTextView listener - calling the alert dialog
        view.findViewById(R.id.changeEmailTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpEmailChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.notification));
        });

        // changePasswordButton listener - calling the alert dialog
        view.findViewById(R.id.changePasswordTextView).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                AuthFunctional.setUpPasswordChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.notification));
        });

        // changeUserNameTextView listener - calling the alert dialog
        view.findViewById(R.id.changeUserNameTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpUserNameChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.notification));
        });

        // changeProfilePictureTextView listener - gets the image from gallery
        view.findViewById(R.id.changeProfilePicture).setOnClickListener(view1 -> ((InAppActivity) requireActivity()).imageGetter.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.account_data_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        return view;
    }
}
