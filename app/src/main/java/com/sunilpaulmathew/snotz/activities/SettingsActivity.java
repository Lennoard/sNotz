package com.sunilpaulmathew.snotz.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sunilpaulmathew.snotz.BuildConfig;
import com.sunilpaulmathew.snotz.R;
import com.sunilpaulmathew.snotz.adapters.SettingsAdapter;
import com.sunilpaulmathew.snotz.utils.AppSettings;
import com.sunilpaulmathew.snotz.utils.Billing;
import com.sunilpaulmathew.snotz.utils.Common;
import com.sunilpaulmathew.snotz.utils.Encryption;
import com.sunilpaulmathew.snotz.utils.Security;
import com.sunilpaulmathew.snotz.utils.SettingsItems;
import com.sunilpaulmathew.snotz.utils.Utils;
import com.sunilpaulmathew.snotz.utils.sNotzColor;
import com.sunilpaulmathew.snotz.utils.sNotzData;
import com.sunilpaulmathew.snotz.utils.sNotzUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import in.sunilpaulmathew.sCommon.Utils.sCreditsUtils;
import in.sunilpaulmathew.sCommon.Utils.sThemeUtils;
import in.sunilpaulmathew.sCommon.Utils.sTranslatorUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 17, 2020
 */
public class SettingsActivity extends AppCompatActivity {

    private AppCompatImageButton mBack;
    private BiometricPrompt mBiometricPrompt;
    private ProgressBar mProgress;
    private String mJSONString = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mBack = findViewById(R.id.back_button);
        mProgress = findViewById(R.id.progress);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SettingsAdapter mRecycleViewAdapter = new SettingsAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (getData().get(position).getUrl() != null) {
                sUtils.launchUrl(getData().get(position).getUrl(), this);
            } else if (position == 0) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                startActivity(settings);
                finish();
            } else if (position == 1) {
                sThemeUtils.setAppTheme(this);
            } else if (position == 3) {
                if (Utils.isFingerprintAvailable(this)) {
                    mBiometricPrompt.authenticate(Utils.showBiometricPrompt(this));
                } else {
                    if (Security.isPINEnabled(this)) {
                        Security.authenticate(true, mRecycleViewAdapter,this);
                    } else {
                        Security.setPIN(false, getString(R.string.pin_enter), mRecycleViewAdapter, this);
                    }
                }
            } else if (position == 4) {
                if (sUtils.getBoolean("use_biometric", false, this) && Utils.isFingerprintAvailable(this)) {
                    Common.isHiddenNote(true);
                    mBiometricPrompt.authenticate(Utils.showBiometricPrompt(this));
                } else if (Security.isPINEnabled(this)) {
                    Security.authenticate(mRecycleViewAdapter, position,this);
                } else {
                    sUtils.saveBoolean("hidden_note", !sUtils.getBoolean("hidden_note", false, this), this);
                    mRecycleViewAdapter.notifyItemChanged(position);
                    Utils.reloadUI( this);
                }
            } else if (position == 6) {
                if (sNotzColor.isRandomColorScheme(this)) {
                    sUtils.snackBar(mRecyclerView, getString(R.string.note_color_random_message)).show();
                    return;
                }
                ColorPickerDialogBuilder
                        .with(this)
                        .setTitle(R.string.choose_color)
                        .initialColor(sUtils.getInt("accent_color", sUtils.getColor(R.color.color_teal, this), this))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(selectedColor -> {
                        })
                        .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) -> {
                            sUtils.saveInt("accent_color", selectedColor, this);
                            sUtils.snackBar(mRecyclerView, getString(R.string.choose_color_message, getString(R.string.note_color_background))).show();
                            Utils.reloadUI(this);
                            mRecycleViewAdapter.notifyItemChanged(position);
                            Common.isReloading(true);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        }).build().show();
            } else if (position == 7) {
                if (sNotzColor.isRandomColorScheme(this)) {
                    sUtils.snackBar(mRecyclerView, getString(R.string.note_color_random_message)).show();
                    return;
                }
                ColorPickerDialogBuilder
                        .with(this)
                        .setTitle(R.string.choose_color)
                        .initialColor(sUtils.getInt("text_color", sUtils.getColor(R.color.color_white, this), this))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(selectedColor -> {
                        })
                        .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) -> {
                            sUtils.saveInt("text_color", selectedColor, this);
                            sUtils.snackBar(mRecyclerView, getString(R.string.choose_color_message, getString(R.string.note_color_text))).show();
                            Utils.reloadUI(this);
                            mRecycleViewAdapter.notifyItemChanged(position);
                            Common.isReloading(true);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        }).build().show();
            } else if (position == 8) {
                if (sNotzColor.isRandomColorScheme(this)) {
                    sUtils.saveInt("random_color", Integer.MIN_VALUE, this);
                } else {
                    sUtils.saveInt("random_color", 0, this);
                }
                mRecycleViewAdapter.notifyItemRangeChanged(6, 3);
            } else if (position == 9) {
                if (sUtils.getBoolean("allow_images", false, this)) {
                    sUtils.saveBoolean("allow_images", false, this);
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else {
                    new MaterialAlertDialogBuilder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.warning)
                            .setMessage(getString(R.string.image_add_warning))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .setPositiveButton(R.string.go_ahead, (dialogInterface, i) -> {
                                sUtils.saveBoolean("allow_images", true, this);
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }).show();
                }
            } else if (position == 10) {
                sUtils.saveBoolean("auto_save", !sUtils.getBoolean("auto_save", false, this), this);
                mRecycleViewAdapter.notifyItemChanged(position);
            } else if (position == 11) {
                ColorPickerDialogBuilder
                        .with(this)
                        .setTitle(R.string.choose_color)
                        .initialColor(sUtils.getInt("checklist_color", sUtils.getColor(R.color.color_white, this), this))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(selectedColor -> {
                        })
                        .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) -> {
                            sUtils.saveInt("checklist_color", selectedColor, this);
                            mRecycleViewAdapter.notifyItemChanged(position);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        }).build().show();
            } else if (position == 12) {
                AppSettings.setRows(this);
            } else if (position == 13) {
                AppSettings.setFontSize(position, getData(), mRecycleViewAdapter, this);
            } else if (position == 14) {
                AppSettings.setFontStyle(position, getData(), mRecycleViewAdapter, this);
            } else if (position == 16) {
                if (sNotzData.isNotesEmpty(this)) {
                    sUtils.snackBar(mRecyclerView, getString(R.string.note_list_empty)).show();
                } else {
                    AppSettings.showBackupOptions(this);
                }
            } else if (position == 17) {
                if (mJSONString != null) mJSONString = null;
                sUtils.filePickerIntent(false, 0, null, this);
            } else if (position == 18) {
                if (sNotzData.isNotesEmpty(this)) {
                    sUtils.snackBar(mRecyclerView, getString(R.string.note_list_empty)).show();
                } else {
                    new MaterialAlertDialogBuilder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.warning)
                            .setMessage(getString(R.string.clear_notes_message))
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.delete, (dialog, which) -> {
                                if (sUtils.getBoolean("use_biometric", false, this) && Utils.isFingerprintAvailable(this)) {
                                    Common.isClearingNotes(true);
                                    mBiometricPrompt.authenticate(Utils.showBiometricPrompt(this));
                                } else if (Security.isPINEnabled(this)) {
                                    Security.authenticate(mRecycleViewAdapter, position,this);
                                } else {
                                    sUtils.delete(new File(getFilesDir(),"snotz"));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                    Utils.reloadUI(this);
                                    finish();
                                }
                            }).show();
                }
            } else if (position == 19) {
                Billing.showDonationMenu(this);
            } else if (position == 20) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_by_message, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            } else if (position == 21) {
                Intent welcome = new Intent(this, WelcomeActivity.class);
                startActivity(welcome);
                finish();
            } else if (position == 22) {
                new sTranslatorUtils(getString(R.string.app_name), "https://poeditor.com/join/project?hash=LOg2GmFfbV", this).show();
            } else if (position == 25) {
                new sCreditsUtils(AppSettings.getCredits(),
                        sUtils.getDrawable(R.mipmap.ic_launcher, this),
                        sUtils.getDrawable(R.drawable.ic_back, this),
                        sUtils.getColor(R.color.color_teal, this),
                        25, getString(R.string.app_name), "2021-2022, sunilpaulmathew",
                        BuildConfig.VERSION_NAME).launchCredits(this);
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());

        Executor executor = ContextCompat.getMainExecutor(this);
        mBiometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                sUtils.snackBar(mBack, getString(R.string.authentication_error, errString)).show();
                mRecycleViewAdapter.notifyItemRangeChanged(3,4);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (Common.isHiddenNote()) {
                    sUtils.saveBoolean("hidden_note", !sUtils.getBoolean("hidden_note", false, SettingsActivity.this), SettingsActivity.this);
                    Common.isHiddenNote(false);
                    Utils.reloadUI(SettingsActivity.this);
                    mRecycleViewAdapter.notifyItemChanged(4);
                } else if (Common.isClearingNotes()) {
                    Common.isClearingNotes(false);
                    sUtils.delete(new File(getFilesDir(),"snotz"));
                    Utils.reloadUI(SettingsActivity.this);
                    mRecycleViewAdapter.notifyItemChanged(18);
                } else {
                    Utils.useBiometric(SettingsActivity.this);
                    mRecycleViewAdapter.notifyItemChanged(3);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                sUtils.snackBar(mBack, getString(R.string.authentication_failed)).show();
                mRecycleViewAdapter.notifyItemRangeChanged(3,4);
            }
        });

        Utils.showBiometricPrompt(this);
    }

    private ArrayList<SettingsItems> getData() {
        ArrayList<SettingsItems> mData = new ArrayList<>();
        mData.add(new SettingsItems(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", "Copyright: © 2021-2022, sunilpaulmathew", sUtils.getDrawable(R.drawable.ic_info, this), null));
        mData.add(new SettingsItems(getString(R.string.app_theme), sThemeUtils.getAppTheme(this), sUtils.getDrawable(R.drawable.ic_theme, this), null));
        mData.add(new SettingsItems(getString(R.string.security), null, null, null));
        if (Utils.isFingerprintAvailable(this)) {
            mData.add(new SettingsItems(getString(R.string.biometric_lock), getString(R.string.biometric_lock_summary), sUtils.getDrawable(R.drawable.ic_fingerprint, this), null));
        } else {
            mData.add(new SettingsItems(getString(R.string.pin_protection), getString(R.string.pin_protection_message), sUtils.getDrawable(R.drawable.ic_lock, this), null));
        }
        mData.add(new SettingsItems(getString(R.string.show_hidden_notes), getString(R.string.show_hidden_notes_summary), sUtils.getDrawable(R.drawable.ic_eye, this), null));
        mData.add(new SettingsItems(getString(R.string.customize_note), null, null, null));
        mData.add(new SettingsItems(getString(R.string.note_color_background), getString(R.string.color_select_dialog, getString(R.string.note_color_background)), sUtils.getDrawable(R.drawable.ic_color, this), null));
        mData.add(new SettingsItems(getString(R.string.note_color_text), getString(R.string.color_select_dialog, getString(R.string.note_color_text)), sUtils.getDrawable(R.drawable.ic_text_color, this), null));
        mData.add(new SettingsItems(getString(R.string.note_color_random), getString(R.string.note_color_random_summary), sUtils.getDrawable(R.drawable.ic_colorize, this), null));
        mData.add(new SettingsItems(getString(R.string.image_include), getString(R.string.image_include_summary), sUtils.getDrawable(R.drawable.ic_image, this), null));
        mData.add(new SettingsItems(getString(R.string.auto_save), getString(R.string.auto_save_summary), sUtils.getDrawable(R.drawable.ic_save, this), null));
        mData.add(new SettingsItems(getString(R.string.check_list_widget_color), getString(R.string.check_list_widget_color_summary), sUtils.getDrawable(R.drawable.ic_checklist, this), null));
        mData.add(new SettingsItems(getString(R.string.notes_in_row), AppSettings.getRows(this), sUtils.getDrawable(R.drawable.ic_row, this), null));
        mData.add(new SettingsItems(getString(R.string.font_size), getString(R.string.font_size_summary, "" + sUtils.getInt("font_size", 18, this)),
                sUtils.getDrawable(R.drawable.ic_format_size, this), null));
        mData.add(new SettingsItems(getString(R.string.text_style), AppSettings.getFontStyle(this), sUtils.getDrawable(R.drawable.ic_text_style, this), null));
        mData.add(new SettingsItems(getString(R.string.misc), null, null, null));
        mData.add(new SettingsItems(getString(R.string.backup_notes), getString(R.string.backup_notes_summary), sUtils.getDrawable(R.drawable.ic_backup, this), null));
        mData.add(new SettingsItems(getString(R.string.restore_notes), getString(R.string.restore_notes_summary), sUtils.getDrawable(R.drawable.ic_restore, this), null));
        mData.add(new SettingsItems(getString(R.string.clear_notes), getString(R.string.clear_notes_summary), sUtils.getDrawable(R.drawable.ic_clear, this), null));
        mData.add(new SettingsItems(getString(R.string.donations), getString(R.string.donations_summary), sUtils.getDrawable(R.drawable.ic_donate, this), null));
        mData.add(new SettingsItems(getString(R.string.invite_friends), getString(R.string.invite_friends_Summary), sUtils.getDrawable(R.drawable.ic_share, this), null));
        mData.add(new SettingsItems(getString(R.string.welcome_note), getString(R.string.welcome_note_summary), sUtils.getDrawable(R.drawable.ic_home, this), null));
        mData.add(new SettingsItems(getString(R.string.translations), getString(R.string.translations_summary), sUtils.getDrawable(R.drawable.ic_translate, this), null));
        mData.add(new SettingsItems(getString(R.string.rate_us), getString(R.string.rate_us_Summary), sUtils.getDrawable(R.drawable.ic_rate, this), "https://play.google.com/store/apps/details?id=com.sunilpaulmathew.snotz"));
        mData.add(new SettingsItems(getString(R.string.support), getString(R.string.support_summary), sUtils.getDrawable(R.drawable.ic_support, this), "https://t.me/smartpack_kmanager"));
        mData.add(new SettingsItems(getString(R.string.credits), getString(R.string.credits_summary), sUtils.getDrawable(R.drawable.ic_credits, this), null));
        mData.add(new SettingsItems(getString(R.string.faq), getString(R.string.faq_summary), sUtils.getDrawable(R.drawable.ic_faq, this), "https://sunilpaulmathew.github.io/sNotz/faq/"));
        return mData;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                for (int result = bis.read(); result != -1; result = bis.read()) {
                    buf.write((byte) result);
                }
                try {
                    if (sNotzUtils.validBackup(Encryption.decrypt(buf.toString("UTF-8")))) {
                        mJSONString = Encryption.decrypt(buf.toString("UTF-8"));
                    } else if (sNotzUtils.validBackup(buf.toString("UTF-8"))) {
                        mJSONString = buf.toString("UTF-8");
                    }
                } catch (IllegalArgumentException ignored) {}
            } catch (IOException ignored) {}

            if (mJSONString == null) {
                sUtils.snackBar(mBack, getString(R.string.restore_error)).show();
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.restore_notes_question))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        sNotzUtils.restoreNotes(mJSONString, mProgress,this).execute();
                        finish();
                    }).show();
        }
    }

}