package hybrid;

/**
 * Entry point of the program: creates the hybrid model, prints the identity cards of all models,
 * then simulates 300 seconds and prints a table of the results.
 */
public class Main {
    public static void main(String[] args) {
        // Creating the hybrid model also creates its two sub-models (physics and ML).
        HybridModel hybrid = new HybridModel();

        // Show the "ID cards" of all three models.
        System.out.println("=== Model Identity Cards ===");
        hybrid.print();
        hybrid.physics.print();
        hybrid.ml.print();

        System.out.println("\n=== Hybrid simulation ===");
        System.out.printf("%6s %7s %8s %10s %11s %10s%n",
                "time", "power", "ambient", "temp_sim", "residual", "temp_pred");

        double dt = 0.1; // time step in seconds (small steps keep the Euler method accurate)
        for (int i = 0; i <= 3000; i++) { // 3000 steps * 0.1 s = 300 s
            double time = i * dt;

            // Inputs of the experiment: 200 W for the first 150 s, then 50 W. Room at 20 degC.
            double power = time < 150 ? 200.0 : 50.0;
            double ambient = 20.0;

            // At time 0 we only read the initial state; afterwards we advance by dt each loop.
            HybridModel.Result r = (i == 0)
                    ? hybrid.evaluate(power, ambient)
                    : hybrid.step(power, ambient, dt);

            // Print only every 250th step (every 25 s) to keep the table short.
            if (i % 250 == 0) {
                System.out.printf("%6.1f %7.1f %8.1f %10.2f %11.3f %10.2f%n",
                        time, power, ambient, r.tempSim, r.residualCorrection, r.tempPred);
            }
        }
    }
}
