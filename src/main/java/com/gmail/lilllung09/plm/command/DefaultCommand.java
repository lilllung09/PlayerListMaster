package com.gmail.lilllung09.plm.command;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class DefaultCommand {
    void execCommand(CommandSender commandSender, String[] args){};

    public List<String> getSubCommands(CommandSender sender, String[] args) {
        return this.getMatchingSubCommands(new ArrayList<>(), args[args.length-1]);
    }

    public List<String> getMatchingSubCommands(List<String> list, String command) {
        List<String> list_m = new ArrayList<>();
        list.forEach(c -> {
            if (c.startsWith(command) || command.length() == 0)
                list_m.add(c);
        });

        return list_m;
    }
}
