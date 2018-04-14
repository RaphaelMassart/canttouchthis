// TODO Advanced heuristics: holes
// TODO 1 Forward looking
// TODO Genetic algo training

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PlayerSkeleton {
	private static final Logger LOGGER = Logger.getLogger( PlayerSkeleton.class.getName() );
	private double numHolesWeight = 14; //1.6;//7.6;//0.35663;
	private double bumpinessWeight = 6.1;//3.1;//0.184483;
	private double aggregateHeightWeight = 1.8;//0.8;//0.510066;
	private double rowsClearedWeight = 1.8;//0.8;//-0.760666;
	private boolean oneLookAhead = false;
	private boolean shouldLogEveryHundredRows = true;
	private boolean shouldLogFinalGrid = false;

	public PlayerSkeleton() {
//		try {
//			FileHandler fh = new FileHandler("%h/tetris_log/tetris.log", true);
//			LOGGER.addHandler(fh);
//			MyFormatter formatter = new MyFormatter();
//			fh.setFormatter(formatter);
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public PlayerSkeleton(double weights[], boolean oneLookAhead, boolean logHundredRows, boolean logGrid) {
		this();
		this.numHolesWeight = weights[0];
		this.bumpinessWeight = weights[1];
		this.aggregateHeightWeight = weights[2];
		this.rowsClearedWeight = weights[3];
		this.oneLookAhead = oneLookAhead;
		this.shouldLogEveryHundredRows = logHundredRows;
		this.shouldLogFinalGrid = logGrid;
	}

	public boolean getShouldLogEveryHundredRows(){
		return this.shouldLogEveryHundredRows;
	}

	public int countHoles(InnerState s) {
		int[][] field = s.getField();
		int rowsNum = field.length;
		int colsNum = field[0].length;
		int totalHoleCount = 0;

		for (int j = 0; j < colsNum; j++) {
			int holeCounter = 0;
			for (int i = 0; i < rowsNum -1; i++) {
				if(field[i][j] == 0) {
					holeCounter++;
				}

				if (field[i][j] !=0 && holeCounter != 0) {
					totalHoleCount += holeCounter;
					holeCounter = 0;
				}
			}
		}
		return totalHoleCount;
	}

	public int calculateBumpiness(InnerState s) {
		int[] tops = s.getTop();
		int sum = 0;
		for (int i = 0; i < tops.length-1; i ++) {
			sum += Math.abs(tops[i] - tops[i+1]);
		}
		return sum;
	}


	public static int aggregateHeight(InnerState s) {
		int[] tops = s.getTop();
		int sum = 0;
		for (int top: tops) {
			//top is row + 1, row is 0-based
			sum += top;
		}
		return sum;
	}

	public double calculateCost(InnerState s, int rowsCleared) {

		int numHoles = countHoles(s);
		int aggregateHeight = aggregateHeight(s);
		int bumpiness = calculateBumpiness(s);

		double cost = numHolesWeight * numHoles +
				aggregateHeightWeight * aggregateHeight +
				bumpinessWeight * bumpiness +
				rowsClearedWeight * rowsCleared;
		return cost;
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		double minCost = Double.MAX_VALUE;
		int moveIdx = 0;

		// iterate through legal moves
		for (int i = 0; i < legalMoves.length; i++) {
			InnerState next = new InnerState(s.nextPiece, s.getTurnNumber(), s.getField(), s.getTop());
			// get move
			int[] move = legalMoves[i];
			double currCost;

			if (this.oneLookAhead) {
				next.innerMakeMove(move);
				currCost = oneLookAhead(next);
			} else {
				int rowsCleared = next.innerMakeMove(move);
				currCost = rowsCleared == -1 ? Double.MAX_VALUE : calculateCost(next, rowsCleared);
			}

			if (currCost <= minCost) {
				minCost = currCost;
				moveIdx = i;
			}

		}
		return moveIdx;
	}

	public double oneLookAhead(InnerState s) {
		int sum = 0;
		for (int i = 0; i < InnerState.N_PIECES; i++) {
			int[][] legalMoves = InnerState.legalMoves[i];
			double minCost = Double.MAX_VALUE;
			for (int j = 0; j < legalMoves.length; j++) {
				InnerState next = new InnerState(i, s.getTurnNumber(), s.getField(), s.getTop());
				int[] move = legalMoves[j];
				int rowsCleared = next.innerMakeMove(move);
				double currCost = rowsCleared == -1 ? Math.pow(10,10): calculateCost(next, rowsCleared);

				if (currCost <= minCost) {
					minCost = currCost;
				}
			}
			sum += minCost;
		}
		return sum;
	}

	public String logGameStart() {
		String startTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( new Date() );
		LOGGER.info("New game started at: " + startTimeStamp);
		LOGGER.info("Rows log:");
		return startTimeStamp;
	}

	public void logEveryHundredRows(int rowsCleared) {
		LOGGER.info(Integer.toString(rowsCleared));
	}

	public String logGameOver(int rowsCleared, int[][] field) {
		String endTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( new Date() );
		LOGGER.info("Game ended at: " + endTimeStamp);
		LOGGER.info ("Rows cleared: "+ Integer.toString(rowsCleared));
		if (this.shouldLogFinalGrid) {
			LOGGER.info("Grid:");
			for (int i = field.length - 1; i >= 0; i--) {
				StringBuilder sb = new StringBuilder(String.format("row %2d ", i));
				for (int j = 0; j < field[i].length; j++) {
					String turn = String.format(" %9d", field[i][j]);
					sb.append(turn);
				}
				LOGGER.info(sb.toString());
			}
		}
		LOGGER.info("===================================");
		return endTimeStamp;
	}

	public void writeClearedRows(int rowsCleared, String start, String end) {
		String filePath = System.getProperty("user.home") + File.separator + "tetris_log" + File.separator + "rowsCleared_" + start + ".csv";
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			out.println(start + "," + end + "," + Integer.toString(rowsCleared));
			out.close();
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
			LOGGER.severe(e.getMessage());
		}
	}


	public void writeToLog(String trainingStart, String gameStart, String gameEnd, String text) {
		String filePath = System.getProperty("user.home") + File.separator + "tetris_log" + File.separator + "rowsCleared_" + trainingStart + ".csv";
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			out.println(gameStart + "," + gameEnd + "," + text);
			out.close();
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
			LOGGER.severe(e.getMessage());
		}
	}

	public static void main(String[] args) {
		int rowsCounter = 0;
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		String start = p.logGameStart();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int rowsCleared = s.getRowsCleared();
			if ((rowsCleared - rowsCounter) >= 100 && p.shouldLogEveryHundredRows) {
				p.logEveryHundredRows(rowsCleared);
				rowsCounter = rowsCleared;
			}
		}
		int rowsCleared = s.getRowsCleared();
		String end = p.logGameOver(rowsCleared, s.getField());
		p.writeClearedRows(rowsCleared, start, end);
	}


}

class InnerState extends State {

	// pWidth is protected so it's inherited
	private static int[][] pHeight;
	private static int[][][] pBottom;
	private static int[][][] pTop;

	private int[][] field;
	private int turn;
	private int[] top;
	private int cleared;


	// test if it's better to instantiate a new InnerState with field, or use setField to reuse the field again
	public InnerState(int nextPiece, int turnNumber, int[][] field, int[] top) {
		super();
		// static members
		pHeight = getpHeight();
		pBottom = getpBottom();
		pTop = getpTop();

		this.nextPiece = nextPiece;
		this.turn = turnNumber;

		int fieldRowLen = field.length;
		int fieldColLen = field[0].length;
		this.field = new int[fieldRowLen][fieldColLen];
		// copy the elements of external field matrix
		for (int i = 0; i < fieldRowLen; i++) {
			for (int j = 0; j < fieldColLen; j++) {
				this.field[i][j] = field[i][j];
			}
		}

		int topLen = top.length;
		this.top = new int[topLen];
		// copy the elements of external top array
		for (int i = 0; i < topLen; i++) {
			this.top[i] = top[i];
		}

		this.cleared = super.getRowsCleared();
	}

	/**
	 * A modified version of {@link State#makeMove(int, int)}. Makes a move based on an array of orient and slot
	 * @param move
	 * @return the number of rows cleared by making the move. If game over returns -1
	 */
	public int innerMakeMove(int[] move) {
		return innerMakeMove(move[ORIENT],move[SLOT]);
	}

	/**
	 * A modified version of {@link State#makeMove(int, int)}. Makes a move based on orient and slot
	 * @param orient
	 * @param slot
	 * @return the number of rows cleared by making the move. If game over returns -1
	 */
	public int innerMakeMove(int orient, int slot) {
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return -1;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}

		int rowsCleared = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		return rowsCleared;
	}

	@Override
	public int[] getTop() {
		return this.top;
	}
	@Override
	public int[][] getField() {
		return this.field;
	}
}

class MyFormatter extends SimpleFormatter {

	@Override
	public String format(LogRecord record){
		if(record.getLevel() == Level.INFO){
			return record.getMessage() + "\r\n";
		}else{
			return super.format(record);
		}
	}
}
