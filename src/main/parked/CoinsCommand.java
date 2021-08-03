package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.command.api.Argument;
import me.justeli.coins.command.api.Command;
import me.justeli.coins.config.Message;
import me.justeli.coins.config.Settings;
import me.justeli.coins.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Eli on July 18, 2021.
 * Coins: me.justeli.coins.command
 */
public class CoinsCommand
{
    private final Command command;

    public CoinsCommand ()
    {
        command = Command.of("coins", "coin");

        command.help();

        command
                .argument("reload", "refresh")
                .permission("coins.reload")
                .queue(details ->
                {
                    CommandSender sender = details.sender();
                    long current = System.currentTimeMillis();

                    Util.resetMultiplier();
                    int errors = Settings.reload();

                    sender.sendMessage(Message.RELOAD_SUCCESS.replace(Long.toString(System.currentTimeMillis() - current)));
                    sender.sendMessage((errors > 0? Message.MINOR_ISSUES : Message.CHECK_SETTINGS).toString());
                });

        command
                .argument("drop")
                .argument(Argument.of("player").type(Player.class), Argument.of("location").type(Location.class))
                .argument(Argument.of("amount").type(Integer.class))
                .argument(Argument.of("radius").type(Integer.class).value(20))
                .permission("coins.drop")
                .complete(details ->
                {

                });

        command
                .argument("remove")
                .argument(Argument.of("radius").type(Integer.class), "all")
                .permission("coins.remove")
                .complete(details ->
                {

                });

        command
                .argument("settings", "config")
                .permission("coins.settings")
                .queue(details ->
                {
                    details.sender().sendMessage(Message.LOADED_SETTINGS.toString());
                    for (String setting : Settings.get())
                    {
                        details.sender().sendMessage(setting);
                    }
                });

        command
                .argument("version", "update")
                .permission("coins.version")
                .queue(details ->
                {
                    CommandSender sender = details.sender();

                    String latest = Coins.latest();
                    String current = Coins.plugin().getDescription().getVersion();

                    sender.sendMessage(Util.color("&eVersion currently installed: &f" + current));
                    sender.sendMessage(Util.color("&eLatest released version: &f" + latest));

                    if (latest.equals(current))
                    {
                        sender.sendMessage(Util.color("&aYou're up to date with version " + current + "."));
                    }
                    else if (latest.equals("Unknown"))
                    {
                        sender.sendMessage(Util.color("&7Couldn't get the latest version of Coins."));
                    }
                    else
                    {
                        sender.sendMessage(Util.color("&cConsider to update the plugin to version " + latest + "."));
                        sender.sendMessage(ChatColor.BLUE + "https://www.spigotmc.org/resources/coins.33382/");
                    }
                });

        command
                .argument("toggle")
                .permission("coins.toggle")
                .queue(details ->
                {
                    CommandSender sender = details.sender();
                    String abled = Coins.toggleDisabled()? "&aenabled" : "&cdisabled";

                    sender.sendMessage(Util.color("&eCoins has been globally " + abled + "&e. Toggle with &f/coins toggle&e."));
                    if (Coins.isDisabled())
                    {
                        sender.sendMessage(Util.color("&eWhen disabled, coins will not drop and withdrawing coins isn't possible. Picking up coins " +
                                "that were already on the ground, and depositing coins is still possible."));
                    }
                });

        command
                .argument("language", "lang")
                .permission("coins.language")
                .queue(details ->
                {
                    for (Message message : Message.values())
                    {
                        details.sender().sendMessage(message.toString());
                    }
                });

        register();
    }

    public void register ()
    {
        command.register(Coins.plugin());
    }

    public void unregister ()
    {
        command.unregister();
    }
}
