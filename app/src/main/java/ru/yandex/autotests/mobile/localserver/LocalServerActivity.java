package ru.yandex.autotests.mobile.localserver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import de.fun2code.android.pawserver.PawServerActivity;
import de.fun2code.android.pawserver.PawServerService;
import de.fun2code.android.pawserver.listener.ServiceListener;
import de.fun2code.android.pawserver.util.Utils;
import ru.yandex.autotests.mobile.localserver.buildownpawserver.R;

/**
 * Sample "Build your own PAW server" Activity.
 *
 *
 */
public class LocalServerActivity extends PawServerActivity implements ServiceListener {
	@SuppressWarnings("unused")
	private Handler handler;
	
	// View that displays the server URL
	private TextView viewUrl;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		TAG = "BuildOwnPawServer";
		
		/*
		 * Defines the installation directory.
		 */
		// Use /data/data/... directory
		//INSTALL_DIR = getFilesDir().getAbsolutePath() + "/www";
		
		// Use sdcard
		INSTALL_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				+ "/www";

		/*
		 * Turn the PawServerActivity into runtime mode.
		 * Otherwise an error may occur if some things special to the
		 * original PAW server are not available.
		 */
		calledFromRuntime = true;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		handler = new Handler();
		
		// URL TextView
		viewUrl = (TextView) findViewById(R.id.url);

		/* Check installation and extract ZIP if necessary */
		checkInstallation();

		/*
		 * Register handler This is needed in order to get dialogs etc. to work.
		 */
		messageHandler = new MessageHandler(this);
		LocalServerService.setActivityHandler(messageHandler);

		/*
		 * Register activity with service.
		 */
		LocalServerService.setActivity(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		/*
		 *  Registers the listener that calls onServiceStart() and
		 *  onServiceStop().
		 */
		LocalServerService.registerServiceListener(this);
		startService();
	}

	@Override
	public void onBackPressed(){
		moveTaskToBack(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService();
		
		/*
		 * Unregisters the listener
		 */
		LocalServerService.unregisterServiceListener(this);

	}

	/**
	 * Stops the service
	 */
	@Override
	public void stopService() {
		Intent serviceIntent = new Intent(this.getApplicationContext(),
				LocalServerService.class);
		stopService(serviceIntent);
	}

	/**
	 * Starts the service
	 */
	@Override
	public void startService() {
		/*
		 * Do nothing, if service is already running.
		 */
		if (LocalServerService.isRunning()) {
			return;
		}

		Intent serviceIntent = new Intent(LocalServerActivity.this,
				LocalServerService.class);

		startService(serviceIntent);
	}

	/**
	 * Called when the service has been started
	 * 
	 * @param success <code>true</code> if service was started successfully, 
	 * 					otherwise <code>false</code>
	 */
	@Override
	public void onServiceStart(boolean success) {
		if (success) {
			// Display URL
			PawServerService service = LocalServerService.getService();
			final String url = service.getPawServer().server.protocol
					+ "://" + Utils.getLocalIpAddress() + ":"
					+ service.getPawServer().serverPort;
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					viewUrl.setText("Server running on: " +  url);	
				}
			});
			
		}
		else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					viewUrl.setText("Server could not be started!");
				}
			});
		}

	}
	
	/**
	 * Called when the service has been stopped
	 * 
	 * @param success <code>true</code> if service was started successfully, 
	 * 					otherwise <code>false</code>
	 */
	@Override
	public void onServiceStop(boolean success) {
		
	}

	/**
	 * Checks the installation and extracts the content.zip file
	 * to INSTALL_DIR if needed
	 */
	private void checkInstallation() {
		if(!new File(INSTALL_DIR).exists()) {
			new File(INSTALL_DIR).mkdirs();
		}

		// Files not to overwrite
		HashMap<String, Integer> keepFiles = new HashMap<String, Integer>();

		// Extract ZIP file form assets
		try {
			extractZip(getAssets().open("content.zip"),
					INSTALL_DIR, keepFiles);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

}