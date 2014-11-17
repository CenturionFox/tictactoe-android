package edu.pti.bem9.android.tictactoe;

import java.util.Random;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;

public class ToeBot {

	/**
	 * List of all button ids.
	 */
	private int[][] idlist;
	/**
	 * The parent activity.
	 */
	private TicTacTivity parent;
	/**
	 * Random number generator.
	 */
	private Random randomizer;
	
	/**
	 * Group of strategy-detecting rows
	 */
	private TicTacRow[] strategic = new TicTacRow[12];
	
	/**
	 * Singleton instance
	 */
	private static ToeBot theToeBot;
	
	/**
	 * Method to get or create the singleton instance.
	 * @return The singleton instance.
	 */
	public static ToeBot getToeBot() {
		
		if(theToeBot == null) theToeBot = new ToeBot();
		
		return theToeBot;
		
	}
	
	/**
	 * Private constructor. Creates a new ToeBot and sets the randomizer with a new seed.
	 */
	private ToeBot(){
		this.randomizer = new Random();
		getRandomizer().setSeed(SystemClock.currentThreadTimeMillis());
	}
	
	/**
	 * Initializes the ToeBot.
	 * Profiling information is output on method completion.
	 * @param parentActivity - the parent activity.
	 */
	public void initialize(TicTacTivity parentActivity) {
		this.parent = parentActivity;
		this.idlist = parentActivity.idlist;
		
		Log.d(TicTacTivity.TAG_AI, "Initializing ToeBot singleton");
		long s = SystemClock.currentThreadTimeMillis();
		
		CharSequence[][] aacs = new CharSequence[3][];
		
		for(int i = 0; i < 3; i++)
		{
			aacs[i] = new CharSequence[3];
		}
		
		for(int i = 0; i < 3; i++) {
			
			for(int j = 0; j < 3; j++)
			{
				aacs[i][j] = ((Button)this.parent.findViewById(this.idlist[i][j])).getText();
			}
		}
		
		this.strategic[0] = new TicTacRow(new CharSequence[]{aacs[0][0], aacs[1][0], aacs[2][1]},
										 new int[]{this.idlist[0][0],this.idlist[1][0],this.idlist[2][1]});
		this.strategic[1] = new TicTacRow(new CharSequence[]{aacs[0][2], aacs[1][2], aacs[2][1]},
										 new int[]{this.idlist[0][2],this.idlist[1][2],this.idlist[2][1]});
		this.strategic[2] = new TicTacRow(new CharSequence[]{aacs[0][1], aacs[1][0], aacs[2][0]},
										 new int[]{this.idlist[0][1], this.idlist[1][0], this.idlist[2][0]});
		this.strategic[3] = new TicTacRow(new CharSequence[]{aacs[0][1], aacs[1][2], aacs[2][2]},
				 						 new int[]{this.idlist[0][1], this.idlist[1][2], this.idlist[2][2]});
		
		this.strategic[4] = new TicTacRow(new CharSequence[]{aacs[0][0], aacs[1][1], aacs[0][2]},
										 new int[]{this.idlist[0][0],this.idlist[1][1],this.idlist[0][2]});
		this.strategic[5] = new TicTacRow(new CharSequence[]{aacs[0][0], aacs[1][1], aacs[2][0]},
										 new int[]{this.idlist[0][0],this.idlist[1][1],this.idlist[2][0]});
		this.strategic[6] = new TicTacRow(new CharSequence[]{aacs[0][2], aacs[1][1], aacs[2][2]},
				 						 new int[]{this.idlist[0][2], this.idlist[1][1], this.idlist[2][2]});
		this.strategic[7] = new TicTacRow(new CharSequence[]{aacs[2][0], aacs[1][1], aacs[2][2]},
				 						 new int[]{this.idlist[2][0], this.idlist[1][1], this.idlist[2][2]});
		
		this.strategic[8] = new TicTacRow(new CharSequence[]{aacs[0][0], aacs[2][0], aacs[2][2]},
										 new int[]{this.idlist[0][0],this.idlist[2][0],this.idlist[2][2]});
		this.strategic[9] = new TicTacRow(new CharSequence[]{aacs[2][0], aacs[2][2], aacs[0][2]},
				 						 new int[]{this.idlist[2][0],this.idlist[2][2],this.idlist[0][2]});
		this.strategic[10] = new TicTacRow(new CharSequence[]{aacs[2][2], aacs[0][2], aacs[0][0]},
				 						 new int[]{this.idlist[2][2], this.idlist[0][2], this.idlist[0][0]});
		this.strategic[11] = new TicTacRow(new CharSequence[]{aacs[0][2], aacs[0][0], aacs[2][0]},
										 new int[]{this.idlist[0][2], this.idlist[0][0], this.idlist[2][0]});
				
		
		Log.i(TicTacTivity.TAG_PROF, "Initialization completed after " + (SystemClock.currentThreadTimeMillis() - s) + "milliseconds.");
	}
	
	/**
	 * Recalculates rows.
	 */
	private void populateRows() 
	{
		Log.d(TicTacTivity.TAG_AI, "Row population in progress...");
		long s = SystemClock.currentThreadTimeMillis();
				
		for(TicTacRow ttr : this.parent.rows)
		{
			ttr.updateRowState();
		}
		
		for(TicTacRow ttr : this.strategic)
		{
			ttr.updateRowState();
		}
		
		Log.i(TicTacTivity.TAG_PROF, "Rows populated after " + (SystemClock.currentThreadTimeMillis() - s) + "milliseconds.");
	}
	
	/**
	 * Choose a space. Checks if the AI can win, if it should block, strategy detection,
	 * center choice, and random choice cases.
	 * @return the space chosen by the AI.
	 */
	public int chooseSpace()
	{
		this.populateRows();
		
		boolean checkCase = false;
		boolean[] cases = new boolean[this.parent.rows.length];
		
		for(int i = 0; i < this.parent.rows.length; i++)
		{
			cases[i] = this.parent.rows[i].impendingWin();
			checkCase |= this.parent.rows[i].impendingWin();
		}
		
		if(checkCase) {
			Log.d(TicTacTivity.TAG_AI, "Win possibility detected.");
			for(int i = 0; i < this.parent.rows.length; i++)
			{
				if(cases[i])
				{
					return this.parent.rows[i].getOpenSpace();
				}
			}
		}
		
		checkCase = false;
		cases = new boolean[this.parent.rows.length];
		
		for(int i = 0; i < this.parent.rows.length; i++)
		{
			cases[i] = this.parent.rows[i].impendingDoom();
			checkCase |= this.parent.rows[i].impendingDoom();
		}
		
		if(checkCase) {
			Log.d(TicTacTivity.TAG_AI, "Lose possibility detected.");
			for(int i = 0; i < this.parent.rows.length; i++)
			{
				if(cases[i])
				{
					return this.parent.rows[i].getOpenSpace();
				}
			}
		}
		
		checkCase = false;
		cases = new boolean[this.strategic.length];
		
		for(int i = 0; i < this.strategic.length; i++)
		{
			cases[i] = this.strategic[i].impendingDoom();
			checkCase |= this.strategic[i].impendingDoom();
		}
		
		if(checkCase) {
			Log.d(TicTacTivity.TAG_AI, "Strategy detected.");
			for(int i = 0; i < this.strategic.length; i++)
			{
				if(cases[i] && this.getRandomizer().nextInt(10) != 0)
				{
					return this.strategic[i].getOpenSpace();
				}
			}
		}
		
		
		if(tryForCenter()) return this.idlist[1][1];
		
		int i = 0;
		int[] openInts = new int[i];
		
		for(TicTacRow row : this.parent.rows) {
			if(row.getOpenSpace() != -1)
			{
				int[] _temp = new int[++i];
				int i1 = 0;
				
				for(int i2 : openInts)
				{
					_temp[i1++] = i2;
				}
				
				_temp[i - 1] = row.getOpenSpace();
				openInts = _temp;
			}
		}
		
		if(openInts.length > 0)
		{
			return openInts[this.getRandomizer().nextInt(openInts.length)];
		}
		
		return -1;
	}
	
	/**
	 * Returns true if the AI should choose the center.
	 * Prioritized over a random selection.
	 * @return True if the AI should try for center.
	 */
	private boolean tryForCenter()
	{
		if(!this.parent.invalidated.contains(this.parent.getButton(this.idlist[1][1])) && getRandomizer().nextBoolean())
		{
			Log.d(TicTacTivity.TAG_AI, "Taking center.");
			return true;
		}
		Log.d(TicTacTivity.TAG_AI, "Random choice.");
		return false;
	}

	/**
	 * Gets the AI randomizer.
	 * @return The AI randomizer.
	 */
	public Random getRandomizer() {
		return this.randomizer;
	}
}
