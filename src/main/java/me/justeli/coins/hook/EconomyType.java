package me.justeli.coins.hook;

import me.lokka30.treasury.api.economy.EconomyProvider;
import net.milkbowl.vault.economy.Economy;

/** by Eli on February 02, 2022 **/
public enum EconomyType
{
    TREASURY ("Treasury"),
    VAULT ("Vault"),
    ;

    private final String name;

    EconomyType (String name)
    {
        this.name = name;
    }

    public String pluginName ()
    {
        return name;
    }

    public Class<?> clazz ()
    {
        switch (this)
        {
            case VAULT: return Economy.class;
            case TREASURY: return EconomyProvider.class;
        }
        return null;
    }
}
