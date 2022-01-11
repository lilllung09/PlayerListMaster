package com.gmail.lilllung09.plm.command;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandHelp extends DefaultCommand {
    @Override
    public void execCommand(CommandSender commandSender, String[] args) {

    }

    @Override
    public List<String> getSubCommands(CommandSender sneder, String[] args) {
        List<String> list = new ArrayList<>();
        list.add("skin");
        list.add("nick");
        list.add("help");


        return super.getMatchingSubCommands(list, args[args.length-1]);
    }
}
