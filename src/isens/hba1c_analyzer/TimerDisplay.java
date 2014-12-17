package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimerDisplay {
	
	public Handler handler = new Handler();
	public static TimerTask OneHundredmsPeriod;

	final static byte FILE_CLOSE 	= 0,
			  		  FILE_OPEN 	= 1,
			  		  FILE_NOT_OPEN = 2;
	
	public static byte ExternalDeviceBarcode = FILE_CLOSE;
	
	public static String rTime[] = new String[8];
	
	public TextView timeText;
	public ImageView deviceImage;
	
	public static Activity activity;
	
	public Timer timer;

	public GpioPort mGpioPort;
	public SerialPort mSerialPort;
	
//	public void TimerInit() {
//		
//		mSerialPort = new SerialPort(0);
//		mGpioPort = new GpioPort();
//		
//		OneHundredmsPeriod = new TimerTask() {
//			
//			int cnt = 0;
//			
//			public void run() {
//				Runnable updater = new Runnable() {
//					public void run() {
//						
//						if(cnt++ == 100) cnt = 0;
//						
//						if((cnt % 10) == 0) { // One second period
//						
//							mGpioPort.CartridgeSensorScan();
//							mGpioPort.DoorSensorScan();
//						
//							RealTime();
//							
//							if(Integer.parseInt(rTime[6]) == 0) { // Whenever 00 second
//											
//								CurrTimeDisplay();
//							}
//
//							ExternalDeviceCheck();
//							
//						} else if((cnt % 2) == 0) {
//							
//							mGpioPort.CartridgeSensorScan();
//							mGpioPort.DoorSensorScan();
//						}
//					}
//				};
//				
//				handler.post(updater);		
//			}
//		};
//		
//		timer = new Timer();
//		timer.schedule(OneHundredmsPeriod, 0, 100); // Timer period : 100msec
//	}
	public static boolean RXBoardFlag = false;
	
	public void TimerInit() {
		
		mSerialPort = new SerialPort(0);
		mGpioPort = new GpioPort();
		
		Log.w("TimerInit", "run");
		
		OneHundredmsPeriod = new TimerTask() {
			
			int cnt = 0;
			
			public void run() {
				Runnable updater = new Runnable() {
					public void run() {
						
						if(cnt++ == 1000) cnt = 0;
						
						if(((cnt % 5) == 0) & RXBoardFlag) mSerialPort.BoardRxData2();
						
						if((cnt % 100) == 0) { // One second period
						
							mGpioPort.CartridgeSensorScan();
							mGpioPort.DoorSensorScan();
						
							RealTime();
							
							if(Integer.parseInt(rTime[6]) == 0) { // Whenever 00 second
											
								CurrTimeDisplay();
							}

							ExternalDeviceCheck();
							
						} else if((cnt % 20) == 0) {
							
							mGpioPort.CartridgeSensorScan();
							mGpioPort.DoorSensorScan();
						}
					}
				};
				
				handler.post(updater);		
			}
		};
		
		timer = new Timer();
		timer.schedule(OneHundredmsPeriod, 0, 10); // Timer period : 10msec
	}
	
	public void ExternalDeviceCheck() {
		
		try {
			
		    boolean isConnect = false;
		    
			Process shell = Runtime.getRuntime().exec("/system/bin/busybox lsusb");

			BufferedReader br = new BufferedReader(new InputStreamReader(shell.getInputStream()));
			String line = "";
			
			while((line = br.readLine()) != null) {
			
				if(line.substring(23).equals("0483:5740")) {
					
					isConnect = true;
					
					if(ExternalDeviceBarcode != FILE_OPEN) {
					
						Log.w("shell", "line : " + line);
						
						mSerialPort.HHBarcodeSerialInit();
						mSerialPort.HHBarcodeRxStart();
						
						ExternalDeviceDisplay(); 
					}
				}
			}
			
			br.close(); 

			if(isConnect == false) {
			
				if(ExternalDeviceBarcode == FILE_OPEN) {
					
					SerialPort.Sleep(500);
					
					ExternalDeviceBarcode = FILE_CLOSE;
					
					ExternalDeviceDisplay();
				}
			}
			
		} catch (IOException e) {
	
			throw new RuntimeException(e);
		}
	}
	
	public void RealTime() { // Get current date and time
	
		Calendar c = Calendar.getInstance();
		
		DecimalFormat dfm = new DecimalFormat("00");
		
		rTime[0] = Integer.toString(c.get(Calendar.YEAR));
		rTime[1] = dfm.format(c.get(Calendar.MONTH) + 1);
		rTime[2] = dfm.format(c.get(Calendar.DAY_OF_MONTH));
		
		if(c.get(Calendar.AM_PM) == 0) {

			rTime[3] = "AM";			
		} else {

			rTime[3] = "PM";			
		}
		
		if(c.get(Calendar.HOUR) != 0) {
			
			rTime[4] = Integer.toString(c.get(Calendar.HOUR));
		} else {
		
			rTime[4] = "12";
		}
		rTime[5] = dfm.format(c.get(Calendar.MINUTE));		
		rTime[6] = dfm.format(c.get(Calendar.SECOND));
		rTime[7] = dfm.format(c.get(Calendar.HOUR_OF_DAY));		
	}
	
	public void ActivityParm(Activity act, int id) {
		
		activity = act;
		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
	}
	
	public void CurrTimeDisplay() {
		
		if(activity != null) {
			
			timeText = (TextView) activity.findViewById(R.id.timeText);
			
			timeText.setText(rTime[3] + " " + rTime[4] + ":" + rTime[5]);
		}
	}
	
	public void ExternalDeviceDisplay() {
		
		deviceImage = (ImageView) activity.findViewById(R.id.device);
		
    	if(ExternalDeviceBarcode == FILE_OPEN) deviceImage.setBackgroundResource(R.drawable.main_usb_c);
    	else deviceImage.setBackgroundResource(R.drawable.main_usb);
	}
}
