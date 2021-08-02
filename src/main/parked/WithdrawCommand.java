package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.command.api.Argument;
import me.justeli.coins.command.api.Command;
import me.justeli.coins.config.Config;
import me.justeli.coins.config.Message;
import me.justeli.coins.item.Coin;
import me.justeli.coins.util.ActionBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Eli on July 18, 2021.
 * Coins: me.justeli.coins.command
 */
public class WithdrawCommand
{
    private final Command command;

    public WithdrawCommand ()
    {
        this.command = Command.of("withdraw");

        command
                .argument(Argument.of("worth").type(Integer.class))
                .argument(Argument.of("amount").type(Integer.class).value(1))
                .permission("coins.withdraw")
                .require(Player.class)
                .complete(details ->
        {
            Player player = details.sender();

            if (Coins.isDisabled() || Config.DISABLED_WORLDS.contains(player.getWorld().getName()))
            {
                player.sendMessage(Message.COINS_DISABLED.toString());
                return;
            }

            if (player.getInventory().firstEmpty() == -1)
            {
                player.sendMessage(Message.INVENTORY_FULL.toString());
                return;
            }

            int worth = details.argument("worth");
            int amount = details.argument("amount");

            int total = worth * amount;

            if (worth <= 0 || amount <= 0 || total <= 0 || amount > 64)
            {
                player.sendMessage(Message.INVALID_AMOUNT.toString());
                return;
            }

            if (worth > Config.MAX_WITHDRAW_AMOUNT || !Coins.economy().has(player, total))
            {
                player.sendMessage(Message.NOT_THAT_MUCH.toString());
            }

            ItemStack coin = new Coin().withdraw(worth).item();
            coin.setAmount(amount);

            player.getInventory().addItem(coin);
            Coins.economy().withdrawPlayer(player, total);

            player.sendMessage(Message.WITHDRAW_COINS.toString().replace("{0}", Long.toString(total)));
            ActionBar.of(Config.DEATH_MESSAGE.replace("%amount%", String.valueOf(total))).send(player);
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
