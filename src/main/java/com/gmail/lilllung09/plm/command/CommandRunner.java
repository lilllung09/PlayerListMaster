package com.gmail.lilllung09.plm.command;

import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRunner implements CommandExecutor {
    private static final Map<String, DefaultCommand> PLM_SUB_COMMANDS = new HashMap<>();

    public CommandRunner(Plugin plugin, PlmLibListener plmLibListener) {
        //only ops
        PLM_SUB_COMMANDS.put("reload", new CommandReload());

        PLM_SUB_COMMANDS.put("help", new CommandHelp());
        PLM_SUB_COMMANDS.put("skin", new CommandSkin(plugin, plmLibListener.getSkinManager()));
        PLM_SUB_COMMANDS.put("nick", new CommandNick(plugin, plmLibListener.getNameTagManager()));
        PLM_SUB_COMMANDS.put("clear", new CommandClear(plmLibListener));


        //PLM_SUB_COMMANDS.put("agu", new CommandAgueppo(plmLibListener));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( 0 == args.length || !PLM_SUB_COMMANDS.containsKey(args[0])) {
            PLM_SUB_COMMANDS.get("help").execCommand(sender, args);
            return false;
        }

        DefaultCommand defaultCommand = PLM_SUB_COMMANDS.get(args[0]);
        defaultCommand.execCommand(sender, args);

        return true;
    }

    public List<String> getSubCommands(CommandSender commandSender, String[] args) {
        return this.PLM_SUB_COMMANDS.get(args[0]).getSubCommands(commandSender, args);
    }
}
