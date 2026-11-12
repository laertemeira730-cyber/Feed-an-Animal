package com.feedanimals.mod.util;

/**
 * Valores de balanceamento reunidos em um único lugar para facilitar ajustes.
 * O dinheiro inicial permanece em zero até a definição final do projeto.
 */
public final class EconomyRules {
    public static final int STARTING_MONEY = 0;
    public static final int STARTER_BASIC_FOOD = 8;

    public static final int BASIC_FOOD_PRICE = 5;
    public static final int RARE_FOOD_PRICE = 20;
    public static final int PREMIUM_FOOD_PRICE = 75;

    public static final int PEN_INCOME_INTERVAL_TICKS = 200;
    public static final int COINS_PER_ANIMAL = 1;

    private EconomyRules() {}
}
