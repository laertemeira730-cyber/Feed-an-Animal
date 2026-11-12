package com.feedanimals.mod.client;

public final class ClientGameData {
    public static int money;
    public static int throwPower;
    public static int animalTokens;
    public static int penAnimals;
    public static int phaseCount;
    public static int completedPhases;
    private static String phaseFlags = "";

    private ClientGameData() {}

    public static void apply(int newMoney, int newThrowPower, int newAnimalTokens,
                             int newPenAnimals, int newPhaseCount, int newCompletedPhases,
                             String flags) {
        money = newMoney;
        throwPower = newThrowPower;
        animalTokens = newAnimalTokens;
        penAnimals = newPenAnimals;
        phaseCount = newPhaseCount;
        completedPhases = newCompletedPhases;
        phaseFlags = flags == null ? "" : flags;
    }

    public static boolean isPhaseDone(int index) {
        return index >= 0 && index < phaseFlags.length() && phaseFlags.charAt(index) == '1';
    }
}
