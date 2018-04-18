package jswarm_pso;

public class SubSwarm extends Swarm{

    public SubSwarm(int numberOfParticles, Particle sampleParticle, FitnessFunction fitnessFunction) {
        super(numberOfParticles, sampleParticle, fitnessFunction);
    }

    public void subEvaluate() {
        // Initialize (if not already done)
        if (particles == null) init();

        evaluate(); // Evaluate particles
    }

    public void subUpdate() {
        update(); // Update positions and velocities

        variablesUpdate.update(this);
    }

    public void evolve(double globalBestFitness, double[] globalBestPosition){
        if(globalBestFitness != -1) bestFitness = globalBestFitness;
        if(globalBestPosition != null) {
            // copy globalBestPosition in to the bestPosition of this SubSwarm
            if (bestPosition == null) bestPosition = new double[sampleParticle.getDimension()];
            for (int i = 0; i < globalBestPosition.length; i ++) {
                bestPosition[i] = globalBestPosition[i];

                // No need to change the best particle inside a subswarm
                // Just update the best position found so far.
                // NO need to change the best particle index either since it's not used for updating
                // particles[bestParticleIndex].position[i] = globalBestPosition[i];
                // particles[bestParticleIndex].bestPosition[i] = globalBestPosition[i];
            }

            subUpdate();
        }
        subEvaluate();
    }

}
