import net.sourceforge.jswarm_pso.FitnessFunction;

public class MyFitnessFunction extends FitnessFunction {

    /**
     * Evaluates a particles at a given position
     * @param position : Particle's position
     * @return Fitness function for a particle
     */
	public static int countHoles(State s) {
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
	
	public static int aggregateHeight(State s) {
		int[] tops = s.getTop();
		int sum = 0;
		for (int top: tops) {
			//top is row + 1, row is 0-based
			sum += top;
		}
		return sum;
	}

	
    public double evaluate(double position[]) {
        int rowsCounter = 0;
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(position, false, false, false);
        String start = p.logGameStart();
        
        int totalHoles = 0;
        int maxHeight = 0;
        
        while(!s.hasLost()) {
            s.makeMove(p.pickMove(s,s.legalMoves()));
            int rowsCleared = s.getRowsCleared();
            totalHoles += countHoles(s);
            int currHeight = aggregateHeight(s);
            if (currHeight > maxHeight) {
            	maxHeight = currHeight;
            }
            if ((rowsCleared - rowsCounter) >= 100 && p.getShouldLogEveryHundredRows()) {
                p.logEveryHundredRows(rowsCleared);
                rowsCounter = rowsCleared;
            }
        }
        int rowsCleared = s.getRowsCleared();
        int finalTotalHoles = totalHoles;
        int finalMaxHeight = maxHeight;
        
        int fitnessFunc = rowsCleared + finalTotalHoles + finalMaxHeight;
        String end = p.logGameOver(rowsCleared, s.getField());
        p.writeClearedRows(rowsCleared, start, end);
        return fitnessFunc;
    }
}
