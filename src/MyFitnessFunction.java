import java.text.SimpleDateFormat;
import java.util.Date;

import jswarm_pso.FitnessFunction;

public class MyFitnessFunction extends FitnessFunction {

    /**
     * Evaluates a particles at a given position
     * @param position : Particle's position
     * @return Fitness function for a particle
     */
    public double evaluate(double position[]) {

    	int numberOfEvaluations = 5;
		PlayerSkeleton p = new PlayerSkeleton(position, false, false, false);
		String start = new SimpleDateFormat("MM-dd-HH.mm.ss").format( new Date() );
		int rowsCounter = 0;
		int totalHoles = 0;

		int totalAggregateHeight = 0;
		int totalPileHeight = 0;
		// max number of holes occurred in a state. Pick a single largest among the entire game
		int totalMaxHoles = 0;
		int totalMaxColTransitions = 0;
		int totalMaxRowTransitions = 0;
		int totalColTransitions = 0;
		int totalRowTransitions = 0;
		int totalRowsCleared = 0;
		int totalTurns = 0;

    	for (int i = 0; i < numberOfEvaluations; i++) {

			int maxHoles = 0;
			int maxColTransitions = 0;
			int maxRowTransitions = 0;
			State s = new State();
			while (!s.hasLost()) {
				s.makeMove(p.pickMove(s, s.legalMoves()));
				int rowsCleared = s.getRowsCleared();
				int field[][] = s.getField();
				int tops[] = s.getTop();

				totalAggregateHeight += p.totalHeight(tops);
				totalPileHeight += p.maxHeight(tops);

				int holes = p.countHoles(field);
				totalHoles += holes;
				maxHoles = Math.max(maxHoles, holes);

				int colTransitions = p.getColTransitions(field);
				totalColTransitions += colTransitions;
				maxColTransitions = Math.max(maxColTransitions, colTransitions);

				int rowTransitions = p.getRowTransitions(field);
				totalRowTransitions += rowTransitions;
				maxRowTransitions = Math.max(maxRowTransitions, rowTransitions);

				if ((rowsCleared - rowsCounter) >= 100 && p.getShouldLogEveryHundredRows()) {
					p.logEveryHundredRows(rowsCleared);
					rowsCounter = rowsCleared;
				}
			}
			totalMaxHoles += maxHoles;
			totalMaxColTransitions += maxColTransitions;
			totalMaxRowTransitions += maxRowTransitions;

			totalRowsCleared += s.getRowsCleared();
			totalTurns += s.getTurnNumber();
		}

		double averagePileHeight = (double)totalPileHeight / totalTurns;
		double averageRowsCleared = (double)totalRowsCleared / numberOfEvaluations;

        double averageHoles = (double)totalHoles / totalTurns;
    	double averageMaxHoles = (double)totalMaxHoles / numberOfEvaluations;
//        double averageAggregateHeight = (double)totalAggregateHeight / totalTurns;

		double averageColTransitions = (double)totalColTransitions / totalTurns;
		double averageMaxColTransitions = (double)totalMaxColTransitions / numberOfEvaluations;
		double averageRowTransitions = (double)totalRowTransitions / totalTurns;
		double averageMaxRowTransitions = (double)totalMaxRowTransitions / numberOfEvaluations;

		// Change this fitness function according to your task!!
		double maxHeightRatio = (20.0 - averagePileHeight) / 20.0;
		double holeRatio = (averageMaxHoles - averageHoles) / averageMaxHoles;
		double colTransitionRatio = (averageMaxColTransitions - averageColTransitions) / averageMaxColTransitions;
		double rowTransitionRatio = (averageMaxRowTransitions - averageRowTransitions) / averageMaxRowTransitions;

        double fitnessFunc = averageRowsCleared + 500 * holeRatio + 500 * maxHeightRatio + 500 * colTransitionRatio + 500 * rowTransitionRatio;

        String end = new SimpleDateFormat("MM-dd-HH.mm.ss").format( new Date() );

		StringBuilder sb = new StringBuilder();
		for (double weight : position) {
			sb.append(weight);
			sb.append(",");
		}

		// Change the stats according to your task!!
        String stats = averageHoles
        		+ "," + maxHeightRatio
				+ "," + holeRatio
				+ "," + colTransitionRatio
				+ "," + rowTransitionRatio
        		+ ",weights," + sb.toString()
				+ "rows," + averageRowsCleared;

        p.writeToCSV(PSO.info, PSO.startTime, start, end, stats);
        return fitnessFunc;
    }
}
