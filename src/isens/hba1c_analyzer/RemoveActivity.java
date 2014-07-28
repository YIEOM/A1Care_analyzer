package isens.hba1c_analyzer;

import java.lang.annotation.Target;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class RemoveActivity extends Activity {

	SerialPort RemoveSerial;
	
	public AnimationDrawable RemoveAni;
	public ImageView RemoveImage;
	
	public static TextView TimeText;
	public static int PatientDataCnt,
					  ControlDataCnt;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.remove);

		RemoveSerial = new SerialPort();
		
		TimeText = (TextView) findViewById(R.id.timeText);
		
		RemoveInit();
	}
	
	public void RemoveInit() {
		
		TimerDisplay.timerState = whichClock.RemoveClock;		
		CurrTimeDisplay();
		
		UserAction UserActionObj = new UserAction();
		UserActionObj.start();
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
	
	public class UserAction extends Thread {
		
		public void run() {
			
			int whichIntent;
			
			User1stAction();
			
			GpioPort.DoorActState = true;
			GpioPort.CartridgeActState = true;
	
			while(ActionActivity.CartridgeCheckFlag != 0);
			
			while((ActionActivity.DoorCheckFlag != 1) | (ActionActivity.CartridgeCheckFlag != 0));
			
			GpioPort.DoorActState = false;
			GpioPort.CartridgeActState = false;
			
			Intent itn = getIntent();
			whichIntent = itn.getIntExtra("WhichIntent", 0);
			
			if(whichIntent != HomeActivity.COVER_ACTION_ESC) {
					 
				if(Barcode.RefNum.substring(0, 1).equals("C")) {
					
					ControlDataCnt = itn.getIntExtra("DataCnt", 0);	
				
				} else if(Barcode.RefNum.substring(0, 1).equals("H")) {
					
					PatientDataCnt = itn.getIntExtra("DataCnt", 0);
				}
				
				DataCntSave();			
			}
			
			RemoveAni.stop();
			
			switch(whichIntent) {
			
			case HomeActivity.ACTION_ACTIVITY	:
				WhichIntent(TargetIntent.Blank);
				break;
			
			case HomeActivity.HOME_ACTIVITY		:	
				WhichIntent(TargetIntent.Home);
				break;
				
			case HomeActivity.COVER_ACTION_ESC	:
				WhichIntent(TargetIntent.Home);
				break;
				
			default	:
				break;
			}
		}
	}
	
	public void User1stAction() { // Cartridge remove animation start
		
		RemoveImage = (ImageView)findViewById(R.id.removeAct1);
		RemoveAni = (AnimationDrawable)RemoveImage.getBackground();
		
//		new Thread(new Runnable() {
//		    public void run() {    
//		        runOnUiThread(new Runnable(){
//		            public void run() {
		            	
		            	RemoveAni.start();
//		            }
//		        });
//		    }
//		}).start();	
	}
	
	public void DataCntSave() { // Saving data number
		
		SharedPreferences DcntPref = getSharedPreferences("Data Counter", MODE_PRIVATE);
		SharedPreferences.Editor edit = DcntPref.edit();
		
		edit.putInt("PatientDataCnt", PatientDataCnt);
		edit.putInt("ControlDataCnt", ControlDataCnt);
		
		edit.commit();
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		SerialPort.Sleep(1000);		
		
		switch(Itn) {
		
		case Home		:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			break;
						
		case Blank		:				
			Intent BlankIntent = new Intent(getApplicationContext(), BlankActivity.class);
			startActivity(BlankIntent);
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
