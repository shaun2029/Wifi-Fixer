/*Copyright [2010-2011] [David Van de Ven]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.wahtod.wififixer.ui;

import java.util.List;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings.SettingNotFoundException;

public class PrefActivityHC extends PreferenceActivity implements
	OnSharedPreferenceChangeListener {

    @Override
    public void onBuildHeaders(List<Header> target) {
	loadHeadersFromResource(R.xml.preference_headers, target);
	super.onBuildHeaders(target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setWifiSleepPolicy(this);
    }

    @Override
    protected void onResume() {
	super.onResume();

	// Set up a listener for when key changes

	getPreferences(Activity.MODE_PRIVATE)
		.registerOnSharedPreferenceChangeListener(this);
	setWifiSleepPolicy(this);
    }

    @Override
    protected void onPause() {
	super.onPause();

	// Unregister the listener when paused
	getPreferences(Activity.MODE_PRIVATE)
		.unregisterOnSharedPreferenceChangeListener(this);
    }

    private static void setWifiSleepPolicy(final Context context) {
	/*
	 * Handle Wifi Sleep Policy
	 */
	ContentResolver cr = context.getContentResolver();
	try {
	    int wfsleep = android.provider.Settings.System.getInt(cr,
		    android.provider.Settings.System.WIFI_SLEEP_POLICY);
	    PrefUtil.writeString(context, PrefConstants.SLPOLICY_KEY, String
		    .valueOf(wfsleep));
	} catch (SettingNotFoundException e) {
	    /*
	     * Don't need a catch, all clients are >= 1.5 per manifest market
	     * restriction
	     */
	}
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	/*
	 * Dispatch intent if this is a pref service is interested in
	 */
	if (Pref.get(key) != null) {
	    /*
	     * First handle Service enable case
	     */
	    if (key.equals(Pref.DISABLE_KEY.key())) {
		if (!PrefUtil.readBoolean(this, Pref.DISABLE_KEY.key())) {
		    Intent intent = new Intent(
			    IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
		    sendBroadcast(intent);
		}

		else {
		    Intent intent = new Intent(
			    IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
		    sendBroadcast(intent);
		}

		return;
	    }
	    /*
	     * We want to notify for these, since they're prefs the service is
	     * interested in.
	     */

	    PrefUtil.notifyPrefChange(this, key, prefs.getBoolean(key, false));

	} else if (key.contains(PrefConstants.PERF_KEY)) {

	    int pVal = Integer.valueOf(PrefUtil.readString(this,
		    PrefConstants.PERF_KEY));

	    switch (pVal) {
	    case 1:
		PrefUtil.writeBoolean(this, Pref.WIFILOCK_KEY.key(), true);
		PrefUtil.writeBoolean(this, Pref.SCREEN_KEY.key(), true);
		break;

	    case 2:
		PrefUtil.writeBoolean(this, Pref.WIFILOCK_KEY.key(), true);
		PrefUtil.writeBoolean(this, Pref.SCREEN_KEY.key(), false);
		break;

	    case 3:
		PrefUtil.writeBoolean(this, Pref.WIFILOCK_KEY.key(), false);
		PrefUtil.writeBoolean(this, Pref.SCREEN_KEY.key(), false);
		break;
	    }
	    PrefActivityHC.this.finish();

	} else if (key.contains(PrefConstants.SLPOLICY_KEY)) {
	    /*
	     * Setting Wifi Sleep Policy
	     */
	    ContentResolver cr = getContentResolver();
	    int wfsleep = Integer.valueOf(PrefUtil.readString(this,
		    PrefConstants.SLPOLICY_KEY));
	    if (wfsleep != 3) {

		android.provider.Settings.System.putInt(cr,
			android.provider.Settings.System.WIFI_SLEEP_POLICY,
			wfsleep);
	    } else {
		/*
		 * Set to system state
		 */
		try {
		    wfsleep = android.provider.Settings.System.getInt(cr,
			    android.provider.Settings.System.WIFI_SLEEP_POLICY);
		    PrefUtil.writeString(this, PrefConstants.SLPOLICY_KEY,
			    String.valueOf(wfsleep));

		} catch (SettingNotFoundException e) {
		    /*
		     * Should always be found since our clients are > SDK2
		     */
		}
	    }
	}
    }
}
