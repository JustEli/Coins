package me.justeli.coins.command.api;

/**
 * Created by Eli on July 18, 2021.
 * Coins: me.justeli.coins.command.api
 */
public class Argument
{
    private final String name;
    private Class<?> type;
    private Object defaultValue;

    private Object rangeMin;
    private Object rangeMax;

    private String regex;
    private boolean greedy;

    private Argument (String name)
    {
        this.name = name;
    }

    public static Argument of (String name)
    {
        return new Argument(name);
    }

    public Argument type (Class<?> type)
    {
        this.type = type;
        return this;
    }

    public Argument value (Object def)
    {
        this.defaultValue = def;
        return this;
    }

    public Argument range (Object min, Object max)
    {
        this.rangeMin = min;
        this.rangeMax = max;
        return this;
    }

    public Argument regex (String regex)
    {
        this.regex = regex;
        return this;
    }

    public Argument greedy ()
    {
        this.greedy = true;
        return this;
    }
}
