package com.gmail.lilllung09.plm.command;

import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CommandTapCompleter extends DefaultCommand implements TabCompleter {

    private Plugin plugin;
    private PlmLibListener plmLibListener;

    private CommandRunner commandRunner;

    public CommandTapCompleter(Plugin plugin, PlmLibListener plmLibListener) {
        this.plugin = plugin;
        this.plmLibListener = plmLibListener;

        this.commandRunner = (CommandRunner) plugin.getServer().getPluginCommand("plm").getExecutor();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            //list.add("help");
            //list.add("reload");
            list.add("skin");
            list.add("nick");
            //list.add("clear");

            list = super.getMatchingSubCommands(list, args[0]);

        } else if (args.length <= 2) {
            list = this.commandRunner.getSubCommands(sender, args);
        }

        return list;
    }
}
