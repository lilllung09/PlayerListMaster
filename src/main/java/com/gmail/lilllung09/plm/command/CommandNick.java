package com.gmail.lilllung09.plm.command;

import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import com.gmail.lilllung09.plm.manager.NameTagManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CommandNick extends DefaultCommand {
    private NameTagManager nameTagManager;
    private Plugin plugin;

    public CommandNick(Plugin plugin, NameTagManager nameTagManager) {
        this.nameTagManager = nameTagManager;
    }

    @Override
    public void execCommand(CommandSender commandSender, String[] args) {

//            [0]
//        plm nick set "nick" "dst" "observers(" ")|[]"

        if (args.length < 4) {
            //missMatch
            return;
        }

        List<Player> observers = new ArrayList<>();
        if (args.length == 4) {
            observers = (List<Player>) Bukkit.getOnlinePlayers();
        } else {
            for (int i = 4; i < args.length; i++) {
                observers.add(Bukkit.getPlayer(args[i]));
            }
        }

        this.nameTagManager.changeNameTag(Bukkit.getPlayer(args[3]), args[2], observers);
    }

    @Override
    public List<String> getSubCommands(CommandSender sneder, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list.add("set");

        } else if (args.length == 3) {
            plugin.getServer().getOnlinePlayers().forEach(player -> {
                list.add(PlmLibListener.PLAYER_REAL_NAME.get(player.getUniqueId()));
            });

        } else if (args.length >= 3) {
            plugin.getServer().getOnlinePlayers().forEach(player -> {
                list.add(PlmLibListener.PLAYER_REAL_NAME.get(player.getUniqueId()));
            });
        }

        return super.getMatchingSubCommands(list, args[args.length-1]);
    }
}
