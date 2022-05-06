package me.justeli.coins.hook.levelledmobs;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class LevelledMobsHandler {
    public LevelledMobsHandler(){
        Plugin lmPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        this.isInstalled = lmPlugin != null;
        if (this.isInstalled)
            this.levelKey = new NamespacedKey(lmPlugin, "level");
    }

    private final boolean isInstalled;
    private NamespacedKey levelKey;

    public boolean getIsInstalled(){
        return this.isInstalled;
    }

    public int getMobLevel(@NotNull LivingEntity livingEntity){
        if (!this.isInstalled || this.levelKey == null ||
            !livingEntity.getPersistentDataContainer().has(this.levelKey, PersistentDataType.INTEGER))
            return 0;

        Integer mobLevel = livingEntity.getPersistentDataContainer().get(this.levelKey, PersistentDataType.INTEGER);

        return mobLevel != null ?
                mobLevel : 0;
    }
}
