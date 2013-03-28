package com.project.sushi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
import android.view.View.OnClickListener;
import com.project.sushi.R;
 
public class Options extends Activity {
 
	Button button;
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);
		addListenerOnButton();
	}
 
	public void onSpeedRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.slow:
	            if (checked)
	                MainActivity.slow = true;
	            break;
	        case R.id.fast:
	            if (checked)
	            	MainActivity.fast = true;
	            break;
	    }
	}
	
	public void onSizeRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.small:
	            if (checked)
	            	CuttingBoard.offset -= 40;
	            	break;
	       case R.id.large:
	            if (checked)
	            	CuttingBoard.offset += 25;
	            break;
	    }
	}
	
	public void addListenerOnButton() {
 
		final Context context = this;
 
		button = (Button) findViewById(R.id.slice);
 
		button.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
 
			    Intent intent = new Intent(context, MainActivity.class);
			    finish();
			    			startActivity(intent);   
 
			}
 
		});
 
	}
 
}