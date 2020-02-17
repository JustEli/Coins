package me.justeli.coins.main;

import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eli on 26 dec 2018.
 * spigotPlugins: me.justeli.coins.main
 */
public class TabComplete
        implements TabCompleter
{
    @Override
    public List<String> onTabComplete (CommandSender sender, Command command, String alias, String[] args)
    {
        if (alias.equalsIgnoreCase("withdraw") && args.length == 1)
        {
            List<String> numbers = new ArrayList<>();
            numbers.add("1-" + Settings.hD.get(Config.DOUBLE.maxWithdrawAmount).intValue());
            Collections.sort(numbers);
            return numbers;
        }

        else if (alias.equalsIgnoreCase("coins"))
        {
            List<String> list = new ArrayList<>();
            if (args.length == 1)
            {
                if (sender.hasPermission("coins.drop"))
                    list.add("drop");
                if (sender.hasPermission("coins.admin"))
                {
                    list.add("reload");
                    list.add("settings");
                    list.add("version");
                }
                if (sender.hasPermission("coins.remove"))
                    list.add("remove");
            }
            else if (args.length == 2)
            {
                if (args[0].equalsIgnoreCase("remove"))
                    if (sender.hasPermission("coins.remove"))
                    {
                        list.add("all");
                        list.add("[radius]");
                    }
                if (args[0].equalsIgnoreCase("drop"))
                {
                    for (Player p : Bukkit.getOnlinePlayers())
                        list.add(p.getName());
                    list.add("<x,y,z>");
                    list.add("<x,y,z,world>");
                }
            }
            else if (sender.hasPermission("coins.remove"))
            {
                if (args.length == 3)
                    list.add("<amount>");
                if (args.length == 4)
                    list.add("[radius]");
            }

            Collections.sort(list);
            return list;
        }

        return new ArrayList<>();
    }
}
