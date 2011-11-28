package com.otdshco.backup;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.otdshco.backup.R;

public class BackupSettings extends
		PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.backup_sample_preferences);
	}
}
