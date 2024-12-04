package util;

public interface BinomialProbability {

    /**
     * 
     * @param tentativeNumber Nombre total de tentatives
     * @param succesNumber Nombre minimum de succès souhaités
     * @param succesProbability Probabilité de succès lors d'une seule tentative
     * @return La probabilité que l'événement se produise au moins " + N + " fois après " + X + " tentatives
     */
    public static double calculateAtLeastN(int tentativeNumber, int succesNumber, double succesProbability) {
        double probability = 0.0;
        for (int k = succesNumber; k <= tentativeNumber; k++) {
            probability += binomialProbability(tentativeNumber, k, succesProbability);
        }
        return probability;
    }

    public static double binomialProbability(int tentativeNumber, int k, double succesProbability) {
        double q = 1 - succesProbability;
        return binomialCoefficient(tentativeNumber, k) * Math.pow(succesProbability, k) * Math.pow(q, tentativeNumber - k);
    }

    public static long binomialCoefficient(int n, int k) {
        if (k > n - k) {
            k = n - k;
        }
        long c = 1;
        for (int i = 0; i < k; i++) {
            c = c * (n - i) / (i + 1);
        }
        return c;
    }
}

