package com.example.authentication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class AuthFunctional {
    // when creating account or requesting password reset, emailLinked is copied back to sign in page
    public static String emailLinked = null;

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
    public static void addPasswordViewToggle(EditText pass) {
        Typeface defaultTypeface = pass.getTypeface(); // regular font converted to typeface
        TransformationMethod defaultTransformationMethod = pass.getTransformationMethod(); // used for regular text
        pass.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index of right drawables
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // ACTION_DOWN = finger on screen, ACTION_UP = finger on -> off screen
                // getRawX() is where touch is registered, anything on x axis greater than eTRightPosition - 2 * drawableWidth is registered
                if (event.getRawX() >= (pass.getRight() - 2 * pass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (pass.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setTransformationMethod(new MyPasswordTransformationMethod());
                        pass.setTypeface(defaultTypeface); // used because it changes the font
                        pass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.custom_lock, 0); // changes the icon
                    } else {
                        pass.setTransformationMethod(defaultTransformationMethod);
                        pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setTypeface(defaultTypeface); // used because it changes the font
                        pass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.custom_unlock, 0); // changes the icon
                    }
                    return true;
                }
            }
            return false;
        });
    }

    // setting the outline of place to red and undoing when text is changed, also sets an error message
    public static void myError(Context context, EditText place, String message){
        place.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input_error));
        place.setError(message);
        place.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            { place.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_input));}
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // starts "loading animation" by hiding button and revealing progress bar
    public static void startLoading(View button, View bar){
        button.setVisibility(View.INVISIBLE);
        bar.setVisibility(View.VISIBLE);
    }

    // finishes "loading animation" by revealing button and hiding progress bar
    public static void finishLoading(View button, View bar){
        bar.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
    }

    // adds toggle to drawable on the right of password editText and transformation method
    public static void setUpPassword(EditText password){
        addPasswordViewToggle(password);
        password.setTransformationMethod(new MyPasswordTransformationMethod());
    }

    // sets errors based on user's input
    public static void setError(Context context, String email, EditText emailEdit, EditText passwordEdit){
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if(result != null) {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null) {
                        if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                            if (passwordEdit != null)
                                myError(context, passwordEdit, context.getString(R.string.incorrect_password));
                        }
                        else
                            myError(context, emailEdit, context.getString(R.string.email_not_registered));
                    }
                } // TODO handle an else if for no network connection
            } else
                myError(context, emailEdit, context.getString(R.string.invalid_email));
        });
    }
}
