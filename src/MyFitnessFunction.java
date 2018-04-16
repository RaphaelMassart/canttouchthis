import jswarm_pso.FitnessFunction;

public class MyFitnessFunction extends FitnessFunction {

	public int countHoles(State s) {
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
	
	public static int aggregateHeight(State s) {
		int[] tops = s.getTop();
		int sum = 0;
		for (int top: tops) {
			//top is row + 1, row is 0-based
			sum += top;
		}
		return sum;
	}

	public static int pileHeight(State s) {
		int[] tops = s.getTop();
		int max = 0;
		for (int top: tops) {
			//top is row + 1, row is 0-based
			if (top > max) {
				max = top;
			}
		}
		return max;
	}

    /**
     * Evaluates a particles at a given position
     * @param position : Particle's position
     * @return Fitness function for a particle
     */
    public double evaluate(double position[]) {

    	int numberOfEvaluations = 5;
		PlayerSkeleton p = new PlayerSkeleton(position, false, false, false);
		String start = p.logGameStart();
		int rowsCounter = 0;
		int totalHoles = 0;
		// max number of holes occurred in a state. Pick a single largest among the entire game
		int totalMaxHoles = 0;
		int totalAggregateHeight = 0;
		int totalPileHeight = 0;
		int totalRowsCleared = 0;
		int totalTurns = 0;

    	for (int i = 0; i < numberOfEvaluations; i++) {

			int maxHoles = 0;
			State s = new State();
			while (!s.hasLost()) {
				s.makeMove(p.pickMove(s, s.legalMoves()));
				int rowsCleared = s.getRowsCleared();
				int holes = countHoles(s);
				totalHoles += holes;
				maxHoles = holes > maxHoles ? holes : maxHoles;
				totalAggregateHeight += aggregateHeight(s);
				totalPileHeight += pileHeight(s);

				if ((rowsCleared - rowsCounter) >= 100 && p.getShouldLogEveryHundredRows()) {
					p.logEveryHundredRows(rowsCleared);
					rowsCounter = rowsCleared;
				}
			}
			totalMaxHoles += maxHoles;
			totalRowsCleared += s.getRowsCleared();
			totalTurns += s.getTurnNumber();
		}

        double finalAverageHoles = (double)totalHoles / totalTurns;
    	double finalAverageMaxHoles = (double)totalMaxHoles / numberOfEvaluations;
        double finalAverageAggregateHeight = (double)totalAggregateHeight / totalTurns;
        double finalAveragePileHeight = (double)totalPileHeight / totalTurns;
        double finalAverageRowsCleared = (double)totalRowsCleared / numberOfEvaluations;

		// Change this fitness function according to your task!!

		double holeFitness = (finalAverageMaxHoles - finalAverageHoles) / finalAverageMaxHoles;
		double maxHeightFitness = (20.0 - finalAveragePileHeight) / 20.0;

        double fitnessFunc = finalAverageRowsCleared + 500 * holeFitness + 500 * maxHeightFitness;

        String end = p.logEvaluationOver(finalAverageRowsCleared);

		StringBuilder sb = new StringBuilder();
		for (double weight : position) {
			sb.append(weight);
			sb.append(",");
		}

		// Change the stats according to your task!!
        String stats = finalAverageHoles
        		+ "," + finalAveragePileHeight
				+ "," + holeFitness
				+ "," + maxHeightFitness
        		+ ",weights," + sb.toString()
				+ "rows," + finalAverageRowsCleared;

        p.writeToCSV(PSO.info, PSO.startTime, start, end, stats);
        return fitnessFunc;
    }
}
