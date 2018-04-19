import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;

import jswarm_pso.FitnessFunction;
import jswarm_pso.SubSwarm;
import jswarm_pso.Swarm;

public class PSO {
    private static final Logger LOGGER = Logger.getLogger( PSO.class.getName() );
    private static final FitnessFunction FITNESS_FUNCTION = new MyFitnessFunction();

    private int numberOfParticles;
    private int numberOfIterations;
    private int numberOfThreads = 3;

    private CyclicBarrier cyclicBarrier;
    private static double[] globalBestPosition = null;
    private static double globalBestFitness = -1;
    private static final Object GLOBAL_LOCK = new Object();

    static final double INERTIA_WEIGHT = 0.72;
    static final double CONGITIVE_TERM_C1 = 1.42;
    static final double SOCIAL_TERM_C2 = 1.42;
    static final double MAX_POSITION = 3;
    static final double MIN_POSITION = -3;
    static final double MAXMIN_VELOCITY = 0.5;
    static final int PARTICLE_DIMENSION= 8;
    static String info;
    static String startTime;

    public PSO(int numberOfParticles, int numberOfIterations, int numberOfThreads) {
        this.numberOfParticles = numberOfParticles;
        this.numberOfIterations = numberOfIterations;
        this.numberOfThreads = numberOfThreads;
    }

    public static synchronized void writeToCSV(String text) {

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

    public static void main(String[] args) {
        int firstArg = 0;
        int secondArg = 0;
        int thirdArg = 0;
        if (args.length > 0) {
            try {
                firstArg = Integer.parseInt(args[0]);
                secondArg = Integer.parseInt(args[1]);
                thirdArg = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer for number of particles.");
                System.err.println("Argument" + args[1] + " must be an integer for number of iterations.");
                System.err.println("Argument" + args[2] + " must be an integer for number of threads.");
                System.exit(1);
            }
        }
        int numberOfParticles = firstArg == 0 ? Swarm.DEFAULT_NUMBER_OF_PARTICLES : firstArg;
        int numberOfIterations = secondArg == 0 ? 100 : secondArg;
        int numberOfThreads = thirdArg == 0 ? 1 : thirdArg;
        PSO pso = new PSO(numberOfParticles, numberOfIterations, numberOfThreads);
        pso.train();
    }


    public void train() {
        PSO.info = "PSO_" +  numberOfParticles + "_" + numberOfIterations;
        PSO.startTime = new SimpleDateFormat("MM-dd-HH.mm.ss").format( new Date() );

        int numberOfSubParticles = this.numberOfParticles / this.numberOfThreads;
        int numberOfIterations = this.numberOfIterations;

        this.cyclicBarrier = new CyclicBarrier(this.numberOfThreads);


        System.out.println("Begin: PSO\n" + "number of parties:" + cyclicBarrier.getParties());

        for (int i = 0; i< numberOfThreads; i ++) {
            Thread worker = new Thread(new SubPSO(numberOfSubParticles, numberOfIterations));
            worker.setName("T" + i);
            worker.start();
        }

    }

    class SubPSO implements Runnable {
        private int numberOfSubParticles;
        private int numberOfIterations;
//        public static String info;

        public SubPSO(int numberOfSubParticles, int numberOfIterations) {
            this.numberOfSubParticles = numberOfSubParticles;
            this.numberOfIterations = numberOfIterations;
        }

        public void run() {
            // Create a subSwarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
            SubSwarm subSwarm = new SubSwarm(this.numberOfSubParticles, new MyParticle(), new MyFitnessFunction());

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

            for (int i = 0; i < this.numberOfIterations; i ++) {

//                double[] bestPosition = subSwarm.getBestPosition();

//                String threadName = Thread.currentThread().getName();
//
//                String text = "";
//                String text2 = "";
//                if (bestPosition != null) {
//                    for (int j = 0; j < PSO.PARTICLE_DIMENSION; j++)
//                        text += bestPosition[j] + ",";
//                }
//                if (PSO.globalBestPosition != null) {
//                    for (int j = 0; j < PSO.PARTICLE_DIMENSION; j++)
//                        text2 += PSO.globalBestPosition[j] + ",";
//                }
//                logInfo(threadName, i + "-before fitness," + subSwarm.getBestFitness() + ",global," + PSO.globalBestFitness + ",weights," + text + ",global," + text2);

                //evolve first update, then evaluate
                subSwarm.evolve(PSO.globalBestFitness, PSO.globalBestPosition);

                double[] bestPosition = subSwarm.getBestPosition();
                double fit = subSwarm.getBestFitness();

//                text = "";
//                text2 = "";
//
//                for (int j = 0; j < PSO.PARTICLE_DIMENSION; j++)
//                    text += bestPosition[j] + ",";
//
//                if (PSO.globalBestPosition != null) {
//                    for (int j = 0; j < PSO.PARTICLE_DIMENSION; j++)
//                        text2 += PSO.globalBestPosition[j] + ",";
//                }
//                logInfo(threadName, i + "-after fitness," + subSwarm.getBestFitness() + ",global," + PSO.globalBestFitness + ",weights," + text + ",global," + text2);


                synchronized (PSO.GLOBAL_LOCK) {

                    if (PSO.globalBestFitness == -1 || PSO.FITNESS_FUNCTION.isBetterThan(PSO.globalBestFitness, fit)) {
                        PSO.globalBestFitness = fit; // Copy best fitness, index, and position vector
                        if (PSO.globalBestPosition == null) PSO.globalBestPosition = new double[PSO.PARTICLE_DIMENSION];
                        for (int j = 0; j < PSO.PARTICLE_DIMENSION; j++)
                            PSO.globalBestPosition[j] = bestPosition[j];
                    }
                }

                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    System.out.print(e.getMessage());
                } catch (BrokenBarrierException e) {
                    System.out.print(e.getMessage());
                }

            }

            PSO.writeToCSV(Thread.currentThread().getName() + subSwarm.toStringStats());
            PSO.writeToCSV(globalToStringStats());

        }

        public void logInfo(String threadName, String text) {
            PSO.writeToCSV(threadName + "," + text);
        }

        public String globalToStringStats() {
            String stats = "";
            if (!Double.isNaN(globalBestFitness)) {
                stats += "Best fitness: " + globalBestFitness + "\nBest position: \t[";
                for (int i = 0; i < globalBestPosition.length; i++)
                    stats += globalBestPosition[i] + (i < (globalBestPosition.length - 1) ? ", " : "");
                stats += "]\nNumber of evaluations: " + numberOfIterations * numberOfParticles + "\n";
            }
            return stats;
        }
    }
}
