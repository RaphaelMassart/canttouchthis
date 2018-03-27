public class PlayerSkeleton {

	public int holes(InnerState s) {
		int hole_ct = 0;
		int rows_num = s.getField().length;
		int cols_num = s.getField()[0].length;

		for (int i = 0; i < rows_num; i++) { // iterate thru rows
			for (int j = 0; j < cols_num; j++) { // iterate thru columns

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


	public int aggregateHeight(InnerState s) {
		int[] tops = s.getTop();
		int sum = 0;
		for (int top: tops) {
			//top is row + 1, row is 0-based
			sum += top;
		}
		return sum;
	}

	public int calculateCost(InnerState s) {
		int cost = 0;

		int numHoles = holes(s);
		int aggregateHeight = aggregateHeight(s);

		System.out.println("holes: " + numHoles + " height: " + aggregateHeight);

		cost = numHoles + aggregateHeight;
		return cost;
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		int minCost = Integer.MAX_VALUE;
		int moveIdx = 0;

		// iterate thru legal moves
		System.out.println("move=================");
		for (int i = 0; i < legalMoves.length; i++) {
			InnerState next = new InnerState(s.getField(), s.nextPiece, s.getTop());
			int currCost;

			// get move
			int[] move = legalMoves[i];

			// make move
			boolean result = next.innerMakeMove(move);

			// calculate cost of move
			if (result == false) {
				currCost = Integer.MAX_VALUE;
			}
			else {
				currCost = calculateCost(next);
			}
			System.out.println(currCost +  " " + minCost);
			if (currCost < minCost) {
				minCost = currCost;
				moveIdx = i;
			}

		}
		return moveIdx;
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}


}

class InnerState extends State {

	private int[][] innerField;
	private int innerTurn;
	private int[] innerTop;
	private int innerCleared;

	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	private static int[][][] pBottom = {
			{{0,0}},
			{{0},{0,0,0,0}},
			{{0,0},{0,1,1},{2,0},{0,0,0}},
			{{0,0},{0,0,0},{0,2},{1,1,0}},
			{{0,1},{1,0,1},{1,0},{0,0,0}},
			{{0,0,1},{1,0}},
			{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
			{{2,2}},
			{{4},{1,1,1,1}},
			{{3,1},{2,2,2},{3,3},{1,1,2}},
			{{1,3},{2,1,1},{3,3},{2,2,2}},
			{{3,2},{2,2,2},{2,3},{1,2,1}},
			{{1,2,2},{3,2}},
			{{2,2,1},{2,3}}
	};

	// test if it's better to instantiate a new InnerState with field, or use setField to reuse the field again
	public InnerState(int[][] field, int nextPiece, int[] top) {
		super();
		this.innerField = new int[field.length][field[0].length];
		for( int i = 0; i < field.length; i++) {
			for ( int j = 0; j < field[i].length; j++) {
				this.innerField[i][j] = field[i][j];
			}
		}
		this.innerTurn = super.getTurnNumber();
		this.innerTop = new int[top.length];
		for( int i = 0; i < top.length; i++) {
			this.innerTop[i] = top[i];
		}
		this.innerCleared = super.getRowsCleared();
		this.nextPiece = nextPiece;
	}

//	public void setInnerField(int[][] field) {
//		// check if it points to the State field, if yes double for loop
//		this.innerField = field;
//	}

	//make a move based on an array of orient and slot
	public boolean innerMakeMove(int[] move) {
		return makeMove(move[ORIENT],move[SLOT]);
	}

	//returns false if you lose - true otherwise
	@Override
	public boolean makeMove(int orient, int slot) {
		this.innerTurn++;
		//height if the first column makes contact
		int height = innerTop[slot]-pBottom[nextPiece][orient][0];
		// System.out.println("height: " + height);
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,innerTop[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return false;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				// System.out.println(height + " " + h + " " + i+slot + " " + innerTurn);
				innerField[h][i+slot] = innerTurn;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			innerTop[slot+c]=height+pTop[nextPiece][orient][c];
		}

		int rowsCleared = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(innerField[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				innerCleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < innerTop[c]; i++) {
						innerField[i][c] = innerField[i+1][c];
					}
					//lower the top
					innerTop[c]--;
					while(innerTop[c]>=1 && innerField[innerTop[c]-1][c]==0)	innerTop[c]--;
				}
			}
		}
//		//pick a new piece
//		nextPiece = randomPiece();
		return true;
	}

	@Override
	public int[] getTop() {
		return this.innerTop;
	}
	@Override
	public int[][] getField() {
		return this.innerField;
	}
}
