
package com.tunav.tunavmedi.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.tunav.tunavmedi.R;
import com.tunav.tunavmedi.dal.sqlite.helper.AuthenticationHelper;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */

public class LoginActivity extends Activity {
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private static final String tag = "UserLoginTask";

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        Integer userID = null;

        String err;

        public UserLoginTask() {
            Log.v(tag, "UserLoginTask()");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v(tag, "doInBackground()");
            mHelper.login(mID, mPassword);
            return mHelper.getStatus();
        }

        // invoked on the UI thread
        @Override
        protected void onCancelled() {
            loginTask = null;
            showProgress(false);
        }

        // invoked on the UI thread
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setResult(RESULT_OK);
                finish();
            } else {
                Log.i(tag, "Authentication faild!");
                mPasswordView
                        .setError(mHelper.getError());
                mPasswordView.requestFocus();
            }
            loginTask = null;
            showProgress(false);
        }

        // invoked on the UI thread
        @Override
        protected void onPreExecute() {
            Log.v(tag, "onPreExecute()");
            err = getString(R.string.error_incorrect_id_password);
        }
    }

    private String spName;
    private String spIsLogged;
    private String spDisplayName;
    private String spPhoto;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    public static final String tag = "LoginActivity";
    private UserLoginTask loginTask = null;
    // Values for ID and password at the time of the login attempt.
    private String mID;

    private String mPassword;

    // UI references.
    private EditText mIDView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private AuthenticationHelper mHelper;

    public static final String ACTION_LOGIN = "com.tunav.tunavmedi.action.LOGIN";
    public static final String ACTION_LOGOUT = "com.tunav.tunavmedi.action.LOGOUT";

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid ID, missing fields, etc.), the errors
     * are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (loginTask != null) {
            return;
        }

        // Reset errors.
        mIDView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mID = mIDView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            Log.d(tag, "Not a valid password");
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid ID.
        if (TextUtils.isEmpty(mID)) {
            Log.d(tag, "Not a valid ID");
            mIDView.setError(getString(R.string.error_field_required));
            focusView = mIDView;
            cancel = true;
        }

        // we are not making the assumption of getting mail as an id
        // else if (!mID.contains("@")) {
        // mIDView.setError(getString(R.string.error_invalid_ID));
        // focusView = mIDView;
        // cancel = true;
        // }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            Log.d(tag, "Error aquaring login/password.");
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            loginTask = new UserLoginTask();
            loginTask.execute();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(tag, "onCreate()");
        mHelper = new AuthenticationHelper(this);

        spName = getResources().getString(R.string.sp_user);
        spIsLogged = getResources().getString(R.string.spkey_is_logged);
        spDisplayName = getResources().getString(R.string.spkey_name);
        spPhoto = getResources().getString(R.string.spkey_password);

        mHelper.logout();
        onLogout();

        setContentView(R.layout.activity_login);
        // Set up the login form.
        // TODO autofilled login ID ?
        mIDView = (EditText) findViewById(R.id.id);
        mIDView.setText(mID);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                            KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.v(tag, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    // Sometimes called at the end of the full lifetime.
    @Override
    public void onDestroy() {
        Log.v(tag, "onDestroy()");
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroy();
    }

    public void onLogin(String name, String photo) {
        Log.v(tag, "onLogin()");
        SharedPreferences.Editor editor = getSharedPreferences(
                spName, Context.MODE_PRIVATE).edit();
        editor.putBoolean(spIsLogged, true);
        Log.v(tag, "sharedpreference set: " + spIsLogged);

        editor.putString(spDisplayName, mHelper.getDisplayName());
        Log.v(tag, "sharedpreference set: " + spDisplayName);

        editor.putString(spPhoto, mHelper.getPhoto());
        Log.v(tag, "sharedpreference set: " + spPhoto);

        editor.commit();
        Log.d(tag, "SharedPreferences commited!");
    }

    public void onLogout() {
        Log.i(tag, "onLogout()");
        SharedPreferences.Editor spEditor = getSharedPreferences(spName, Context.MODE_PRIVATE)
                .edit();
        spEditor.remove(spIsLogged);
        Log.v(tag, "sharedpreference removed: " + spIsLogged);
        spEditor.remove(spDisplayName);
        Log.v(tag, "sharedpreference removed: " + spDisplayName);
        spEditor.remove(spPhoto);
        Log.v(tag, "sharedpreference removed: " + spPhoto);
        spEditor.commit();
        Log.d(tag, "SharedPreferences commited!");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(tag, "onOptionItemSelected()");
        switch (item.getItemId()) {
            case R.id.action_about:
                MainActivity.about(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        Log.v(tag, "onPause()");
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground Activity.
        super.onPause();
    }

    // Called before subsequent visible lifetimes
    // for an activity process.
    @Override
    public void onRestart() {
        super.onRestart();
        Log.v(tag, "onRestart()");
        // Load changes knowing that the Activity has already
        // been visible within this process.
    }

    // Called after onCreate has finished, use to restore UI state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(tag, "onRestoreInstanceState()");
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        // Will only be called if the Activity has been
        // killed by the system since it was last visible.
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        Log.v(tag, "onResume()");
        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.
    }

    // Called to save UI state changes at the
    // end of the active lifecycle.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate and
        // onRestoreInstanceState if the process is
        // killed and restarted by the run time.
        Log.v(tag, "onSaveInstanceState()");
        super.onSaveInstanceState(savedInstanceState);
    }

    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();
        // Apply any required UI change now that the Activity is visible.
        Log.v(tag, "onStart()");
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop() {
        // Suspend remaining UI updates, threads, or processing
        // that aren't required when the Activity isn't visible.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        Log.v(tag, "onStop()");
        super.onStop();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        Log.v(tag, "showProgress()");
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
