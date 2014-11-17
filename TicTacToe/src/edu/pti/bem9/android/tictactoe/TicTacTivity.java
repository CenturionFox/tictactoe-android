package edu.pti.bem9.android.tictactoe;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Simple Tic Tac Toe game for android 4.0.3+
 * @author Bridger Maskrey
 * @version 0.1.0
 */
public class TicTacTivity extends Activity 
{
    /**
	 * The name of the application.
	 */
	protected static final String APP_NAME = "TicTacToe";
	/**
	 * The name of the application package.
	 */
	protected static final String APP_PACKAGE_NAME = "edu.pti.bem9.android.tictactoe";
	/**
	 * The default tag for debug output.
	 */
	protected static final String TAG_DEBUG = APP_NAME + " Debug";
	/**
	 * The default tag for profiling output.
	 */
	protected static final String TAG_PROF = APP_NAME + " Profiling";
	/**
	 * The default tag for AI processing output.
	 */
	protected static final String TAG_AI = APP_NAME + " AI";
	
	/**
	 * Stores whether or not the game has been won.
	 */
	private boolean gameWon = false;
	
	/**
	 * Increments on each move by player and computer to 
	 */
	private int currentMove = 0;
	
	/**
	 * A size 8 array of all 
	 */
	protected TicTacRow[] rows = new TicTacRow[8];
	
	/**
	 * ArrayList containing each of the buttons that have already been pressed.
	 */
	protected ArrayList<Button> invalidated = new ArrayList<Button>();
	
	/**
	 * Map of int arrays for storing button rows, cols, and diags present on the board.
	 */
	protected int[][] idlist;
	
	/**
	 * Contains the winning player's letter, or null if nobody is winning.
	 */
	private CharSequence winner = "null";
	
	/**
	 * True if the computer is currently making or is about to make a move.
	 */
	private boolean makingComputerMove = false;
	
	/**
	 * Int array containing the ids of any row containning three of the same letters.
	 */
	private int[] winRow;
	
	/**
	 * Internal class used to execute the ToeBot move processing algorithm.
	 * Should be used after a delay to "slow down" computer thinking.
	 * 
	 * @method computerMoveMaker.run() - Executes AI processing tasks.
	 */
	private Runnable computerMoveMaker = new Runnable() {
		/**
		 * Executes AI processing tasks.
		 */
		public void run() {
			Log.d(TicTacTivity.TAG_AI, "Choosing space.");
			long s = android.os.SystemClock.currentThreadTimeMillis();
			
        	Button toeBotChoice = getButton(ToeBot.getToeBot().chooseSpace());
        	
        	Log.i(TicTacTivity.TAG_PROF, "Choose operation took " + (android.os.SystemClock.currentThreadTimeMillis() - s) + "milliseconds.");
        	
        	if(toeBotChoice != null)
        	{
        		TicTacTivity.this.invalidated.add(toeBotChoice);
        		toeBotChoice.setText(getString(R.string.o_player));
        		toeBotChoice.setTextColor(getResources().getColor(R.color.o_player_color));
        		TicTacTivity.this.currentMove++;
        		
        		if(TicTacTivity.this.currentMove > 2) TicTacTivity.this.gameWon = checkWin();
        	}
        	TicTacTivity.this.makingComputerMove = false;
        	doGameWin();
		}
	};
	
	/**
	 * Executed when the app is started.
	 * First it sets the content view to the main layout.
	 * Then it creates a new typeface representing assets/fonts/ComicNeue-Bold.ttf.
	 * Then it populates the idlist using reflection.
	 * Profiling information is output upon method completion.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	Log.d(TicTacTivity.TAG_DEBUG, "Creating new TicTacToe activity!");
    	long s = SystemClock.currentThreadTimeMillis();
    	    	
        setContentView(R.layout.main);
        
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/ComicNeue-Bold.ttf");
        
        this.idlist = new int[3][];
        
        Field[] f = R.id.class.getDeclaredFields();
        
        this.idlist[0] = new int[3];
        int i1 = 0;
        this.idlist[1] = new int[3];
        int i2 = 0;
        this.idlist[2] = new int[3];
        int i3 = 0;
        
        try
        {
	        for(Field f1 : f)
	        {
	        	CharSequence name = f1.getName();
	        	if(name.length() < "button".length()) continue;
	        	if(name.subSequence(0, "button".length()).equals("button"))
	        	{
	        		switch(name.charAt(6))
	        		{
	        		case '1':
	        			this.idlist[0][i1++] = f1.getInt(null);
	        			break;
	        		case '2':
	        			this.idlist[1][i2++] = f1.getInt(null);
	        			break;
	        		case '3':
	        			this.idlist[2][i3++] = f1.getInt(null);
	        			break;
	        		default: break;
	        		}
	        		
	        		this.getButton(f1.getInt(null)).setTypeface(tf);
	        	}
	        }
        } catch(Exception e)
        {
        	Log.e(TicTacTivity.TAG_DEBUG, "Exception thrown during id array construction! EXCEPTION TYPE: " + e.getClass().getCanonicalName() 
        			+ " EXCEPTION LOCATION: " + e.getStackTrace()[0].getClassName() + ":" +e.getStackTrace()[0].getLineNumber());
        	e.printStackTrace();
        	throw new RuntimeException("Failed to create button array! " + e.getLocalizedMessage());
        }
        
        ToeBot.getToeBot().initialize(this);
        TicTacRow.init(this);
        
        super.onCreate(savedInstanceState);
        Log.i(TicTacTivity.TAG_PROF, "onCreate completed after " + (SystemClock.currentThreadTimeMillis() - s) + " milliseconds.");
    }
    
    /**
     * Executed whenever the activity saves its state.
     * Profiling information is output upon method completion.
     * 
     * @param state - Bundle that all program data will be saved to.
     * 				  The program data saved includes the {@linkplain TicTacTivity#winner the current winner},
     * 				  whether or not {@linkplain TicTacTivity#gameWon the game has been won}, the {@linkplain TicTacTivity#currentMove
     * 				  current move}, whether or not {@linkplain TicTacTivity#makingComputerMove the computer is making a move},
     * 				  two arrays representing {@linkplain TicTacTivity#invalidated the buttons that have already been pressed}, and, finally,
     * 				  {@linkplain TicTacTivity#winRow the current winning row}, if applicable.
     */
    public void onSaveInstanceState(Bundle state)
    {
    	Log.d(TicTacTivity.TAG_DEBUG, "Saving instance state!");
    	long s = SystemClock.currentThreadTimeMillis();
    	
    	super.onSaveInstanceState(state);
    	
    	state.putCharSequence("winner", this.winner);
    	state.putBoolean("gameWon", this.gameWon);
    	state.putInt("currentMove", this.currentMove);
    	state.putBoolean("cpu", this.makingComputerMove);
    	int[] invalid = new int[this.invalidated.size()];
    	CharSequence[] plays = new CharSequence[this.invalidated.size()];
    	for(int i = 0; i < this.invalidated.size(); i++)
    	{
    		invalid[i] = this.invalidated.get(i).getId();
    		plays[i] = this.invalidated.get(i).getText();
    	}
    	
    	state.putCharSequenceArray("plays", plays);
    	state.putIntArray("invalid", invalid);
    	
    	try
    	{
        	state.putIntArray("winningRow", this.winRow);
    	} catch(NullPointerException e)
    	{
    		Log.d(TicTacTivity.TAG_DEBUG, "Error parsing winRow. Perhaps it is uninitialized?");
    	}
    	
    	Log.i(TicTacTivity.TAG_PROF, "Instance state saved after " + (SystemClock.currentThreadTimeMillis() - s) + " milliseconds.");
    }
    
    /**
     * Executes when the activity is restored from a saved state.
     * Profiling information is output upon method completion.
     * 
     * @param state - Bundle that contains the program data saved on last run.
     * 				  This method assumes that that saved state will work.
     */
    public void onRestoreInstanceState(Bundle state)
    {
    	Log.d(TicTacTivity.TAG_DEBUG, "Restoring saved instance state!");
    	long s = SystemClock.currentThreadTimeMillis();
    	
    	super.onRestoreInstanceState(state);
    	
    	this.winner = state.getCharSequence("winner");
    	this.gameWon = state.getBoolean("gameWon");
    	this.currentMove = state.getInt("currentMove");
    	this.winRow = state.getIntArray("winningRow");
    	
    	int[] invalid = state.getIntArray("invalid");
    	CharSequence[] plays = state.getCharSequenceArray("plays");
    	
    	for(int i = 0; i < invalid.length; i++) {
    		
    		Button b = this.getButton(invalid[i]);
    		b.setText(plays[i]);
    		b.setTextColor(plays[i].equals(this.getString(R.string.x_player)) ? 
    				this.getResources().getColor(R.color.x_player_color) : this.getResources().getColor(R.color.o_player_color));
    		
    		this.invalidated.add(b);
    	}
    	
    	this.makingComputerMove = state.getBoolean("cpu");
    	try
    	{
    		for(int i : this.winRow)
       		{
       			this.getButton(i).setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_selected));
       		}
    	} catch(NullPointerException e)
    	{
    		Log.d(TicTacTivity.TAG_DEBUG, "Error parsing winRow. Perhaps it is uninitialized?");
    	}
  		
    	Log.i(TicTacTivity.TAG_PROF, "Restored instance state after " + (SystemClock.currentThreadTimeMillis() - s) + " milliseconds.");
    }
    
    /**
     * Executes when the activity is started.
     * Profiling information is output upon method completion.
     */
    public void onStart() 
    {
    	Log.d(TicTacTivity.TAG_DEBUG, "Starting TicTacTivity...");
    	long s = SystemClock.currentThreadTimeMillis();
    	
    	super.onStart();
    	
    	CharSequence[][] aacs = new CharSequence[3][];
		
		for(int i = 0; i < 3; i++)
		{
			aacs[i] = new CharSequence[3];
		}
		
		for(int i = 0; i < 3; i++) {
			
			for(int j = 0; j < 3; j++)
			{
				aacs[i][j] = ((Button)this.findViewById(this.idlist[i][j])).getText();
			}
		}
		
    	this.rows[0] = new TicTacRow(aacs[0], this.idlist[0]);
		this.rows[1] = new TicTacRow(aacs[1], this.idlist[1]);
		this.rows[2] = new TicTacRow(aacs[2], this.idlist[2]);
		
		this.rows[3] = new TicTacRow(new CharSequence[]{aacs[0][0], aacs[1][0], aacs[2][0]},
									new int[]{this.idlist[0][0], this.idlist[1][0], this.idlist[2][0]});
		this.rows[4] = new TicTacRow(new CharSequence[]{aacs[0][1], aacs[1][1], aacs[2][1]},
									new int[]{this.idlist[0][1], this.idlist[1][1], this.idlist[2][1]});
		this.rows[5] = new TicTacRow(new CharSequence[]{aacs[0][2], aacs[1][2], aacs[2][2]},
									new int[]{this.idlist[0][2], this.idlist[1][2], this.idlist[2][2]});
		
		this.rows[6] = new TicTacRow(new CharSequence[]{aacs[0][0], aacs[1][1], aacs[2][2]},
									new int[]{this.idlist[0][0], this.idlist[1][1], this.idlist[2][2]});
		this.rows[7] = new TicTacRow(new CharSequence[]{aacs[0][2], aacs[1][1], aacs[2][0]},
									new int[]{this.idlist[0][2], this.idlist[1][1], this.idlist[2][0]});
		
    	if(this.makingComputerMove)
    	{
    		new Handler().postDelayed(this.computerMoveMaker, 500);
    	}
    	
    	Log.i(TicTacTivity.TAG_PROF, "Started TicTacTivity; operation took " + (SystemClock.currentThreadTimeMillis() - s) + " milliseconds.");
    }
    
    /**
     * Gets a button view by ID.
     * 
     * @param id - the ID of the button view.
     * @return The button related to the given ID.
     */
    public Button getButton(int id)
    {
		return (Button)this.findViewById(id);
	}

    /**
     * Handles button input for the board.
     * Profiling information is output upon method completion.
     * This method also contains the main calls for the ToeBot AI.
     * 
     * If the ToeBot AI is making a move, this method does nothing.
     * This method will also call the gameWon() method.
     * 
     * @param view - the view that called the event (may never be null)
     */
	public void buttonEventHandler(View view)
    {
		Log.d(TicTacTivity.TAG_DEBUG, "Handling button input!");
		long s = SystemClock.currentThreadTimeMillis();
		
		if(this.makingComputerMove) return;
		
    	Button b = this.getButton(view.getId());
    	
    	if(!this.invalidated.contains(b) && !this.gameWon && this.currentMove < 9)
    	{
    		this.invalidated.add(b);
    		
    		b.setTextColor(this.getResources().getColor(R.color.x_player_color));
    		b.setText(this.getString(R.string.x_player));
    		b.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button));
    		
    		this.currentMove++;
    		
    		if(this.currentMove > 2) this.gameWon = this.checkWin();
    		
    		if(!this.gameWon && this.currentMove < 9) {
    			
    			this.makingComputerMove = true;
    			
    			new Handler().postDelayed(this.computerMoveMaker, 500);  			
    		} else
    		{
    			this.doGameWin();
    		}
    	} else
    	{
    		if(this.gameWon) Toast.makeText(this, this.getString(R.string.game_over), Toast.LENGTH_SHORT).show();
    		else if(this.currentMove >= 9) Toast.makeText(this, this.getString(R.string.draw_reached), Toast.LENGTH_LONG).show();
    		else Toast.makeText(this, this.getString(R.string.area_played), Toast.LENGTH_SHORT).show();
    	}
    	
    	Log.i(TicTacTivity.TAG_PROF, "Button press operations took " + (SystemClock.currentThreadTimeMillis() - s) + " milliseconds.");
    	
    }
	
	/**
	 * Checks to see if the game was won by either player or by the "cat"
	 * (which I have recently been told is a thing)
	 * Profiling information is output upon method completion.
	 */
	private void doGameWin() 
	{
		Log.d(TicTacTivity.TAG_DEBUG, "Handling Game Win Check!");
		long s = SystemClock.currentThreadTimeMillis();
		
		if(this.gameWon) {
			for(int id : this.winRow)
			{
				Button b1 = this.getButton(id);
				b1.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_selected));
			}
			
			final android.app.FragmentManager fm = this.getFragmentManager();
			
			new Handler().postDelayed(new Runnable() {
				public void run() {
					new EndgameDialog().show(fm, "endGame");
				}
			}, 500);
			
		} else
		if(this.currentMove == 9)
		{
			final android.app.FragmentManager fm = this.getFragmentManager();
			new Handler().postDelayed(new Runnable() {
				public void run() {
					new EndgameDialog().show(fm, "endGame");
				}
			}, 500);
		}
		
		Log.i(TicTacTivity.TAG_PROF, "Checking game win took " + (SystemClock.currentThreadTimeMillis() - s) + "milliseconds.");
	}

	/**
	 * Handles restarting the game.
	 * Clears all buttons and resets all counters and bools.
	 * Profiling information is output upon method completion.
	 * 
	 * @param menu - the Menu Item that called this event (can be null)
	 */
	public void restart(MenuItem menu) {
		
		Log.d(TicTacTivity.TAG_DEBUG, "Restarting game.");
		long s = SystemClock.currentThreadTimeMillis();
		
		for(int i = 0; i < 3; i++) {
			
			for(int j = 0; j < 3; j++) 
			{
				Button button = this.getButton(this.idlist[i][j]);
				button.setText("");
				button.setTextColor(0);
				button.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_state));
			}
		}
		
		this.invalidated.clear();
		this.gameWon = false;
		this.currentMove = 0;
		this.winner = "null";
		this.winRow = new int[3];
		
		for(TicTacRow row : this.rows)
		{
			row.updateRowState();
		}
		
		System.gc();
		
		Log.i(TicTacTivity.TAG_PROF, "Restarting took " + (SystemClock.currentThreadTimeMillis() - s) + " milliseconds.");
	}
	
	/**
	 * Displays an "About the Author"-esque dialog box.
	 * @param menu - The menu item that called this event (can be null)
	 */
	public void about(MenuItem menu) {
				
		try
		{
			new AboutDialog().show(getFragmentManager(), "aboutDialog");
		} catch(RuntimeException e)
		{
			Toast.makeText(this, "ERROR: Unable to execute command.", Toast.LENGTH_LONG).show();
		}
		
	}
	
	/**
	 * Exits the activity.
	 * @param menu - The menu item that called the event (can be null)
	 */
	public void exit(MenuItem menu) {
		
		this.finish();
		
	}
	
	/**
	 * Creates the options menu.
	 * @param menu - The menu to construct.
	 */
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inf = this.getMenuInflater();
		inf.inflate(R.menu.menu, menu);
		
		return true;
	}
	
	/**
	 * Checks to see if any row is in a winning state.
	 * @return If the game has been won.
	 */
	public boolean checkWin() {
		
		for(TicTacRow row : this.rows)
		{
			row.updateRowState();
			
			if(row.isWon())
			{
				this.winner = row.getWinner();
				this.winRow = row.getIds();
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * Dialog Fragment that contains the information to create a new endgame dialog.
	 * @author Bridger Maskrey
	 */
	public static class EndgameDialog extends DialogFragment {
		
		public EndgameDialog() {
			super();
		}

		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			final TicTacTivity activity = (TicTacTivity) this.getActivity();
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setMessage(activity.gameWon ? activity.winner + " " + this.getString(R.string.player_win) : getString(R.string.draw_reached))
			   .setNegativeButton(getString(R.string.return_to_game), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
			   })
			   .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) 
				{
					activity.restart(null);
					dialog.dismiss();
				}
			   })
			   .setTitle(getString(R.string.game_win_dialog))
			   .setIcon(activity.gameWon ? (activity.winner.equals(this.getString(R.string.x_player)) ? 
					    getResources().getDrawable(R.drawable.ic_trophy) : getResources().getDrawable(R.drawable.id_loss)) 
					    : getResources().getDrawable(R.drawable.ic_draw));
			return adb.create();
		}
	}
	
	/**
	 * Dialog Fragment that contains the information to create a new "About the Author" dialog.
	 * @author bem9
	 */
	public static class AboutDialog extends DialogFragment {
		
		public AboutDialog() {
			super();
		}
		
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setMessage(this.getString(R.string.app_desc) + " " + this.getString(R.string.app_version))
			   .setNegativeButton(getString(R.string.return_to_game), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
			   })
			   .setPositiveButton(getString(R.string.rate), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) 
				{
					getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));
					dialog.dismiss();
				}
			   })
			   .setTitle(getString(R.string.info))
			   .setIcon(getResources().getDrawable(R.drawable.ic_about));
			return adb.create();
		}
		
	}
}