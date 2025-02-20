package com.sunilpaulmathew.snotz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.sunilpaulmathew.snotz.activities.WelcomeActivity;
import com.sunilpaulmathew.snotz.fragments.sNotzFragment;
import com.sunilpaulmathew.snotz.utils.sNotzUtils;
import com.sunilpaulmathew.snotz.utils.sNotzWidgets;

import in.sunilpaulmathew.sCommon.Utils.sCrashReporterUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 13, 2020
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);

        // Record crashes
        new sCrashReporterUtils(sUtils.getColor(R.color.color_teal, this), 25, this).initialize();

        if (!sUtils.getBoolean("welcome_message", false, this)) {
            Intent welcome = new Intent(this, WelcomeActivity.class);
            startActivity(welcome);
            sUtils.saveBoolean("welcome_message", true, this);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                getMainFragment()).commit();

    }

    private Fragment getMainFragment() {
        int extraNoteId = getIntent().getIntExtra(sNotzWidgets.getNoteID(), sNotzWidgets.getInvalidNoteId());
        String extraCheckListPath = getIntent().getStringExtra(sNotzWidgets.getChecklistPath());
        String externalNote = getIntent().getStringExtra(sNotzUtils.getExternalNote());

        Bundle bundle = new Bundle();
        if (extraCheckListPath != null) {
            bundle.putString(sNotzWidgets.getChecklistPath(), extraCheckListPath);
        } else if (extraNoteId != sNotzWidgets.getInvalidNoteId()) {
            bundle.putInt(sNotzWidgets.getNoteID(), extraNoteId);
        } else if (externalNote != null) {
            bundle.putString(sNotzUtils.getExternalNote(), externalNote);
        }

        Fragment sNotzFragment = new sNotzFragment();
        sNotzFragment.setArguments(bundle);
        return sNotzFragment;
    }

}