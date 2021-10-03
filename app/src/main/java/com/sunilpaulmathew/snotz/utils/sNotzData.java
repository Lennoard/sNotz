package com.sunilpaulmathew.snotz.utils;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 17, 2020
 */
public class sNotzData {

    public static List<sNotzItems> getData(Context context) {
        List<sNotzItems> mData = new ArrayList<>();
        String json = context.getFilesDir().getPath() + "/snotz";
        if (Utils.exist(json)) {
            for (int i = 0; i < Objects.requireNonNull(getsNotzItems(Utils.read(json))).length(); i++) {
                try {
                    JSONObject command = Objects.requireNonNull(getsNotzItems(Utils.read(json))).getJSONObject(i);
                    if (Common.getSearchText() == null) {
                        if (Utils.getBoolean("hidden_note", false, context)) {
                            mData.add(new sNotzItems(getNote(command.toString()), getDate(command.toString()), isHidden(command.toString()),
                                    getBackgroundColor(command.toString(), context), getTextColor(command.toString(), context), i));
                        } else if (!isHidden(command.toString())) {
                            mData.add(new sNotzItems(getNote(command.toString()), getDate(command.toString()), isHidden(command.toString()),
                                    getBackgroundColor(command.toString(), context), getTextColor(command.toString(), context), i));
                        }
                    } else if (Objects.requireNonNull(getNote(command.toString())).toLowerCase().contains(Common.getSearchText().toLowerCase())) {
                        if (Utils.getBoolean("hidden_note", false, context)) {
                            mData.add(new sNotzItems(getNote(command.toString()), getDate(command.toString()), isHidden(command.toString()),
                                    getBackgroundColor(command.toString(), context), getTextColor(command.toString(), context), i));
                        } else if (!isHidden(command.toString())) {
                            mData.add(new sNotzItems(getNote(command.toString()), getDate(command.toString()), isHidden(command.toString()),
                                    getBackgroundColor(command.toString(), context), getTextColor(command.toString(), context), i));
                        }
                    }
                } catch (JSONException ignored) {
                }
            }
            if (Utils.getInt("sort_notes", 0, context) == 2) {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getTimeStamp(), rhs.getTimeStamp()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Utils.getInt("sort_notes", 0, context) == 1) {
                Collections.sort(mData, Comparator.comparingLong(sNotzItems::getColorBackground));
            }
            if (Utils.getBoolean("reverse_order", true, context)) {
                Collections.reverse(mData);
            }
        }
        return mData;
    }

    public static List<sNotzItems> getRawData(Context context) {
        List<sNotzItems> mData = new ArrayList<>();
        String json = context.getFilesDir().getPath() + "/snotz";
        for (int i = 0; i < Objects.requireNonNull(getsNotzItems(Utils.read(json))).length(); i++) {
            try {
                JSONObject command = Objects.requireNonNull(getsNotzItems(Utils.read(json))).getJSONObject(i);
                mData.add(new sNotzItems(getNote(command.toString()), getDate(command.toString()), isHidden(command.toString()),
                        getBackgroundColor(command.toString(), context), getTextColor(command.toString(), context), getNoteD(command.toString())));
            } catch (JSONException ignored) {
            }
        }
        return mData;
    }

    public static JSONArray getsNotzItems(String json) {
        if (json != null && !json.isEmpty()) {
            try {
                JSONObject main = new JSONObject(json);
                return main.getJSONArray("sNotz");
            } catch (JSONException ignored) {
            }
        }
        return null;
    }

    public static String getNote(String string) {
        try {
            JSONObject obj = new JSONObject(string);
            return obj.getString("note");
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static String getDate(String string) {
        try {
            JSONObject obj = new JSONObject(string);
            return obj.getString("date");
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static boolean isHidden(String string) {
        try {
            JSONObject obj = new JSONObject(string);
            return obj.getBoolean("hidden");
        } catch (JSONException ignored) {
        }
        return false;
    }

    public static int getBackgroundColor(String string, Context context) {
        try {
            JSONObject obj = new JSONObject(string);
            return obj.getInt("colorBackground");
        } catch (JSONException ignored) {
        }
        return sNotzColor.getAccentColor(context);
    }

    public static int getTextColor(String string, Context context) {
        try {
            JSONObject obj = new JSONObject(string);
            return obj.getInt("colorText");
        } catch (JSONException ignored) {
        }
        return sNotzColor.getTextColor(context);
    }

    public static int getNoteD(String string) {
        try {
            JSONObject obj = new JSONObject(string);
            return obj.getInt("noteID");
        } catch (JSONException ignored) {
        }
        return -1;
    }

}