package com.example.network;

import android.app.Application;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.LifecycleOwner;

import com.example.authentication.R;
import com.example.util.authentication.AuthFunctional;

// used to notify user about networkState with usage of com.example.network.ConnectionStateMonitor class
public class NetworkManager {
    private final Application application;
    private final TextView notificationView;
    private final ConnectionStateMonitor csMonitor;

    public NetworkManager(Application application){
        this.application = application;
        csMonitor = new ConnectionStateMonitor(application);
        notificationView = notificationView();
    }

    // returns notification textView
    private TextView notificationView(){
        TextView textView = new TextView(application);

        //android:id="@id/banner_id" - defined in res/ids.xml
        textView.setId(R.id.banner_id);

        // android:layout_width="wrap_content" && android:layout_height="wrap_content"
        textView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT));

        // android:gravity="center_horizontal"
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        // android:paddingTop="7dp"
        // android:paddingBottom="7dp"
        textView.setPadding(0,7,0,7);

        // android:text="@string/limited_or_no_connectivity"
        textView.setText(R.string.limited_or_no_connectivity);
        // other text attributes
        textView.setTextAppearance(R.style.textStyleNoConnectivityNotification);

        // android:drawableStartCompat="@drawable/signal" (works with drawableLeftCompat as well)
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.signal, 0, 0, 0);
        // android:drawablePadding="10dp"
        textView.setCompoundDrawablePadding(10);

        return textView;
    }

    // creates the whole banner
    public void addBanner(ConstraintLayout layout){
        // creating a linearLayout so that drawable in the textView can be centered too
        LinearLayout linearLayout = new LinearLayout(application);

        //android:id="@id/notification_layout_id" - defined in res/ids.xml
        linearLayout.setId(R.id.notification_layout_id);

        // android:layout_width="match_parent" && android:layout_height="wrap_content"
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // android:background="@color/BlueYonder"
        linearLayout.setBackgroundColor(application.getColor(R.color.BlueYonder));

        // android:gravity="center"
        linearLayout.setGravity(Gravity.CENTER);

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // adds the textView
        linearLayout.addView(notificationView);

        // setting up the flashing animation
        Animation flash = new AlphaAnimation(0.0f, 1.0f);
        flash.setDuration(800); // flash duration
        flash.setStartOffset(1600); // staying visible duration
        flash.setRepeatMode(Animation.REVERSE);
        flash.setRepeatCount(Animation.INFINITE);

        linearLayout.setAnimation(flash);

        // adds the linearLayout to the main one
        layout.addView(linearLayout);

        // setting constraints for linearLayout to constraintLayout(main one)
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        // app:layout_constraintStart_toStartOf="parent"
        constraintSet.connect(linearLayout.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, 0);
        // app:layout_constraintEnd_toEndOf="parent"
        constraintSet.connect(linearLayout.getId(), ConstraintSet.END, layout.getId(), ConstraintSet.END, 0);
        // app:layout_constraintTop_toTopOf="parent"
        constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 0);
        // applying constraints
        constraintSet.applyTo(layout);
    }

    public void registerConnectionObserver(LifecycleOwner lifecycleOwner, ConstraintLayout layout){
        csMonitor.observe(lifecycleOwner, connected -> {
            // removes any previous banner if it exists
            if (notificationView.getParent() != null)
                ((ViewGroup) notificationView.getParent()).removeView(notificationView);
            // adds new banner
            if (!connected) {
                addBanner(layout);
                layout.startLayoutAnimation(); // starts the flashing animation
                AuthFunctional.currentlyOnline = false;
            } else{ // updating currentlyOnline for the usage of the app
                AuthFunctional.currentlyOnline = true;
            }
        });
    }

    public void unregisterConnectionObserver(LifecycleOwner lifecycleOwner){
        csMonitor.removeObservers(lifecycleOwner);
    }
}
