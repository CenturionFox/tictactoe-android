package edu.pti.bem9.android.tictactoe;

import android.util.Log;

class TicTacRow {
	
	/**
	 * The parent activity for all TicTacRow instances.
	 */
	private static TicTacTivity parent = null;
	
	/**
	 * Array containing the values contained within the row's squares.
	 */
	private CharSequence[] rowValues;
	/**
	 * Array containing the button IDs represented by this row.
	 */
	private int[] rowIds;
	/**
	 * Is this row won?
	 */
	private boolean isWinningRow = false;
	/**
	 * The winner of this row.
	 */
	private CharSequence winner = null;
	
	/**
	 * Initializes the TicTacRow class.
	 * @param newParent - TicTacTivity to set as the parent for all TicTacRow instances.
	 */
	public static void init(TicTacTivity newParent)
	{
		Log.d(TicTacTivity.TAG_DEBUG, "Initializing TicTacRow static values...");
		parent = newParent;
	}
	
	/**
	 * Creates a new TicTacRow.
	 * @param charsInRow - The characters in each box of this row.
	 * @param idsInRow - The ids of each box in this row.
	 */
	TicTacRow(CharSequence[] charsInRow, int[] idsInRow) 
	{
		
		this.rowValues = charsInRow;
		this.rowIds = idsInRow;
	}
	
	/**
	 * Updates this row's state.
	 */
	public void updateRowState() 
	{
		this.rowValues[0] = parent.getButton(this.rowIds[0]).getText();
		this.rowValues[1] = parent.getButton(this.rowIds[1]).getText();
		this.rowValues[2] = parent.getButton(this.rowIds[2]).getText();
		
		if(this.rowValues[0].equals(this.rowValues[1]) && this.rowValues[1].equals(this.rowValues[2]) && !this.rowValues[0].equals(""))
		{
			this.isWinningRow = true;
			this.winner = this.rowValues[0];
		} else
		{
			this.isWinningRow = false;
			this.winner = null;
		}
		
		return;
	}

	/**
	 * Checks to see if this is a winning row.
	 * @return Whether or not this row is a winning row.
	 */
	public boolean isWon()
	{
		return this.isWinningRow;
	}
	
	/**
	 * Returns the IDS in this row.
	 * @return The ids in this row.
	 */
	public int[] getIds()
	{
		return this.rowIds;
	}
	
	/**
	 * Gets the winner of this row.
	 * @return The row's winner
	 */
	public CharSequence getWinner()
	{
		return this.winner;
	}
	
	/**
	 * Can the AI win this turn?
	 * @return true if the AI can win in this row.
	 */
	public boolean impendingWin()
	{
		String cpu = parent.getString(R.string.o_player);
		if((this.rowValues[0].equals(cpu) && this.rowValues[1].equals(this.rowValues[0]) && this.rowValues[2].equals("")) ||
				(this.rowValues[1].equals(cpu) && this.rowValues[2].equals(this.rowValues[1]) && this.rowValues[0].equals("")) ||
				(this.rowValues[0].equals(cpu) && this.rowValues[2].equals(this.rowValues[0]) && this.rowValues[1].equals("")))
			{
				return true;
			}
			
			return false;
	}
	
	/**
	 * Should the AI block?
	 * @return true if the AI should block.
	 */
	public boolean impendingDoom()
	{
		String ply = parent.getString(R.string.x_player);
		if((this.rowValues[0].equals(ply) && this.rowValues[1].equals(this.rowValues[0]) && this.rowValues[2].equals("")) ||
				(this.rowValues[1].equals(ply) && this.rowValues[2].equals(this.rowValues[1]) && this.rowValues[0].equals("")) ||
				(this.rowValues[0].equals(ply) && this.rowValues[2].equals(this.rowValues[0]) && this.rowValues[1].equals("")))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Gets a randomized open space in this row.
	 * @return A randomized open space in this row.
	 */
	public int getOpenSpace()
	{
		int x = 0;
		int[] openInts = new int[x];
		
		for(int i = 0; i < 3; i++)
		{
			if(this.rowValues[i].length() == 0)
			{
				int[] _temp = new int[++x];
				int x1 = 0;
				
				for(int i1 : openInts)
				{
					_temp[x1++] = i1;
				}
				
				_temp[x - 1] = this.rowIds[i];
				openInts = _temp;
			}
		}
		
		if(openInts.length > 0)
		{
			return openInts[ToeBot.getToeBot().getRandomizer().nextInt(openInts.length)];
		}
		
		return -1;
	}
	
}