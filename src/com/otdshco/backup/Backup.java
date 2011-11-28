package com.otdshco.backup;
import java.io.IOException;
import com.otdshco.backup.R;
import com.otdshco.tools.CheckStatus;
import com.otdshco.tools.Convert;
import com.otdshco.tools.Logger;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class Backup extends
		Activity implements
				OnClickListener
{
	private ScrollView			backupScrollView;
	private TextView			backupTextView1;
	private String				backupStringView1;
	private TextView			backupTextView2;
	private String				backupStringView2;
	private Thread				backupProcess;
	private Button				backupButton;
	private Button				backupSettingsButton;
	private boolean				backupBlinking	=true;
	private SharedPreferences	backupSharedPreferences;
	private String				sdcard			="/sdcard:";
	private String				data_app		="/data/app:";
	private String				system			="/system:";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_main);
		backupButton=(Button)findViewById(R.id.backup_button);
		backupButton.setOnClickListener(this);
		backupSettingsButton=(Button)findViewById(R.id.backup_settings_button);
		backupSettingsButton.setOnClickListener(this);
		backupScrollView=(ScrollView)findViewById(R.id.backup_scroll_view);
		backupTextView1=(TextView)findViewById(R.id.backup_text_view_1);
		backupTextView2=(TextView)findViewById(R.id.backup_text_view_2);
		CheckStatus backupCheckStatus=new CheckStatus();
		addText("_____________________________\n");
		try
		{
			if (backupCheckStatus.checkInstalled())
			{
				setText("DSI Update is INSTALLED\n");
			}
			else
			{
				setText("DSI Update is NOT INSTALLED\n");
			}
			if (backupCheckStatus.checkDsi())
			{
				setText("DSI Update is ON\n");
			}
			else
			{
				setText("DSI Update is OFF\n");
			}
		}
		catch(IOException e)
		{
			setText("No DSI Update\n");
		}
		catch(InterruptedException e)
		{
			setText("No DSI Update\n");
		}
		try
		{
			if (backupCheckStatus.checkHome())
			{
				addText("Launcher kept on memory\n");
			}
			else
			{
				addText("Launcher not kept on memory\n");
			}
		}
		catch(IOException e)
		{
			addText("Launcher not kept on memory\n");
		}
		catch(InterruptedException e)
		{
			addText("Launcher not kept on memory\n");
		}
		try
		{
			double sdtot=backupCheckStatus.getTotal(sdcard);
			double sduse=backupCheckStatus.getUsed(sdcard);
			double sdfre=backupCheckStatus.getFree(sdcard);
			double sdusep=backupCheckStatus.getUsedP(sdcard);
			double sdfrep=backupCheckStatus.getFreeP(sdcard);
			addText("_____________________________\n");
			addText("SDCARD\n");
			addText("TOTAL:\t"+
					Convert.decimal(sdtot)+
					" bytes\t100,00%\n");
			addText("USED:\t"+
					Convert.decimal(sduse)+
					" bytes\t"+
					Convert.percent(sdusep)+
					"%\n");
			addText("FREE:\t"+
					Convert.decimal(sdfre)+
					" bytes\t"+
					Convert.percent(sdfrep)+
					"%\n");
			double aptot=backupCheckStatus.getTotal(data_app);
			double apuse=backupCheckStatus.getUsed(data_app);
			double apfre=backupCheckStatus.getFree(data_app);
			double apusep=backupCheckStatus.getUsedP(data_app);
			double apfrep=backupCheckStatus.getFreeP(data_app);
			addText("_____________________________\n");
			addText("DATA\n");
			addText("TOTAL:\t"+
					Convert.decimal(aptot)+
					" bytes\t100,00%\n");
			addText("USED:\t"+
					Convert.decimal(apuse)+
					" bytes\t"+
					Convert.percent(apusep)+
					"%\n");
			addText("FREE:\t"+
					Convert.decimal(apfre)+
					" bytes\t"+
					Convert.percent(apfrep)+
					"%\n");
			addText("_____________________________\n");
			double sytot=backupCheckStatus.getTotal(system);
			double syuse=backupCheckStatus.getUsed(system);
			double syfre=backupCheckStatus.getFree(system);
			double syusep=backupCheckStatus.getUsedP(system);
			double syfrep=backupCheckStatus.getFreeP(system);
			addText("SYSTEM\n");
			addText("TOTAL:\t"+
					Convert.decimal(sytot)+
					" bytes\t100,00%\n");
			addText("USED:  "+
					Convert.decimal(syuse)+
					" bytes\t"+
					Convert.percent(syusep)+
					"%\n");
			addText("FREE:\t"+
					Convert.decimal(syfre)+
					" bytes\t"+
					Convert.percent(syfrep)+
					"%\n");
			addText("_____________________________\n");
			if (apuse>=sdfre)
			{
				addText("\n[NO SPACE AVAILABLE TO BACKUP!]\n");
			}
			else
			{
				addText("\n[FREE SPACE AFTER BACKUP: "+
						Convert.decimal(sdfre-
										apuse)+
						" bytes]\n");
			}
		}
		catch(IOException ioe)
		{
			log("Unable to SU Command");
		}
		catch(InterruptedException ie)
		{
			log("Unable to DU Command");
		}
	}

	private boolean isRoot()
	{
		backupSharedPreferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return backupSharedPreferences.getBoolean(	"backup_checkbox",
													false);
	}

	private String customDir()
	{
		backupSharedPreferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return backupSharedPreferences.getString(	"backupdir",
													"PrestoBackup");
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(	"ID1",
							backupStringView1);
		outState.putString(	"ID2",
							backupStringView2);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		setText(savedInstanceState.getString("ID1"));
		setMessage(savedInstanceState.getString("ID2"));
		scrollTo();
	}

	private Runnable	backupUpdateTask	=new Runnable()
												{
													public void run()
													{
														if ((backupProcess!=null))
														{
															if (!isRoot())
															{
																((BackupThread)backupProcess).update();
															}
															setText(((BackupThread)backupProcess).get());
															scrollTo();
															if ((((BackupThread)backupProcess).isWorking().equalsIgnoreCase("WORKING")))
															{
																if (backupBlinking)
																{
																	setMessage("[ Backup is Running. Please wait. ]");
																	backupBlinking=false;
																}
																else
																{
																	setMessage("");
																	backupBlinking=true;
																}
															}
															if ((((BackupThread)backupProcess).isWorking().equalsIgnoreCase("DONE")))
															{
																setMessage("[ Done ]");
																backupProcess.stop();
																backupProcess=null;
																if (backupButton!=null)
																{
																	backupButton.setText("Start Backup");
																	backupButton.setClickable(true);
																	backupSettingsButton.setText("Settings");
																	backupSettingsButton.setClickable(true);
																}
															}
														}
														backupHandler.postDelayed(	this,
																					750);
													}
												};

	@Override
	protected void onStop()
	{
		super.onStop();
		backupHandler.removeCallbacks(backupUpdateTask);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		backupHandler.removeCallbacks(backupUpdateTask);
		backupHandler.postDelayed(	backupUpdateTask,
									1000);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (backupHandler!=null)
		{
			backupHandler.removeCallbacks(backupUpdateTask);
		}
		backupHandler=null;
		if (backupProcess!=null)
		{
			((BackupThread)backupProcess).exit();
			backupProcess=null;
		}
	}

	private Handler	backupHandler	=new Handler();

	@Override
	public void onClick(View v)
	{
		log("Backup Click");
		Button backupButtonLocal=(Button)v.findViewById(R.id.backup_button);
		Button backupSettingsButtonLocal=(Button)v.findViewById(R.id.backup_settings_button);
		if (backupButtonLocal!=null)
		{
			try
			{
				backupProcess=new BackupThread(	isRoot(),
												customDir(),
												this);
			}
			catch(IOException ioe)
			{
				addText("Input/Output Error: \n"+
						ioe);
			}
			backupProcess.start();
			backupButtonLocal.setText("[Working...]");
			backupButtonLocal.setClickable(false);
			backupSettingsButton.setText("[Please Wait...]");
			backupSettingsButton.setClickable(false);
		}
		if (backupSettingsButtonLocal!=null)
		{
			startActivity(new Intent(	this,
										BackupSettings.class));
		}
	}

	private void setText(String message)
	{
		backupTextView1.setText(message);
	}

	private void addText(String message)
	{
		backupTextView1.append(message);
		backupStringView1=message;
	}

	private void setMessage(String message)
	{
		backupTextView2.setText(message);
		backupStringView2=message;
	}

	private void scrollTo()
	{
		backupScrollView.scrollTo(	0,
									backupTextView1.getLineHeight()*
											backupTextView1.getLineCount());
	}

	private void log(String logMessage)
	{
		Logger.log(	"BackupMain",
					logMessage,
					Logger.MAIN_SOFTWARE);
	}
}
