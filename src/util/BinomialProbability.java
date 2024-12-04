package util;

public interface BinomialProbability {

    /**
     * 
     * @param tentativeNumber Nombre total de tentatives
     * @param succesNumber Nombre minimum de succ�s souhait�s
     * @param succesProbability Probabilit� de succ�s lors d'une seule tentative
     * @return La probabilit� que l'�v�nement se produise au moins " + N + " fois apr�s " + X + " tentatives
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

