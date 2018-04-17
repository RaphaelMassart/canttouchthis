package jswarm_pso;

public class SubSwarm extends Swarm{

    public SubSwarm(int numberOfParticles, Particle sampleParticle, FitnessFunction fitnessFunction) {
        super(numberOfParticles, sampleParticle, fitnessFunction);
    }

    public void subEvaluate() {
        // Initialize (if not already done)
        if (particles == null) init();

        evaluate(); // Evaluate particles
//        return getBestPosition();
    }

    public void subUpdate() {
        update(); // Update positions and velocities

        variablesUpdate.update(this);
    }

    public void evolve(double[] globalBestPosition){
        Thread.currentThread().getName();
        if(globalBestPosition != null) {
            this.setBestPosition(globalBestPosition);
            particles[bestParticleIndex].copyPosition(globalBestPosition);
            subUpdate();
        }
        subEvaluate();
    }

}
