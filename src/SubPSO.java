import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import jswarm_pso.SubSwarm;

public class SubPSO implements Runnable {
    private static final Logger LOGGER = Logger.getLogger( SubPSO.class.getName() );
    private int numberOfParticles;
    private int numberOfIterations;
    public static String info;

    public SubPSO(int numberOfParticles, int numberOfIterations) {
        this.numberOfParticles = numberOfParticles;
        this.numberOfIterations = numberOfIterations;
    }

    public void run() {
        // Create a subSwarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
        SubSwarm subSwarm = new SubSwarm(this.numberOfParticles, new MyParticle(), new MyFitnessFunction());

        // Use neighborhood
//        Neighborhood neigh = new Neighborhood1D(Swarm.DEFAULT_NUMBER_OF_PARTICLES  / 5, true);
//        subSwarm.setNeighborhood(neigh);
//        subSwarm.setNeighborhoodIncrement(0.9);

        // Set position (and velocity) constraints. I.e.: where to look for solutions
        subSwarm.setInertia(PSO.INERTIA_WEIGHT);
        subSwarm.setMaxPosition(PSO.MAX_POSITION);
        subSwarm.setMinPosition(PSO.MIN_POSITION);
        subSwarm.setMaxMinVelocity(PSO.MAXMIN_VELOCITY);
        subSwarm.setParticleIncrement(PSO.CONGITIVE_TERM_C1);
        subSwarm.setGlobalIncrement(PSO.SOCIAL_TERM_C2);

        for (int i = 0; i < numberOfIterations; i++) {
            subSwarm.evolve();
        }

        PSO.writeToCSV(subSwarm.toStringStats());

    }

}
