package me.justeli.coins.cancel;

import io.papermc.lib.PaperLib;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Eli on 2/4/2017.
 */

public class CoinPlace
        implements Listener
{
    @EventHandler
    public void coinPlace (PlayerInteractEvent e)
    {
        if (!e.getAction().equals(Action.PHYSICAL)
                && e.getItem() != null
                && e.getItem().hasItemMeta()
                && e.getItem().getItemMeta() != null
                && e.getItem().getItemMeta().hasDisplayName())
        {
            Player p = e.getPlayer();

            String pickupName = e.getItem().getItemMeta().getDisplayName();
            if (pickupName.endsWith(Settings.getCoinName() + Settings.hS.get(Config.STRING.multiSuffix)))
            {
                // because of .setAmount(0) AND Container, have to drop coin instead
                if (PaperLib.getMinecraftVersion() < 9 || !p.hasPermission("coins.withdraw"))
                {
                    e.setCancelled(true);
                    return;
                }

                if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Container))
                {
                    e.setCancelled(true);
                    int multi = e.getItem().getAmount();

                    // doesn't work on 1.8
                    e.getItem().setAmount(0);

                    double amount = Integer.parseInt(ChatColor.stripColor(pickupName.split(" ")[0]));
                    CoinsPickup.addMoney(p, amount * multi, 0);

                    playSound(p);
                }
            }
        }
    }

    public static void playSound (Player p)
    {
        try
        {
            String sound = Settings.hS.get(Config.STRING.soundName);

            Sound playSound = Sound.valueOf(PaperLib.getMinecraftVersion() < 9 && (sound.equals("BLOCK_LAVA_POP") || sound
                    .equals("ITEM_ARMOR_EQUIP_GOLD"))? "NOTE_STICKS" : sound.toUpperCase());

            float volume = Settings.hD.get(Config.DOUBLE.soundVolume).floatValue();
            float pitch = Settings.hD.get(Config.DOUBLE.soundPitch).floatValue();

            p.playSound(p.getEyeLocation(), playSound, volume == 0? 0.3f : volume, pitch == 0? 0.3f : pitch);
        }
        catch (IllegalArgumentException ex)
        {
            Settings.errorMessage(Settings.Msg.NO_SUCH_SOUND, new String[]{ex.getMessage()});
        }
    }

}
