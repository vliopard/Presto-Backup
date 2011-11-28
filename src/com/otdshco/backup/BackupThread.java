package com.otdshco.backup;
import java.io.File;
import java.io.IOException;
import android.app.Activity;
import com.otdshco.tools.FileOperations;
import com.otdshco.tools.Logger;
import com.otdshco.tools.PackageProperties;
import com.otdshco.tools.Su;

public class BackupThread extends
		Thread
{
	private String				messageBuffer		="";
	private String				running				="";
	private int					backupLen			=2;
	private String[]			backupFrom			=new String[backupLen];
	private String[]			backupTo			=new String[backupLen];
	private String[]			backupName			=new String[backupLen];
	private boolean				massiveCopyEnabled	=false;
	private PackageProperties	packageProperties;
	private Su					su;
	private FileOperations		fileOperation;
	private Activity			mainBackup;
	private static String		sdcard				="/mnt/sdcard/";

	public BackupThread(boolean mce,
						String backupDir,
						Activity activityBackup) throws IOException
	{
		fileOperation=new FileOperations();
		sdcard=fileOperation.getSDPath(backupDir);
		massiveCopyEnabled=mce;
		mainBackup=activityBackup;
		backupFrom[0]="/data/app";
		backupTo[0]=sdcard+
					"/data_app";
		backupName[0]="App Data";
		backupFrom[1]="/data/app-private";
		backupTo[1]=sdcard+
					"/data_prv";
		backupName[1]="App Private";
		// backupFrom[2]="/system/sd-ext/app";
		// backupTo[2]= sdcard + backupDir+ "/syst_exta";
		// backupName[2]="App Ext";
		// backupFrom[3]="/system/sd-ext/app-private";
		// backupTo[3]= sdcard + backupDir+ "/syst_extp";
		// backupName[3]="App Ext Private";
	}

	private void set(String message)
	{
		messageBuffer=message;
		log(messageBuffer);
	}

	private void add(String message)
	{
		messageBuffer=messageBuffer+
						"\n"+
						message;
		log(messageBuffer);
	}

	public String get()
	{
		return messageBuffer;
	}

	public void update()
	{
		try
		{
			String tmpBuffer=su.getMessage();
			if (tmpBuffer.trim()!="")
			{
				add(tmpBuffer.trim());
			}
		}
		catch(NullPointerException npe)
		{
			log("Backup Update NPE: "+
				npe);
		}
	}

	public String isWorking()
	{
		return running;
	}

	public void run()
	{
		running="WORKING";
		try
		{
			su=new Su(massiveCopyEnabled);
			doBackup();
			add("[ Done ]");
			if (massiveCopyEnabled)
			{
				add("__________________\nBackup done: Verifying...");
				for(int i=0; i<backupLen(); i++)
				{
					if (fileOperation.isDir(backupFrom[i]))
					{
						if (!isEmpty(backupTo[i]))
						{
							showBackup(	backupTo[i],
										"Verifying "+
												backupName[i]+
												"...");
						}
						else
						{
							add(backupTo[i]+
								" is empty.");
						}
					}
					else
					{
						add(backupFrom[i]+
							" does not exist.");
					}
				}
				add("__________________\nVerification done.");
			}
			else
			{
				add("[ - Massive Copy Disabled - ]\n[ No verification is needed ]");
			}
			su._exit();
		}
		catch(IOException ioe)
		{
			add("\n\nBACKUP STOPPED. ACTION INCOMPLETE!");
			add("run(IOException):\n"+
				ioe);
		}
		catch(InterruptedException ie)
		{
			add("run(IntException):\n"+
				ie);
		}
		running="DONE";
	}

	private void showBackup(String sourceDirectory,
							String message) throws NullPointerException
	{
		log("showBackup");
		add("==========================\n"+
			message);
		add("Checking: "+
			sourceDirectory);
		int index=1;
		File directory=new File(sourceDirectory);
		for(File file : directory.listFiles())
		{
			if (file.isFile())
			{
				add(file.getName());
				packageProperties=new PackageProperties(file.getAbsolutePath(),
														mainBackup);
				add("["+
					packageProperties.getPackage()+
					"] ["+
					packageProperties.getName()+
					" "+
					packageProperties.getVersion()+
					"] ["+
					index+
					"].apk");
				index++;
			}
		}
	}

	private int backupLen()
	{
		return backupLen;
	}

	private void doBackup()	throws IOException,
							InterruptedException
	{
		log("doBackup");
		set("Backup started:\n__________________");
		if (Su.CODE_OK!=su._mkdir(sdcard))
		{
			add("Cannot Create Directory "+
				sdcard);
		}
		for(int i=0; i<backupLen(); i++)
		{
			if (fileOperation.isDir(backupFrom[i]))
			{
				if (Su.CODE_OK!=su._mkdir(backupTo[i]))
				{
					add("Cannot Create Directory "+
						backupTo[i]);
				}
				add("===============================\nBacking up "+
					backupName[i]+
					"...\n===============================");
				log("doBackup [_cp IN] BAKLEN["+
					backupLen()+
					"] CURRENT["+
					i+
					"]");
				int errorCode=su._cp(	backupFrom[i]+
												"/*",
										backupTo[i]);
				log("doBackup [_cp OUT] BAKLEN["+
					backupLen()+
					"] CURRENT["+
					i+
					"]");
				if (Su.CODE_OK!=errorCode)
				{
					if (Su.CODE_EMPTY==errorCode)
					{
						add(backupFrom[i]+
							" is empty.");
					}
					else
					{
						add("Can't copy from "+
							backupFrom[i]+
							"\nto "+
							backupTo[i]+
							"\n["+
							Su.CODE_LIST[errorCode]+
							"]");
					}
				}
			}
			else
			{
				add("Source Directory Does Not Exist");
			}
		}
	}

	private boolean isEmpty(String sourceDirectory)	throws IOException,
													InterruptedException
	{
		log("isEmpty "+
			sourceDirectory);
		if (su._size(sourceDirectory)==0)
		{
			log("isEmpty [TRUE]");
			return true;
		}
		log("isEmpty [FALSE]");
		return false;
	}

	public void exit()
	{
		log("exit");
		try
		{
			log("exit [stopWork]");
			su.stopWork();
			log("exit [_exit]");
			su._exit();
		}
		catch(InterruptedException ie)
		{
			log("exit [Interrupted Exception]");
		}
		catch(IOException ioe)
		{
			log("exit [Input Output Exception]");
		}
		catch(NullPointerException npe)
		{
			log("exit [Null Pointer Exception]");
		}
	}

	private void log(String logMessage)
	{
		Logger.log(	"BackupThread",
					logMessage,
					Logger.MAIN_THREAD);
	}
}
