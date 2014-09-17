package isens.hba1c_analyzer;

import java.util.Calendar;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DateActivity extends Activity {

	private TimerDisplay DateTimer;
	
	private enum AddSub {PLUS, MINUS}
	
	private TextView yearText,
					 monthText,
					 dayText;
	private Calendar c;
	
	private Button escBtn,
				   yPlusBtn,
				   yMinusBtn,
				   mPlusBtn,
				   mMinusBtn,
				   dPlusBtn,
				   dMinusBtn;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	private int year,
				month,
				day;
	
	private boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.date);
		
		TimeText  = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		yearText  = (TextView) findViewById(R.id.yeartext);
		monthText = (TextView) findViewById(R.id.monthtext);
		dayText   = (TextView) findViewById(R.id.daytext);
		
		/*Home Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					escBtn.setEnabled(false);
					
					DateSave();
					
					WhichIntent(TargetIntent.SystemSetting);
				}
			}
		});
		
		yPlusBtn = (Button) findViewById(R.id.yplusbtn);
		yPlusBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
				
					YearChange(AddSub.PLUS);
				}
			}
		});
		
//		yPlusBtn.setOnLongClickListener(new View.OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//			
//				switch()
//				
//				return false;
//			}
//		});
		
		yMinusBtn = (Button) findViewById(R.id.yminusbtn);
		yMinusBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {

				if(!btnState) {
					
					btnState = true;
					
					YearChange(AddSub.MINUS);
				}
			}
		});
		
		mPlusBtn = (Button) findViewById(R.id.mplusbtn);
		mPlusBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {

				if(!btnState) {
					
					btnState = true;
					
					MonthChange(AddSub.PLUS);
				}
			}
		});
		
		mMinusBtn = (Button) findViewById(R.id.mminusbtn);
		mMinusBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					MonthChange(AddSub.MINUS);
				}	
			}
		});
		
		dPlusBtn = (Button) findViewById(R.id.dplusbtn);
		dPlusBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					DayChange(AddSub.PLUS);
				}
			}
		});

		dMinusBtn = (Button) findViewById(R.id.dminusbtn);
		dMinusBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					DayChange(AddSub.MINUS);
				}
			}
		});
		
		DateInit();
	}
	
	public void DateInit() {
		
		TimerDisplay.timerState = whichClock.DateClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		GetCurrDate();
	}
		
	public void CurrTimeDisplay() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
		            	TimeText.setText(TimerDisplay.rTime[3] + " " + TimerDisplay.rTime[4] + ":" + TimerDisplay.rTime[5]);
		            }
		        });
		    }
		}).start();	
	}
	
	public void ExternalDeviceDisplay() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		           
		            	if(HomeActivity.ExternalDevice == true) deviceImage.setBackgroundResource(R.drawable.main_usb_c);
		            	else deviceImage.setBackgroundResource(R.drawable.main_usb);
		            }
		        });
		    }
		}).start();
	}
	
	public synchronized void DateDisplay() { // displaying date parameter
		
		yearText.setText(Integer.toString(year));
		monthText.setText(Integer.toString(month));
		dayText.setText(Integer.toString(day));
		
		btnState = false;
	}
	
	public void GetCurrDate() { // acquiring date parameter displayed
		
		c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		
		DateDisplay();
	}
	
	public void GetDate() { // getting the calendar data for date
		
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		
		DateDisplay();
	}
	
	private void YearChange(AddSub i) { // increasing or decreasing the year value one by one
		
		switch(i) {
		
		case PLUS	:
			c.add(Calendar.YEAR, 1);
			break;
			
		case MINUS	:
			c.add(Calendar.YEAR, -1);
			break;
			
		default		:
			break;
		}
		
		GetDate();
	}
	
	private void MonthChange(AddSub i) { // increasing or decreasing the month value one by one
		
		switch(i) {
		
		case PLUS	:
			c.add(Calendar.MONTH, 1);
			break;
			
		case MINUS	:
			c.add(Calendar.MONTH, -1);
			break;
			
		default		:
			break;
		}
		
		GetDate();
	}
	
	private void DayChange(AddSub i) { // increasing or decreasing the day value one by one
		
		switch(i) {
		
		case PLUS	:
			c.add(Calendar.DAY_OF_MONTH, 1);
			break;
			
		case MINUS	:
			c.add(Calendar.DAY_OF_MONTH, -1);
			break;
			
		default		:
			break;
		}
	
		GetDate();
	}
	
	private void DateSave() { // saving the date modified
		
		TimerDisplay.OneHundredmsPeriod.cancel(); // finishing the running timer 
		
		SystemClock.setCurrentTimeMillis(c.getTimeInMillis());

		DateTimer = new TimerDisplay();
		DateTimer.TimerInit(); // starting the timer

		SerialPort.Sleep(1000);
	}
	
	private void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case SystemSetting	:				
			Intent SystemSettingIntent = new Intent(getApplicationContext(), SystemSettingActivity.class);
			startActivity(SystemSettingIntent);
			break;
						
		default		:	
			break;			
		}
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
