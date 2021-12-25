package com.example.fitnessassistant.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageFragment extends Fragment {
    // gives welcome message based on time
    private void greetUser(View view){
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(true);
        TextView welcomeTextView = view.findViewById(R.id.welcomeMessageTextView); // TextView in top right corner for welcome message

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcomeTextView.setText(getString(R.string.good_morning));
        else if(systemHours >= 12 && systemHours < 18)
            welcomeTextView.setText(getString(R.string.good_afternoon));
        else if(systemHours >= 18 && systemHours < 22)
            welcomeTextView.setText(getString(R.string.good_evening));
        else
            welcomeTextView.setText(getString(R.string.good_night));
        if(FirebaseAuth.getInstance().getCurrentUser() != null) // greet the user, don't just say welcome :)
            welcomeTextView.setText(String.format("%s, %s", welcomeTextView.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
        else
            Toast.makeText(requireContext(), R.string.unable_to_refresh, Toast.LENGTH_SHORT).show();
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
    }

    private void setUpOnClickListeners(View view){
        // swipeRefreshLayout refresh listener - refreshes for 1.5s while updating UI
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(() -> greetUser(view));

        view.findViewById(R.id.startActivity).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction().hide(HomePageFragment.this).add(R.id.in_app_container, InAppActivity.activityTrackingFragment).addToBackStack(null).commit();
            //requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        greetUser(requireView());
    }
}
