import net.sourceforge.jswarm_pso.Neighborhood;
import net.sourceforge.jswarm_pso.Neighborhood1D;
import net.sourceforge.jswarm_pso.Swarm;
import net.sourceforge.jswarm_pso.example_2.SwarmShow2D;

public class PSO {

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

        int numberOfParticles = firstArg == 0 ? Swarm.DEFAULT_NUMBER_OF_PARTICLES : firstArg;
        int numberOfIterations = secondArg == 0 ? 25 : secondArg;

        System.out.println("Begin: PSO\n");

        // Create a swarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
        Swarm swarm = new Swarm(numberOfParticles, new MyParticle(), new MyFitnessFunction(false));

        // Use neighborhood
        Neighborhood neigh = new Neighborhood1D(numberOfParticles / 5, true);
        swarm.setNeighborhood(neigh);
        swarm.setNeighborhoodIncrement(0.9);

        // Set position (and velocity) constraints. I.e.: where to look for solutions
        swarm.setInertia(0.95);
        swarm.setMaxPosition(1);
        swarm.setMinPosition(0);
        swarm.setMaxMinVelocity(0.1);

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

        // Print results
        System.out.println(swarm.toStringStats());
        System.out.println("End: Example 1");
    }
}
