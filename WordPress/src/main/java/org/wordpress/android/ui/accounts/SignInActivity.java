package org.wordpress.android.ui.accounts;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.models.Blog;
import org.wordpress.android.ui.ActivityId;

public class SignInActivity extends Activity {
    public static final int SIGN_IN_REQUEST = 1;
    public static final int REQUEST_CODE = 5000;
    public static final int ADD_SELF_HOSTED_BLOG = 2;
    public static final int NEW_LOGIN_SELF_HOSTED = 3;
    public static final int NEW_LOGIN_WP_COM = 5;
    public static final int SHOW_CERT_DETAILS = 4;
    public static String START_FRAGMENT_KEY = "start-fragment";
    public static final String ARG_JETPACK_SITE_AUTH = "ARG_JETPACK_SITE_AUTH";
    public static final String ARG_JETPACK_MESSAGE_AUTH = "ARG_JETPACK_MESSAGE_AUTH";
    public static final String ARG_IS_AUTH_ERROR = "ARG_IS_AUTH_ERROR";

    protected SignInFragment mSignInFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_activity);

        mSignInFragment = getSignInFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mSignInFragment);
        fragmentTransaction.commit();

        actionMode(getIntent().getExtras());

        ActivityId.trackLastActivity(ActivityId.LOGIN);
    }

    protected void actionMode(Bundle extras) {
        int actionMode = SIGN_IN_REQUEST;
        if (extras != null) {
            actionMode = extras.getInt(START_FRAGMENT_KEY, -1);

            if (extras.containsKey(ARG_JETPACK_SITE_AUTH)) {
                Blog jetpackBlog = WordPress.getBlog(extras.getInt(ARG_JETPACK_SITE_AUTH));
                if (jetpackBlog != null) {
                    String customMessage = extras.getString(ARG_JETPACK_MESSAGE_AUTH, null);
                    mSignInFragment.setBlogAndCustomMessageForJetpackAuth(jetpackBlog, customMessage);
                }
            } else if (extras.containsKey(ARG_IS_AUTH_ERROR)) {
                mSignInFragment.showAuthErrorMessage();
            }
        }
        switch (actionMode) {
            case ADD_SELF_HOSTED_BLOG:
                mSignInFragment.forceSelfHostedMode();
                break;
            case NEW_LOGIN_SELF_HOSTED:
                mSignInFragment.setNewLoginView(false);
                break;
            case NEW_LOGIN_WP_COM:
                mSignInFragment.setNewLoginView(true);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_CERT_DETAILS) {
            mSignInFragment.askForSslTrust();
        } else if (resultCode == RESULT_OK && data != null) {
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            if (username != null) {
                mSignInFragment.signInDotComUser(username, password);
            }
        }
    }

    public SignInFragment getSignInFragment() {
        return new SignInFragment();
    }
}
