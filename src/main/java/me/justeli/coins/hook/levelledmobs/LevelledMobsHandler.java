package me.justeli.coins.hook.levelledmobs;

import me.justeli.coins.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class LevelledMobsHandler {
    public LevelledMobsHandler(){
        Plugin lmPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        this.isInstalled = lmPlugin != null && Bukkit.getPluginManager().isPluginEnabled("LevelledMobs");
        if (this.isInstalled)
            this.levelKey = new NamespacedKey(lmPlugin, "level");
    }

    private final boolean isInstalled;
    private NamespacedKey levelKey;
    public LivingEntity lastKilledMob;

    public boolean getIsInstalled(){
        return this.isInstalled;
    }

    public int getMobLevel(LivingEntity livingEntity){
        if (!this.isInstalled || this.levelKey == null ||
            !livingEntity.getPersistentDataContainer().has(this.levelKey, PersistentDataType.INTEGER))
            return 0;

        Integer mobLevel = livingEntity.getPersistentDataContainer().get(this.levelKey, PersistentDataType.INTEGER);

        return mobLevel != null ?
                mobLevel : 0;
    }

    public double getLevelledMobsMultipliedAmount(double increment){
        if (lastKilledMob == null || !getIsInstalled())
            return 0.0;

        if (Config.LEVELLEDMOBS_LEVEL_MULTIPLIER == 0.0) return 0.0;

        int mobLevel = getMobLevel(lastKilledMob);
        if (mobLevel <= 1) return 0.0;

        return increment + Config.LEVELLEDMOBS_LEVEL_MULTIPLIER * (double) mobLevel;
    }
}
