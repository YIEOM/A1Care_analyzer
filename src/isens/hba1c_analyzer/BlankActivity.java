package isens.hba1c_analyzer;

import java.util.Timer;
import java.util.TimerTask;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.RunActivity.AnalyzerState;
import isens.hba1c_analyzer.RunActivity.CartDump;
import isens.hba1c_analyzer.RunActivity.CheckCoverError;
import isens.hba1c_analyzer.SystemCheckActivity.MotorCheck;
import isens.hba1c_analyzer.SystemCheckActivity.TemperatureCheck;
import isens.hba1c_analyzer.Model.ActivityChange;
import isens.hba1c_analyzer.View.FunctionalTestActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BlankActivity extends Activity {

	public SerialPort mSerialPort;
	public ErrorPopup mErrorPopup;
	public TimerDisplay mTimerDisplay;
	public ActivityChange mActivityChange;
	
	public Handler runHandler = new Handler();
	public Timer runningTimer;
	
	public Button escIcon;
	
	public ImageView warning;

	private RunActivity.AnalyzerState blankState;
	private byte photoCheck;
	
	private int checkError = RunActivity.NORMAL_OPERATION;

	public boolean btnState = false;
	
	public byte runSec = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.blank);
		
		mErrorPopup = new ErrorPopup(this, this, R.id.blanklayout);
		
		BlankInit();
	}                     
	
	public void setButtonId() {
		
		escIcon = (Button)findViewById(R.id.escicon);
	}
	
	public void setButtonClick() {
		
		escIcon.setOnTouchListener(mTouchListener);
	}
	
	public void setButtonState(int btnId, boolean state) {
		
		findViewById(btnId).setEnabled(state);
	}
	
	Button.OnTouchListener mTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			switch(event.getAction()) {
			
			case MotionEvent.ACTION_UP	:
				
				if(!btnState) {

					btnState = true;
					
					switch(v.getId()) {
				
					case R.id.escicon		:
						ESC();
						btnState = false;
						break;
							
					default	:
						break;
					}
				}
			
				break;
			}
			
			return false;
		}
	};
	
	public void enabledAllBtn() {

		setButtonState(R.id.escicon, true);
	}
	
	public void unenabledAllBtn() {
		
		setButtonState(R.id.escicon, false);
		
		btnState = false;
	}
	
	public void BlankInit() {
		
		setButtonId();
		unenabledAllBtn();
		setButtonClick();
		
		RunActivity.IsStop = false;
		RunActivity.IsError = false;
		ActionActivity.IsEnablePopup = false;
		
		mTimerDisplay = new TimerDisplay();
		mTimerDisplay.ActivityParm(this, R.id.blanklayout);
				
		mSerialPort = new SerialPort();
		
		blankState = RunActivity.AnalyzerState.InitPosition;
		photoCheck = 0;
		
		RunTimerInit(this);
		
		TimerDisplay.RXBoardFlag = true;
		SensorCheck SensorCheckObj = new SensorCheck(this, this, R.id.blanklayout);
		SensorCheckObj.start();
	}
	
	public class SensorCheck extends Thread {
		
		Activity activity;
		Context context;
		int layoutid;
		
		public SensorCheck(Activity activity, Context context, int layoutid) {
			
			this.activity = activity;
			this.context = context;
			this.layoutid = layoutid;
		}
		
		public void run() {
			
			GpioPort.DoorActState = true;			
			GpioPort.CartridgeActState = true;
			
			SerialPort.Sleep(2000);
			
			while(ActionActivity.DoorCheckFlag != 1 || ActionActivity.CartridgeCheckFlag != 0) {
			
				if(ActionActivity.CartridgeCheckFlag != 0) mErrorPopup.ErrorDisplay(R.string.w002);
				else if(ActionActivity.DoorCheckFlag != 1) mErrorPopup.ErrorDisplay(R.string.w001);
				
				SerialPort.Sleep(100);
			}
			mErrorPopup.ErrorPopupClose();
			
			GpioPort.DoorActState = false;
			GpioPort.CartridgeActState = false;
			
			new Thread(new Runnable() {
				public void run() {
					runOnUiThread(new Runnable(){
						public void run() {

							enabledAllBtn();
						}
					});
				}
			}).start();
			
			BlankStep BlankStepObj = new BlankStep();
			BlankStepObj.start();
		}
	}
	
	public class BlankStep extends Thread { // Blank run
		
		public void run() {
			
			CartDump mCartDump = new CartDump();
			
			for(int i = 0; i < 9; i++) {
			
				checkMode();
				
				switch(blankState) {
				
				case InitPosition		:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.NormalSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.MeasurePosition, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 10);
					break; 
				
				case MeasurePosition :
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.NormalSet);			
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.FilterDark, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 10);
					break;
					
				case FilterDark :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 10);
					RunActivity.BlankValue[0] = 0;
					RunActivity.BlankValue[0] = AbsorbanceMeasure(SystemCheckActivity.MinDark, SystemCheckActivity.MaxDark, SystemCheckActivity.ERROR_DARK); // Dark Absorbance

					/* TEST Mode */
					if(HomeActivity.ANALYZER_SW == HomeActivity.NORMAL)
						
					PhotoErrorCheck();
					break;
					
				case Filter535nm :
					/* 535nm filter Measurement */
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 10);
					RunActivity.BlankValue[1] = AbsorbanceMeasure(SystemCheckActivity.Min535, SystemCheckActivity.Max535, SystemCheckActivity.ERROR_535nm); // Dark Absorbance
					break;
				
				case Filter660nm :
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 10);
					RunActivity.BlankValue[2] = AbsorbanceMeasure(SystemCheckActivity.Min660, SystemCheckActivity.Max660, SystemCheckActivity.ERROR_660nm); // Dark Absorbance
					break;
				
				case Filter750nm :
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 10);
					RunActivity.BlankValue[3] = AbsorbanceMeasure(SystemCheckActivity.Min750, SystemCheckActivity.Max750, SystemCheckActivity.ERROR_750nm); // Dark Absorbance
				
					/* TEST Mode */
					if(HomeActivity.ANALYZER_SW == HomeActivity.NORMAL)
					
					PhotoErrorCheck();
					break;
				
				case FilterHome :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 10);
					break;
				
				case CartridgeHome :
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 10);
					break;
					
				case NormalOperation	:
					SerialPort.Sleep(1000);
					WhichIntent(TargetIntent.Action);
					break;
				
				case ShakingMotorError	:
					checkError = R.string.e211;
					blankState = AnalyzerState.NoWorking;
					WhichIntent(TargetIntent.Home);
					break;
					
				case FilterMotorError	:
					checkError = R.string.e212;
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.NormalSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 10);
					WhichIntent(TargetIntent.Home);
					break;
				
				case PhotoSensorError	:
					blankState = AnalyzerState.NoWorking;
					mCartDump.start();
					break;
					
				case LampError			:
					checkError = R.string.e232;
					blankState = AnalyzerState.NoWorking;
					mCartDump.start();
					break;
					
				case NoResponse :
					checkError = R.string.e241;
					blankState = AnalyzerState.NoWorking;
					WhichIntent(TargetIntent.Home);
					break;
					
				case Stop		:
					checkError = R.string.stop;
					blankState = AnalyzerState.NoWorking;
					break;
					
				case ErrorCover	:
					checkError = R.string.e322;
					blankState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}

			if(checkError == R.string.e322) {
				
				mErrorPopup.ErrorDisplay(R.string.w004);
				CheckCoverError mCheckCoverError = new CheckCoverError();
				mCheckCoverError.start();
			
			} else if(checkError == R.string.stop) {
				
				mCartDump.start();
			}
		}
	}
	
	public void MotionInstruct(String str, SerialPort.CtrTarget target) { // Motion of system instruction
		
		mSerialPort.BoardTx(str, target);
	}
	
	public synchronized double AbsorbanceMeasure(double min, double max, byte errBits) { // Absorbance measurement
	
		int time = 0;
		String rawValue;
		double douValue = 0;
		
		mSerialPort.BoardTx("VH", SerialPort.CtrTarget.NormalSet);
		
		do {
		
			rawValue = mSerialPort.BoardMessageOutput();			
			
			if(time++ > 50)	break;
			
			if(RunActivity.IsError || RunActivity.IsStop) break;
			
			SerialPort.Sleep(100);
		
		} while(rawValue.length() != 8);	
		
		try {
			
			douValue = Double.parseDouble(rawValue);
			
			if((min > douValue) || (douValue > max)) photoCheck += errBits;
			
		} catch(NumberFormatException e) {
			
			douValue = 0.0;
			
			blankState = AnalyzerState.NoResponse;
			checkError = R.string.e241;
		}
		
		return (douValue - RunActivity.BlankValue[0]);
	}
	
	public void PhotoErrorCheck() {
		
		switch(photoCheck) {
		
		case 1	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = R.string.e231;
			break;
			
		case 2	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = R.string.e233;
			break;
			
		case 4	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = R.string.e234;
			break;
			
		case 8	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = R.string.e235;
			break;
			
		case 14	:
			blankState = AnalyzerState.LampError;
			checkError = R.string.e232;
			break;
			
		default	:
			break;
		}
	}
	
	public void BoardMessage(String colRsp, AnalyzerState nextState, String errRsp, AnalyzerState errState, int rspTime) {
		
		int time = 0;
		String temp = "";
		
		rspTime = rspTime * 10;
		
		while(true) {
			
			temp = mSerialPort.BoardMessageOutput();
			
			if(colRsp.equals(temp)) {
				
				blankState = nextState;
				break;
			
			} else if(errRsp.equals(temp)) {
				
				blankState = errState;
				break;
			}
			
			if(time++ > rspTime) {
				
				blankState = AnalyzerState.NoResponse;
				checkError = R.string.e241;
				break;
			}
			
			if(RunActivity.IsError || RunActivity.IsStop) break;
			
			SerialPort.Sleep(100);
		}
	}
	
	public void checkMode() {
		
		if(RunActivity.IsError) {
				
			blankState = AnalyzerState.ErrorCover;
		
		} else if(!RunActivity.IsError && RunActivity.IsStop) {
			
			blankState = AnalyzerState.Stop;
		}
	}
	
	public class CheckCoverError extends Thread {
		
		public void run() {
			
			GpioPort.DoorActState = true;			
			GpioPort.CartridgeActState = true;
			
			SerialPort.Sleep(2000);
			
			while(ActionActivity.DoorCheckFlag != 1) SerialPort.Sleep(100);
			
			GpioPort.DoorActState = false;			
			GpioPort.CartridgeActState = false;
			
			mErrorPopup.ErrorDisplay(R.string.wait);
			
			CartDump mCartDump = new CartDump();
			mCartDump.start();
		}
	}
	
	public class CartDump extends Thread { // Cartridge dumping motion
		
		public void run() {
				
			RunActivity.IsError = false;
			RunActivity.IsStop = false;
			blankState = AnalyzerState.FilterDark;
			
			CartDump mCartDump = new CartDump();
			
			for(int i = 0; i < 3; i++) {
				
				checkMode();
				
				switch(blankState) {
				
				case FilterDark	:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 10);
					break;
					
				case CartridgeHome	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.NormalSet);
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 10);
					break;
					
				case ShakingMotorError	:
					checkError = R.string.e211;
					blankState = AnalyzerState.NoWorking;
					WhichIntent(TargetIntent.Home);
					break;
					
				case FilterMotorError	:
					checkError = R.string.e212;
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.NormalSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 10);
					WhichIntent(TargetIntent.Home);
					break;
					
				case LampError			:
					checkError = R.string.e232;
					mCartDump.start();
					break;
					
				case NoResponse :
					blankState = AnalyzerState.NoWorking;
					WhichIntent(TargetIntent.Home);
					break;
					
				case Stop		:
					checkError = R.string.stop;
					blankState = AnalyzerState.NoWorking;
					mCartDump.start();
					break;
					
				case ErrorCover	:
					checkError = R.string.e322;
					blankState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(blankState == AnalyzerState.NormalOperation) {
			
				mErrorPopup.ErrorPopupClose();
				
				switch(HomeActivity.MEASURE_MODE) {
				
				case HomeActivity.A1C	:
					WhichIntent(TargetIntent.Home);
					break;
					
				case HomeActivity.A1C_QC	:
					WhichIntent(TargetIntent.FunctionalTest);
					break;
					
				default	:
					break;
				}
			
			} else if(checkError == R.string.e322) {
				
				mErrorPopup.ErrorDisplay(R.string.w004);
				CheckCoverError mCheckCoverError = new CheckCoverError();
				mCheckCoverError.start();
			}
		}
	}
	
	public void RunTimerInit(final Activity activity) {

		TimerTask OneSecondPeriod = new TimerTask() {
			
			public void run() {
				Runnable updater = new Runnable() {
					public void run() {
						
						WariningDisplay(activity);
					}
				};
				
				runHandler.post(updater);		
			}
		};
		
		runningTimer = new Timer();
		runningTimer.schedule(OneSecondPeriod, 0, 1000); // Timer period : 100msec
	}
	
	public void WariningDisplay(Activity activity) { // Display running time
		
		warning = (ImageView) activity.findViewById(R.id.warning);
		
		if(runSec++ % 2 == 1) warning.setBackgroundResource(R.drawable.blank_snr_1);
		else warning.setBackgroundResource(R.drawable.blank_snr_2);
	}
	
	public void ESC() {
		
		mErrorPopup.OXBtnDisplay(R.string.esc);
	}
	
	public void BlankStop() {
		
		RunActivity.IsStop = true;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		TimerDisplay.RXBoardFlag = false;
		
		Intent nextIntent = null;
		
		switch(Itn) {
		
		case Home	:				
			nextIntent = new Intent(getApplicationContext(), HomeActivity.class);
			nextIntent.putExtra("System Check State", (int) checkError);
			break;
			
		case Action	:				
			nextIntent = new Intent(getApplicationContext(), ActionActivity.class);
			break;
			
		case FunctionalTest	:				
			nextIntent = new Intent(getApplicationContext(), FunctionalTestActivity.class);
			break;
			
		default			:	
			break;
		}		
		
		startActivity(nextIntent);
		finish();
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
