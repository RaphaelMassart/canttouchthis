import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import jswarm_pso.Swarm;

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
        // Create a swarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
        Swarm swarm = new Swarm(this.numberOfParticles, new MyParticle(), new MyFitnessFunction());

        // Use neighborhood
//        Neighborhood neigh = new Neighborhood1D(Swarm.DEFAULT_NUMBER_OF_PARTICLES  / 5, true);
//        swarm.setNeighborhood(neigh);
//        swarm.setNeighborhoodIncrement(0.9);

        // Set position (and velocity) constraints. I.e.: where to look for solutions
        swarm.setInertia(PSO.INERTIA_WEIGHT);
        swarm.setMaxPosition(PSO.MAX_POSITION);
        swarm.setMinPosition(PSO.MIN_POSITION);
        swarm.setMaxMinVelocity(PSO.MAXMIN_VELOCITY);
        swarm.setParticleIncrement(PSO.CONGITIVE_TERM_C1);
        swarm.setGlobalIncrement(PSO.SOCIAL_TERM_C2);

        for (int i = 0; i < numberOfIterations; i++) {
            swarm.evolve();
            swarm.getBestPosition();
        }

        PSO.writeToCSV(swarm.toStringStats());

    }

}
