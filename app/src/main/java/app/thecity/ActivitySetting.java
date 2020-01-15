package app.thecity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import app.thecity.data.AppConfig;
import app.thecity.data.Constant;
import app.thecity.data.SharedPref;
import app.thecity.data.ThisApplication;
import app.thecity.utils.PermissionUtil;
import app.thecity.utils.Tools;

/**
 * ATTENTION : To see where list of setting comes is open res/xml/setting_notification.xml
 */
public class ActivitySetting extends PreferenceActivity {

    private AppCompatDelegate mDelegate;
    private ActionBar actionBar;

    private View parent_view;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_notification);
        parent_view = (View) findViewById(android.R.id.content);

        sharedPref = new SharedPref(getApplicationContext());

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ringtone)));

        Preference notifPref = (Preference) findPreference(getString(R.string.pref_key_notif));
        Preference resetCachePref = (Preference) findPreference(getString(R.string.pref_key_reset_cache));
        Preference themePref = (Preference) findPreference(getString(R.string.pref_key_theme));
        Preference ratePref = (Preference) findPreference("key_rate");
        Preference morePref = (Preference) findPreference("key_more");
        Preference aboutPref = (Preference) findPreference("key_about");
        Preference languaugePref= (Preference) findPreference(getString(R.string.pref_key_language));
        final Preference prefTerm = (Preference) findPreference(getString(R.string.pref_title_term));


        String language_saved=sharedPref.getLanguauge();
        if("fr".equals(language_saved))
            languaugePref.setSummary("Français");
        else if("en".equals(language_saved))
            languaugePref.setSummary("English");
        else
            languaugePref.setSummary("عربية");

        if (!AppConfig.THEME_COLOR) {
            PreferenceCategory categoryOthers = (PreferenceCategory) findPreference(getString(R.string.pref_category_display));
            categoryOthers.removePreference(themePref);
        }

        if (!PermissionUtil.isStorageGranted(this)) {
            PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.pref_category_notif));
            prefCat.setTitle(Html.fromHtml("<b>" + getString(R.string.pref_category_notif) + "</b><br>" + getString(R.string.grant_permission_account_storage)));
            notifPref.setEnabled(false);
        }

        resetCachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySetting.this);
                builder.setTitle(getString(R.string.dialog_confirm_title));
                builder.setMessage(getString(R.string.message_clear_image_cache));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.clearImageCacheOnBackground(ActivitySetting.this);
                        Snackbar.make(parent_view, getString(R.string.message_after_clear_image_cache), Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.CANCEL, null);
                builder.show();
                return true;
            }
        });

        notifPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean flag = (boolean) o;
                // analytics tracking
                ThisApplication.getInstance().trackEvent(Constant.Event.NOTIFICATION.name(), (flag ? "ENABLE" : "DISABLE"), "-");
                return true;
            }
        });

        ratePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Tools.rateAction(ActivitySetting.this);
                return true;
            }
        });

        morePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Tools.directLinkToBrowser(ActivitySetting.this, getString(R.string.more_app_url));
                return true;
            }
        });

        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Tools.aboutAction(ActivitySetting.this);
                return true;
            }
        });

        themePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                dialogColorChooser(ActivitySetting.this);
                // analytics tracking
                ThisApplication.getInstance().trackEvent(Constant.Event.THEME.name(), "CHANGE", "-");
                return true;
            }
        });

        prefTerm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dialogTerm(ActivitySetting.this);
                return true;
            }
        });

        languaugePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showChangeLangDialog();
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        initToolbar();
        super.onResume();
    }

    public void dialogTerm(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.pref_title_term));
        builder.setMessage(activity.getString(R.string.content_term));
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void dialogColorChooser(Activity activity) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_color_theme);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        ListView list = (ListView) dialog.findViewById(R.id.list_view);
        final String stringArray[] = getResources().getStringArray(R.array.arr_main_color_name);
        final String colorCode[] = getResources().getStringArray(R.array.arr_main_color_code);
        list.setAdapter(new ArrayAdapter<String>(ActivitySetting.this, android.R.layout.simple_list_item_1, stringArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setBackgroundColor(Color.parseColor(colorCode[position]));
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                sharedPref.setThemeColor(colorCode[pos]);
                dialog.dismiss();
                onResume();
            }
        });

        //global.setIntPref(global.I_PREF_COLOR, global.I_KEY_COLOR, getResources().getColor(R.color.red));
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed.
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "")
        );
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0 ? listPreference.getEntries()[index] : null
                );

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    private void initToolbar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.activity_title_settings);
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
        Tools.setActionBarColor(this, actionBar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Support for Activity : DO NOT CODE BELOW ----------------------------------------------------
     */

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }




    /*
     * To Change the language
     */

    public void showChangeLangDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.language_dialog, null);
        dialogBuilder.setView(dialogView);

        final RadioGroup radioGroupe = (RadioGroup) dialogView.findViewById(R.id.radio_groupe_languauge);
        RadioButton radio_en=(RadioButton)dialogView.findViewById(R.id.lang_english);
        RadioButton radio_fr=(RadioButton)dialogView.findViewById(R.id.lang_french);
        RadioButton radio_ar=(RadioButton)dialogView.findViewById(R.id.lang_arab);

        String language_saved=sharedPref.getLanguauge();
        if("fr".equals(language_saved))
            radio_fr.setChecked(true);
        else if("en".equals(language_saved))
            radio_en.setChecked(true);
        else
            radio_ar.setChecked(true);

       //  RadioButton radioButton;;

        dialogBuilder.setTitle(getResources().getString(R.string.pref_title_language));//;
        //dialogBuilder.setMessage(getResources().getString(R.string.lang_dialog_message));//;
        dialogBuilder.setPositiveButton(getResources().getString(R.string.change_lang_dialog_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int selectedId  = radioGroupe.getCheckedRadioButtonId();

                switch(selectedId) {
                    case R.id.lang_english: //English
                        //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "en").commit();
                        sharedPref.setLanguage("en");
                        setLangRecreate("en");
                        return;
                    case R.id.lang_french: //Francais
                        sharedPref.setLanguage("fr");
                        setLangRecreate("fr");
                        return;
                    case R.id.lang_arab:
                        sharedPref.setLanguage("ar");
                        setLangRecreate("ar");
                        return;
                    default: //By default set to english
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "ar").commit();
                        setLangRecreate("en");
                        return;
                }

                //showMsgAlertChangeLanguauge();
                         }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel_lang_dialog_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

/*    public void showMsgAlertChangeLanguauge()
    {

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.lang_dialog_message), Toast.LENGTH_LONG);

    }*/
    public void setLangRecreate(String langval) {
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(langval);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        recreate();

        //startActivity(new Intent(this,ActivitySplash.class));

/*        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();*/
    }


}
