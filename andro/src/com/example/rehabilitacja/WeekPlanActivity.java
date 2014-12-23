package com.example.rehabilitacja;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.rehabilitacja.klasy.Cwiczenie;
import com.example.rehabilitacja.klasy.UserFunctions;

public class WeekPlanActivity extends ActionBarActivity {
	
	final Context context = this;
	@SuppressLint("UseSparseArrays") HashMap<Integer, List<Cwiczenie>> hm = new HashMap<Integer, List<Cwiczenie>>();
	List<LinearLayout> layoutList = new ArrayList<LinearLayout>();

	private String uid, sid;
	private int id = 5;
	private boolean error = false;
	private String error_msg = null;
	private static JSONObject jObj = null;
	LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5;
	
	private ViewFlipper viewFlipper;
    private float lastX;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_week_plan);
		
		Bundle bb = getIntent().getExtras();
		uid=bb.getString("uid");
		sid=bb.getString("sid");
		layoutList.add((LinearLayout) findViewById(R.id.linearLay1));
		layoutList.add((LinearLayout) findViewById(R.id.linearLay2));
		layoutList.add((LinearLayout) findViewById(R.id.linearLay3));
		layoutList.add((LinearLayout) findViewById(R.id.linearLay4));
		layoutList.add((LinearLayout) findViewById(R.id.linearLay5));
		new nowyWatek().execute();
		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.week_plan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class nowyWatek extends AsyncTask<Void,Void,Void>{

		final Dialog dialog = new Dialog(context);
		Button zamknij;
		TextView pleaseWait;
		
		@Override
		protected void onPreExecute(){
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	dialog.setContentView(R.layout.activity_login_dialog);
	    	//dialog.setTitle("Logowanie");
	    	zamknij = (Button) dialog.findViewById(R.id.zamknijButton);
	    	zamknij.setVisibility(View.INVISIBLE);
	    	pleaseWait = (TextView) dialog.findViewById(R.id.pleaseWait);
	    	pleaseWait.setText("Loading");
	    	dialog.show();	
	    	
		}
		
		@Override
		protected Void doInBackground(Void... params){
			
			String odpowiedz = UserFunctions.getWeekPlan(uid, sid);
			
			String[] treatment_days = odpowiedz.split("--");
			try {
				jObj = new JSONObject(treatment_days[0]); 
				if("0".equals(jObj.getString("success"))){
					error = true;
					error_msg = jObj.getString("error_msg");
					return null;
				}
	        } catch (JSONException e) {
	           // Log.e("JSONParserTreningAct", "Error parsing data " + e.toString());
	        }
			for(int i=1;i<treatment_days.length;i++){
				String day = treatment_days[0];
				String[] tablica = day.split("-");
				for(String tmp : tablica){
					try {
						jObj = new JSONObject(tmp); 
			        } catch (JSONException e) {
			            Log.e("JSONParserTreningAct", "Error parsing data " + e.toString());
			            break;
			        }
					
					try {
						List<Cwiczenie> lista = new ArrayList<Cwiczenie>();
						lista.add(new Cwiczenie(jObj.getString("nazwa"),jObj.getString("serie"),jObj.getString("powtorzenia")));
						hm.put(i-1, lista);
					} catch (JSONException e) {
						Log.e("JSON Parser", "Error parsing data " + e.toString());
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			if(!error){
				for(int i=0; i<5;i++){
					dynamicInt(i);
				}
				dialog.dismiss();
			}
			else{
				ProgressBar pasek=(ProgressBar) dialog.findViewById(R.id.progressBar1);
				TextView tekst = (TextView) dialog.findViewById(R.id.pleaseWait);
				pasek.setVisibility(View.INVISIBLE);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
				
				p.addRule(RelativeLayout.BELOW, R.id.image);
				p.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.image);
				
				//tekst.setLayoutParams(p);
				tekst.setText(error_msg);
				tekst.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
				tekst.setTextSize(20);
								
				zamknij.setVisibility(View.VISIBLE);
				zamknij.setOnClickListener(new View.OnClickListener(){
		     	@Override
	            public void onClick(View v) {
		     		dialog.dismiss();
		     		Intent i = new Intent(context,MenuActivity.class);
					startActivity(i);
		        }
				});
			}
		}
	}
	
	private void dynamicInt(int a) {
		List<Cwiczenie> lista = hm.get(a);
		LinearLayout linearLayout = layoutList.get(a);
		for(int i = 0; i < lista.size(); i++){
	        TextView tytul = new TextView(context);
	        tytul.setText("Cwiczenie "+Integer.toString(i+1));
	        tytul.setId(id);
	        id++;
	        tytul.setBackgroundResource(R.drawable.btn_green_matte);
	        tytul.setTextAppearance(context, R.style.naglowekTrening);
	        tytul.setGravity(Gravity.CENTER_HORIZONTAL);
	        
	        ((LinearLayout) linearLayout).addView(tytul);
	        
	        TextView przerwa = new TextView(context);
	        przerwa.setText("");
	        przerwa.setId(id);
	        id++;
	        przerwa.setGravity(Gravity.CENTER_HORIZONTAL);
	
	        ((LinearLayout) linearLayout).addView(przerwa);
	        
	        TextView nazwaCwiczenia = new TextView(context);
	        nazwaCwiczenia.setText( (String)lista.get(i).getNazwa());
	        nazwaCwiczenia.setId(id);
	        id++;
	        nazwaCwiczenia.setTextAppearance(context, R.style.planTreningu);
	        nazwaCwiczenia.setGravity(Gravity.CENTER_HORIZONTAL);
	
	        ((LinearLayout) linearLayout).addView(nazwaCwiczenia);
	        
	        TextView serie = new TextView(context);
	        serie.setText("("+(String)lista.get(i).getSerie()+" Serie)");
	        serie.setId(id);
	        id++;
	        serie.setTextAppearance(context, R.style.planTreninguSmall);
	        serie.setGravity(Gravity.CENTER_HORIZONTAL);
	
	        ((LinearLayout) linearLayout).addView(serie);
	        
	        TextView przerwa2 = new TextView(context);
	        przerwa2.setText("");
	        przerwa2.setId(id);
	        id++;
	        przerwa2.setGravity(Gravity.CENTER_HORIZONTAL);
	
	        ((LinearLayout) linearLayout).addView(przerwa2);
	        
	        int liczba_serii = Integer.parseInt((String)lista.get(i).getSerie());
	        for(int j=1;j<=liczba_serii;j++){
		        TextView d = new TextView(context);
		        d.setText(Integer.toString(j)+" Seria : "+(String)lista.get(i).getPowtorzenia()+" powtorzen");
		        d.setId(id);
		        id++;
		        d.setTextAppearance(context, R.style.planTreninguMedium);
		        ((LinearLayout) linearLayout).addView(d);
	        }
	        
	        TextView przerwa3 = new TextView(context);
	        przerwa3.setText("");
	        przerwa3.setId(id);
	        id++;
	        przerwa3.setGravity(Gravity.CENTER_HORIZONTAL);
	        ((LinearLayout) linearLayout).addView(przerwa3);
	        
	        TextView przerwa4 = new TextView(context);
	        przerwa4.setText("");
	        przerwa4.setId(id);
	        id++;
	        przerwa4.setGravity(Gravity.CENTER_HORIZONTAL);
	        ((LinearLayout) linearLayout).addView(przerwa4);
	        
		}
		layoutList.set(a,linearLayout);
	}
	
	// Using the following method, we will handle all screen swaps.
    public boolean onTouchEvent(MotionEvent touchevent) {
    	switch (touchevent.getAction()) {
        
        case MotionEvent.ACTION_DOWN: 
        	lastX = touchevent.getX();
            break;
        case MotionEvent.ACTION_UP: 
            float currentX = touchevent.getX();
            
            // Handling left to right screen swap.
            if (lastX < currentX) {
            	
            	// If there aren't any other children, just break.
                if (viewFlipper.getDisplayedChild() == 0)
                	break;
                
                // Next screen comes in from left.
                viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);
                // Current screen goes out from right. 
                viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);
                
                // Display next screen.
                viewFlipper.showNext();
             }
                                     
            // Handling right to left screen swap.
             if (lastX > currentX) {
            	 
            	 // If there is a child (to the left), kust break.
            	 if (viewFlipper.getDisplayedChild() == 1)
            		 break;
    
            	 // Next screen comes in from right.
            	 viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
            	// Current screen goes out from left. 
            	 viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);
                 
            	// Display previous screen.
                 viewFlipper.showPrevious();
             }
             break;
    	 }
         return false;
    }
}
