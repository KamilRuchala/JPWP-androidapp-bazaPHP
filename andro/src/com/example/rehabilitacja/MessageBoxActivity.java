package com.example.rehabilitacja;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabilitacja.klasy.UserFunctions;
import com.example.rehabilitacja.klasy.Wiadomosc;

public class MessageBoxActivity extends ActionBarActivity {
	
	private String error_msg = null;
	private boolean error = false;
	private Context context = this;
	private static String KEY_UID = "uid";
    private static String KEY_SID = "sid";
	
	private String uid, sid;
	private static JSONObject jObj = null;
	private static String page;
	
	private List<Wiadomosc> messages_list = new ArrayList<Wiadomosc>();
	private LinearLayout[] tablicaLayout = new LinearLayout[21];
	
	private Button new_message, prev_button, next_button;
	private EditText editTitle;
	private String title = "";
	private Dialog new_dialog;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_box);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
	        | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

		new_message = (Button) findViewById(R.id.button2);
		prev_button = (Button) findViewById(R.id.button1);
		next_button = (Button) findViewById(R.id.button3);
		
		prev_button.setOnClickListener(new View.OnClickListener(){
			@Override
            public void onClick(View v) {
				int foo = Integer.parseInt(page);
				foo--;
				page = Integer.toString(foo);
				new nowyWatek().execute();
			}
		});
		
		next_button.setOnClickListener(new View.OnClickListener(){
			@Override
            public void onClick(View v) {
				int foo = Integer.parseInt(page);
				foo++;
				page = Integer.toString(foo);
				new nowyWatek().execute();
			}
		});
		
		new_message.setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
        		new_dialog = new Dialog(context);
        		new_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        		new_dialog.setContentView(R.layout.new_message_dialog);
        		editTitle = (EditText) new_dialog.findViewById(R.id.editTitle);
        		Button ok = (Button) new_dialog.findViewById(R.id.okButton);
    	    	new_dialog.show();
    	    	ok.setOnClickListener(new View.OnClickListener(){
    	        	@Override
    	            public void onClick(View v) {
    	        		title = editTitle.getText().toString();
    	        		if(title.matches("\\w+")){
    	        			new_dialog.dismiss();
    	        			forward(title);
    	        		}
    	        		else{
    	        			Toast.makeText(getApplicationContext(), "Podaj tytul!", Toast.LENGTH_LONG).show();
    	        		}
    	        	}
    	    	});
            }
        });
		page = "0";
		Bundle bb = getIntent().getExtras();
		uid=bb.getString("uid");
		sid=bb.getString("sid");
		int tmp = R.id.layout1;
		tablicaLayout[0] = (LinearLayout) findViewById(tmp);
		for(int i=1;i<21;i++){
			tablicaLayout[i] = (LinearLayout) findViewById(tmp + i*3);
			tablicaLayout[i].setVisibility(LinearLayout.GONE);
		}
		
		new nowyWatek().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    // action with ID action_refresh was selected
	    case R.id.action_refresh:
	      refresh();
	      break;
	    default:
	      break;
	    }
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		Intent i = new Intent(this,MenuActivity.class);
 	   	startActivity(i);
 	   	finish();
	}
	
	private class nowyWatek extends AsyncTask<Void,Void,Void>{

		final Dialog dialog = new Dialog(context);
		Button zamknij;
		TextView pleaseWait;
		
		@Override
		protected void onPreExecute(){
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	dialog.setContentView(R.layout.activity_login_dialog);
	    	zamknij = (Button) dialog.findViewById(R.id.zamknijButton);
	    	zamknij.setVisibility(View.INVISIBLE);
	    	pleaseWait = (TextView) dialog.findViewById(R.id.pleaseWait);
	    	pleaseWait.setText("Loading");
	    	dialog.show();	
	    	
		}
		
		@Override
		protected Void doInBackground(Void... params){
			String page_tmp;
			page_tmp = Integer.toString((Integer.parseInt(page)+1));
			String odpowiedz = UserFunctions.getMessageBox(uid, sid, page_tmp);
			
			String[] message_titles = odpowiedz.split("--");
			try {
				jObj = new JSONObject(message_titles[0]); 
				if("0".equals(jObj.getString("success"))){
					error = true;
					error_msg = jObj.getString("error_msg");
					return null;
				}
	        } catch (JSONException e) {
	           // Log.e("JSONParserTreningAct", "Error parsing data " + e.toString());
	        }
			
			for(int i = 1;i<message_titles.length;i++){
				String tmp = message_titles[i];
				try {
					jObj = new JSONObject(tmp);
					boolean readed = false;
					if("1".equals(jObj.getString("is_read"))) readed = true;
					messages_list.add(new Wiadomosc(jObj.getString("data"),jObj.getString("tytul"), jObj.getString("tag"), readed));
		        } catch (JSONException e) {
		            Log.e("JSONParserTreningAct", "Error parsing data " + e.toString());
		            break;
		        }
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			if(!error){
				prepareLayout();
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
	
	private void prepareLayout(){
		int size = messages_list.size();
		int tmp=0;
		Wiadomosc aaa;
		TextView title, data;
		for(int i=0;i<size;i++){
			aaa = messages_list.get(i);
			tablicaLayout[i].setVisibility(LinearLayout.VISIBLE);
			tmp = tablicaLayout[i].getId();
			title = (TextView) findViewById(tmp+1);
			data = (TextView) findViewById(tmp+2);
			title.setText(aaa.getTitle());
			data.setText(aaa.getData());
			title.setOnClickListener(clicks);
			if(!aaa.getIs_read() && "1".equals(aaa.getTag())){
				title.setTypeface(null, Typeface.BOLD);
				data.setTypeface(null, Typeface.BOLD);
			}	
		} 
		tablicaLayout[20].setVisibility(LinearLayout.VISIBLE);
		
		Button prev = (Button) findViewById(R.id.button1);
		Button next = (Button) findViewById(R.id.button3);
		if(size < 22){
			prev.setVisibility(View.INVISIBLE);
			next.setVisibility(View.INVISIBLE);
		}
		if("0".equals(page) == false){
			prev.setVisibility(View.VISIBLE);
		}
		
	}
	
	private void forward(String tytul){
		Intent i = new Intent(this,MessageActivity.class);
		i.putExtra(KEY_UID, uid);
		i.putExtra(KEY_SID, sid);
		i.putExtra("title", tytul);
 	   	startActivity(i);
	}
	
	private void refresh(){
		Intent i = new Intent(this,MessageBoxActivity.class);
		i.putExtra(KEY_UID, uid);
		i.putExtra(KEY_SID, sid);
 	   	startActivity(i);
	}
	
	OnClickListener clicks=new OnClickListener() {
		
	    @Override
	    public void onClick(View v) {
	    	int id1 = v.getId();
	    	TextView tmp = (TextView) findViewById(id1);
	    	String title = (tmp.getText()).toString();
	    	forward(title);
	    }
	};
}
