import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.sourceforge.jswarm_pso.Swarm;
import net.sourceforge.jswarm_pso.example_2.SwarmShow2D;

public class PSO {
    private static final Logger LOGGER = Logger.getLogger( PSO.class.getName() );
    private static final double INERTIA_WEIGHT = 0.72;
    private static final double CONGITIVE_TERM_C1 = 1.42;
    private static final double SOCIAL_TERM_C2 = 1.42;
    private static final double MAX_POSITION = 1;
    private static final double MIN_POSITION = -1;
    private static final double MAXMIN_VELOCITY = 0.5;
    public static String startTime;
    public static int numberOfParticles;
    public static int numberOfIterations;
    public static String info;

    public static void writeToCSV(String text) {

        String filePath = System.getProperty("user.home") + File.separator + "tetris_log" + File.separator  + PSO.info + "_" + PSO.startTime + ".csv";
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
            out.println(text);
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
            LOGGER.severe(e.getMessage());
        }
    }

    //-------------------------------------------------------------------------
    // Main
    //-------------------------------------------------------------------------
    public static void main(String[] args) {
        int firstArg = 0;
        int secondArg = 0;
        if (args.length > 0) {
            try {
                firstArg = Integer.parseInt(args[0]);
                secondArg = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer for number of particles.");
                System.err.println("Argument" + args[1] + " must be an integer for number of iterations.");
                System.exit(1);
            }
        }

        Swarm.DEFAULT_NUMBER_OF_PARTICLES = firstArg == 0 ? Swarm.DEFAULT_NUMBER_OF_PARTICLES : firstArg;
        PSO.numberOfIterations = secondArg == 0 ? 25 : secondArg;
        PSO.numberOfParticles = Swarm.DEFAULT_NUMBER_OF_PARTICLES;
        PSO.info = "PSO_" +  numberOfParticles + "_" + numberOfIterations;

        int numberOfIterations = PSO.numberOfIterations;

        System.out.println("Begin: PSO\n");
        PSO.startTime = new SimpleDateFormat("MM-dd-HH.mm.ss").format( new Date() );

        // Create a swarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
        Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES , new MyParticle(), new MyFitnessFunction());


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

        boolean showGraphics = false;

        if (showGraphics) {
            int displayEvery = numberOfIterations / 100 + 1;
            SwarmShow2D ss2d = new SwarmShow2D(swarm, numberOfIterations, displayEvery, true);
            ss2d.run();
        } else {
            // Optimize (and time it)
            for (int i = 0; i < numberOfIterations; i++)
                swarm.evolve();
        }

        PSO.writeToCSV(swarm.toStringStats());

    }

}
