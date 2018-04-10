import net.sourceforge.jswarm_pso.FitnessFunction;

public class MyFitnessFunction extends FitnessFunction {


    /**
     * Evaluates a particles at a given position
     * @param position : Particle's position
     * @return Fitness function for a particle
     */
    public double evaluate(double position[]) {
        int rowsCounter = 0;
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(position, false, false, false);
        String start = p.logGameStart();
        while(!s.hasLost()) {
            s.makeMove(p.pickMove(s,s.legalMoves()));
            int rowsCleared = s.getRowsCleared();
            if ((rowsCleared - rowsCounter) >= 100 && p.getShouldLogEveryHundredRows()) {
                p.logEveryHundredRows(rowsCleared);
                rowsCounter = rowsCleared;
            }
        }
        int rowsCleared = s.getRowsCleared();
        String end = p.logGameOver(rowsCleared, s.getField());
        p.writeClearedRows(rowsCleared, start, end);
        return rowsCleared;
    }

}
