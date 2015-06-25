package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class RecordActivity extends Activity {

	final static byte CONTROL = 1,
					  PATIENT = 2;
	
	public TimerDisplay mTimerDisplay;
	
	private Activity activity;
	
	private Button patientBtn,
				   controlBtn,
				   backIcon;
	
	public static int DataPage;
	
	public boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.record);			
		
		MemoryInit();
	}	
	
	public void setButtonId(Activity activity) {
		
		patientBtn = (Button)activity.findViewById(R.id.patientbtn);
		controlBtn = (Button)activity.findViewById(R.id.controlbtn);
		backIcon = (Button)activity.findViewById(R.id.backicon);
	}
	
	public void setButtonClick() {
		
		patientBtn.setOnTouchListener(mTouchListener);
		controlBtn.setOnTouchListener(mTouchListener);
		backIcon.setOnTouchListener(mTouchListener);
	}
	
	public void setButtonState(int btnId, boolean state, Activity activity) {
		
		activity.findViewById(btnId).setEnabled(state);
	}
	
	Button.OnTouchListener mTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			switch(event.getAction()) {
			
			case MotionEvent.ACTION_UP	:
				unenabledAllBtn(activity);
				
				switch(v.getId()) {
				
				case R.id.patientbtn	:
					WhichIntent(TargetIntent.PatientFileLoad);
					break;
					
				case R.id.controlbtn	:
					WhichIntent(TargetIntent.ControlFileLoad);
					break;
				
				case R.id.backicon		:
					WhichIntent(TargetIntent.Home);
					break;
			
				default	:
					break;
				}
			
				break;
			}
			
			return false;
		}
	};
	
	public void enabledAllBtn(Activity activity) {

		setButtonState(R.id.patientbtn, true, activity);
		setButtonState(R.id.controlbtn, true, activity);
		setButtonState(R.id.backicon, true, activity);
	}
	
	public void unenabledAllBtn(Activity activity) {
		
		setButtonState(R.id.patientbtn, false, activity);
		setButtonState(R.id.controlbtn, false, activity);
		setButtonState(R.id.backicon, false, activity);
	}
	
	public void MemoryInit() {

		activity = this;
		
		setButtonId(activity);
		setButtonClick();
		
		mTimerDisplay = new TimerDisplay();
		mTimerDisplay.ActivityParm(this, R.id.memorylayout);
		
		DataPage = 0;
	}

	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		Intent nextIntent = null;
		
		switch(Itn) {
		
		case Home				:
			nextIntent = new Intent(getApplicationContext(), HomeActivity.class);
			break;
			
		case ControlFileLoad	:
			nextIntent = new Intent(getApplicationContext(), FileLoadActivity.class);
			nextIntent.putExtra("DataCnt", RemoveActivity.ControlDataCnt); // delivering recent data number
			nextIntent.putExtra("DataPage", DataPage);
			nextIntent.putExtra("Type", (int) CONTROL);
			break;
			
		case PatientFileLoad	:
			nextIntent = new Intent(getApplicationContext(), FileLoadActivity.class);
			nextIntent.putExtra("DataCnt", RemoveActivity.PatientDataCnt); // delivering recent data number
			nextIntent.putExtra("DataPage", DataPage);
			nextIntent.putExtra("Type", (int) PATIENT);
			break;
			
		default					:
			break;			
		}
		
		startActivity(nextIntent);
		finish();		
	}
	
	public void finish() {
		
		super.finish();
	}
}
