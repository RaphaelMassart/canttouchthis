// TODO Advanced heuristics: holes
// TODO 1 Forward looking
// TODO Genetic algo training

public class PlayerSkeleton {

	private double numHolesWeight = 14; //1.6;//7.6;//0.35663;
	private double bumpinessWeight = 6.1;//3.1;//0.184483;
	private double aggregateHeightWeight = 1.8;//0.8;//0.510066;
	private double rowsClearedWeight = 1.8;//0.8;//-0.760666;

	public int countHoles(InnerState s) {
		int hole_ct = 0;
		int rows_num = s.getField().length;
		int cols_num = s.getField()[0].length;

		for (int i = 0; i < rows_num; i++) { // iterate through rows
			for (int j = 0; j < cols_num; j++) { // iterate through columns

				if(!(i == rows_num - 1)) {
					if(s.getField()[i][j] == 0) {

						int block_idx = 1;

						// if there is some blockade above the current block, increment hole count
						for (block_idx = 1; block_idx < rows_num - i; block_idx++) {
							if (s.getField()[i+block_idx][j] != 0) {
								hole_ct++;
								break;
							}
						}
					}
				}
			}
		}
		return hole_ct;
	}

	public int calculateBumpiness(InnerState s) {
		int[] tops = s.getTop();
		int sum = 0;
		for (int i = 0; i < tops.length-1; i ++) {
			sum += Math.abs(tops[i] - tops[i+1]);
		}
		return sum;
	}


	public int aggregateHeight(InnerState s) {
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

		// System.out.println("holesCount: " + numHoles + " aggregateHeight: " + aggregateHeight);

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

			// make move
			//int rowsCleared =
			next.innerMakeMove(move); //don't return
			double currCost = oneLookAhead(next);

			// calculate cost of move
//			double currCost = rowsCleared == -1 ? Double.MAX_VALUE: calculateCost(next, rowsCleared);

			// System.out.println(currCost +  " " + minCost);
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

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
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
