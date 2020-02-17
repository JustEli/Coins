package me.justeli.coins.api;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eli on 6 jan. 2020.
 * spigotPlugins: me.justeli.coins.api
 */
public class Extras
{
    private static final HashMap<UUID, Double> multiplier = new HashMap<>();

    public static void resetMultiplier ()
    {
        multiplier.clear();
    }

    public static double getMultiplier (Player p)
    {
        if (!multiplier.containsKey(p.getUniqueId()))
        {
            List<Double> permissions = new ArrayList<>();
            for (PermissionAttachmentInfo permissionInfo : p.getEffectivePermissions())
            {
                String permission = permissionInfo.getPermission();
                if (permission.startsWith("coins.multiplier."))
                {
                    String number = permission.replace("coins.multiplier.", "");
                    permissions.add(Double.parseDouble(number));
                }
            }
            multiplier.put(p.getUniqueId(), permissions.size() == 0? 1d : Collections.max(permissions));
        }
        return multiplier.get(p.getUniqueId());
    }
}
