package com.example.fitnessassistant.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.fitnessassistant.R;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// class used for variables and functions called during authentication processes with Firebase
public class AuthFunctional {
    public static boolean currentlyOnline;
    private static boolean quickFlashAnimating; // used to prevent spamming the notification

    // when password is hidden, its characters are transformed by this function into '●'
    public static class MyPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(super.getTransformation(source, view));
        }
        private static class PasswordCharSequence implements CharSequence {
            private final CharSequence mSource;
            public PasswordCharSequence(CharSequence source) { mSource = source; }
            public char charAt(int index) {
                if(mSource.charAt(index) == '\u2022') // '\u2022' are dots used by default
                    return '●'; // they are replaced by bigger ones
                else
                    return mSource.charAt(index);
            }
            public int length() { return mSource.length(); }
            @NonNull
            public CharSequence subSequence(int start, int end) { return mSource.subSequence(start, end); }
        }
    }

    // adding the toggle view option for lock drawable in password editText
    @SuppressLint("ClickableViewAccessibility")
    public static void addPasswordViewToggle(EditText passEdit) {
        Typeface defaultTypeface = passEdit.getTypeface(); // regular font converted to typeface
        TransformationMethod defaultTransformationMethod = passEdit.getTransformationMethod(); // used for regular text
        passEdit.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index of right drawables
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // ACTION_DOWN = finger on screen, ACTION_UP = finger on -> off screen
                // getRawX() is where touch is registered, anything on x axis greater than eTRightPosition - 2 * drawableWidth is registered
                if (event.getRawX() >= (passEdit.getRight() - 2 * passEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (passEdit.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                        passEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passEdit.setTransformationMethod(new MyPasswordTransformationMethod());
                        passEdit.setTypeface(defaultTypeface); // used because it changes the font
                        passEdit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.custom_lock, 0); // changes the icon
                    } else {
                        passEdit.setTransformationMethod(defaultTransformationMethod);
                        passEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passEdit.setTypeface(defaultTypeface); // used because it changes the font
                        passEdit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.custom_unlock, 0); // changes the icon
                    }
                    return true;
                }
            }
            return false;
        });
    }

    public static void myError(Context context, EditText place, String message){
        // setting outline of place to red
        place.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input_error));
        // setting an error message
        place.setError(message);
        place.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            // undo when text is changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            { place.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input));}
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // starts "loading animation" by hiding view and revealing progress bar
    public static void startLoading(View view, View bar){
        view.setVisibility(View.INVISIBLE);
        bar.setVisibility(View.VISIBLE);
    }

    // finishes "loading animation" by revealing view and hiding progress bar
    public static void finishLoading(View view, View bar){
        bar.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }

    // adds toggle to drawable on the right of password editText and transformation method
    public static void setUpPassword(EditText password){
        addPasswordViewToggle(password);
        password.setTransformationMethod(new MyPasswordTransformationMethod());
    }

    // sets errors based on user's input
    public static void setAuthenticationError(Context context, String email, EditText emailEdit, EditText passwordEdit, TextView forgotPassword, TextView createAccount){
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if(result != null) {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null) {
                        if(signInMethods.isEmpty()){
                            myError(context, emailEdit, context.getString(R.string.email_not_registered));
                            if(createAccount != null)
                                createAccount.startAnimation(AnimationUtils.loadAnimation(context,R.anim.quick_flash));
                        } else if(signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)){
                            if (passwordEdit != null) {
                                myError(context, passwordEdit, context.getString(R.string.incorrect_password));
                                if(forgotPassword != null)
                                    forgotPassword.startAnimation(AnimationUtils.loadAnimation(context, R.anim.quick_flash));
                            }
                        } else if(signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD))
                            myError(context, emailEdit, context.getString(R.string.email_connected_via_google));
                        else if(signInMethods.contains(FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD))
                            myError(context, emailEdit, context.getString(R.string.email_connected_via_facebook));
                    }
                }
            } else
                myError(context, emailEdit, context.getString(R.string.invalid_email));
        });
    }

    // used for quickFlashing the notification layout
    public static void quickFlash(Context context, LinearLayout notificationLayout){
        if(notificationLayout != null) { // this could happen if quickFlash is called faster than the notificationLayout is registered

            Animation previous = notificationLayout.getAnimation();

            Animation current = AnimationUtils.loadAnimation(context, R.anim.quick_flash);
            current.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // continuing(starting) the previous animation
                    notificationLayout.startAnimation(previous);
                    quickFlashAnimating = false;
                }
            });

            // this avoids spamming and problems that may get caused by spamming
            if(!quickFlashAnimating) {
                notificationLayout.startAnimation(current);
                quickFlashAnimating = true;
            }
        }
    }

    // flashing used for the checkbox at registering (if user doesn't check)
    public static void checkboxFlash(Context context, CheckBox checkBox){
        // extracted into a function to disable clicking
        checkBox.setEnabled(false);
        checkBox.setClickable(false);
        Animation quickFlash = AnimationUtils.loadAnimation(context, R.anim.quick_flash);
        quickFlash.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                // enabling clicking once animation is finished
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
            }
        });
        checkBox.startAnimation(quickFlash);
    }

    // validating username for register with regex
    public static boolean validUsername(Context context, EditText userEdit){
        String username = userEdit.getText().toString();

        // must not be empty
        if(TextUtils.isEmpty(username)) {
            AuthFunctional.myError(context, userEdit, context.getString(R.string.empty_name));
            return false;
        }

        // length: 5 - 20
        if(username.length() > 20){
            myError(context, userEdit, context.getString(R.string.username_too_long_error));
            return false;
        } else if(username.length() < 5){
            myError(context, userEdit, context.getString(R.string.username_too_short_error));
            return false;
        }

        // starts with a letter
        String firstCharPattern= "^[A-Za-z]";
        Pattern p1 = Pattern.compile(firstCharPattern);
        Matcher m1 = p1.matcher(username);
        if(!m1.find()){
            myError(context, userEdit, context.getString(R.string.username_first_char_error));
            return false;
        }

        // can contain 5-20 letters, numbers, dots, underscores and hyphens but those can't be consecutive
        String otherCharsPattern = "^([._-](?![._-])|[A-Za-z0-9]){5,20}$";
        Pattern p2 = Pattern.compile(otherCharsPattern);
        Matcher m2 = p2.matcher(username);
        if(!m2.find()){
            myError(context, userEdit, context.getString(R.string.username_other_chars_error));
            return false;
        }

        // last character must be a letter or a number
        String lastCharPattern = "[A-Za-z0-9]$";
        Pattern p3 = Pattern.compile(lastCharPattern);
        Matcher m3 = p3.matcher(username);
        if(!m3.find()){
            myError(context, userEdit, context.getString(R.string.username_last_char_error));
            return false;
        }

        return true;
    }

    // in case registering fails, error will be set if the entered email is already connected with an FitnessAssistant account or an Google account
    public static void emailAlreadyRegistered(Context context, EditText emailEdit, String email){
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if(result != null) {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null){
                        if(signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                            myError(context, emailEdit, context.getString(R.string.email_already_registered));
                        else if(signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD))
                            myError(context, emailEdit, context.getString(R.string.email_connected_via_google));
                        else if(signInMethods.contains(FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD))
                            myError(context, emailEdit, context.getString(R.string.email_connected_via_facebook));
                    }
                }
            }
        });
    }

    // validating email for register with regex
    public static boolean validEmail(Context context, EditText emailEdit){
        String email = emailEdit.getText().toString();

        // must not be empty
        if(TextUtils.isEmpty(email)) {
            AuthFunctional.myError(context, emailEdit, context.getString(R.string.empty_email));
            return false;
        }

        // regex for email
        String emailRegexPattern = "(?=.{1,64}@)^[a-zA-z0-9_-]+(\\.[a-zA-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-za-z]{2,})$";
        Pattern p = Pattern.compile(emailRegexPattern);
        Matcher m = p.matcher(email);
        if(!m.find()){
            myError(context, emailEdit, context.getString(R.string.email_register_error));
            return false;
        }

        return true;
    }

    // validating password for register with regex
    public static boolean validPassword(Context context, EditText passEdit){
        String password = passEdit.getText().toString();

        // must not be empty
        if(TextUtils.isEmpty(password)) {
            AuthFunctional.myError(context, passEdit, context.getString(R.string.empty_password));
            return false;
        }

        // used for a strong password
        String passwordPattern = "^" +
                "(?=.*[0-9])" + // at least one digit
                "(?=.*[a-z])" + // at least one lowercase character
                "(?=.*[A-Z])" + // at least one uppercase character
                "(?=.*[!@#&()–{}:;',?/*~$^+=<>])" + // at least one special character
                ".{6,20}$"; // length between 6 and 20
        Pattern p = Pattern.compile(passwordPattern);
        Matcher m = p.matcher(password);
        if(!m.matches()){
            myError(context, passEdit, context.getString(R.string.password_not_strong_enough));
            return false;
        }
        return true;
    }

    // used for passwordEditText and confirmPasswordEditText -> setting the error on both and removing from both onTextChanged
    public static void bothPasswordsError(Context context, EditText first, EditText second, String message){
        // setting outline of place to red
        first.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input_error));
        second.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input_error));
        // setting an error message
        first.setError(message);
        second.setError(message);
        first.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            // undo when text is changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                first.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input));
                second.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input));
                // editText on which text changed will null error by default, other has to be change manually
                second.setError(null);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        second.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            // undo when text is changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                first.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input));
                second.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input));
                // editText on which text changed will null error by default, other has to be change manually
                first.setError(null);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // displaying results from sending an email verification
    public static void emailVerificationSentAnimation(Context context, TextView messageTextView){
        // textView fading in and finishing the activity
        Animation fadeIn  = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);

        // textView fading out, setting new text and calling fadeIn animation
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                messageTextView.setText(context.getString(R.string.successful_verification_sent));
                messageTextView.startAnimation(fadeIn);
            }
        });
        messageTextView.startAnimation(fadeOut);
    }
}