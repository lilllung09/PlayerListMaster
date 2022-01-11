package com.gmail.lilllung09.plm.command;

import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import com.gmail.lilllung09.plm.manager.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class CommandSkin extends DefaultCommand {
    private SkinManager skinManager;
    private Plugin plugin;

    public CommandSkin(Plugin plugin, SkinManager skinManager) {
        this.plugin = plugin;
        this.skinManager = skinManager;
    }

    @Override
    public void execCommand(CommandSender commandSender, String[] args) {

//            [0]
//        plm skin set  "src0"  "src1" "dst" "observers(" ")|[]"
//                      player
//                      mojang
//                      stored
//
//        plm skin save "src"  "src1"  "[storename]"
//                      player
//                      mojang
//
//        plm skin upload "filename" "[storename]"

        switch(args[1]) {
            case "set": {
                if (args.length < 5) {
                    //명령어 실행 불가 args missMatch

                    return;
                }

                //적용할 플레이어
                Player dst = Bukkit.getPlayer(args[4]);
                boolean me = false;

                //바뀐 스킨으로 보여줄 대상
                List<Player> observers = new ArrayList<>();
                if (args.length == 5) {
                    observers = (List<Player>) Bukkit.getOnlinePlayers();
                    me = true;

                } else if (args.length >= 6) {
                    for (int i = 5; i < args.length; i++) {
                        Player p = Bukkit.getPlayer(args[i]);
                        observers.add(p);
                        if (dst == p) {
                            me = true;
                        }
                    }
                }

                switch (args[2]) {
                    case "player":
                        this.skinManager.changeSkinFromServer(dst, args[3], observers, me);
                        break;

                    case "mojang":
                        this.skinManager.changeSkinFromMojang(dst, args[3], observers, me);
                        break;

                    case "stored":
                        this.skinManager.changeSkinFromStored(dst, args[3], observers, me);
                        break;
                }
            } break;
            case "save": {
                if (args.length < 4) {
                    //명령어 실행 불가 args missMatch

                    return;
                }

                String storeName = args[3];
                if (args.length == 5) {
                    storeName = args[4];
                }

                switch (args[2]) {
                    case "player":
                        this.skinManager.storeSkinFromServer(args[3], storeName);
                        break;

                    case "mojang":
                        this.skinManager.storeSkinFromMojang(args[3], storeName);
                        break;
                }

            } break;
            case "upload": {
                if (args.length < 3) {
                    //명령어 실행 불가 args missMatch

                    return;
                }

                String storeName = args[2];
                if (args.length == 4) {
                    storeName = args[3];
                }

                this.skinManager.uploadSkin(commandSender, args[2], storeName);
            } break;

            default: {
                //명령어 실행 불가 args missMatch
            }
        }

    }

    @Override
    public List<String> getSubCommands(CommandSender sneder, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list.add("set");
            list.add("save");
            list.add("upload");

        } else if (args.length == 3) {
            switch (args[1]) {
                case "set":
                    list.add("stored");
                case "save":
                    list.add("player");
                    list.add("mojang");
                    break;
            }

        } else if (args.length == 4) {
            if (args[2].equals("player")) {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    list.add(PlmLibListener.PLAYER_REAL_NAME.get(player.getUniqueId()));
                });

            } else if (args[2].equals("stored")) {
                this.skinManager.getStoredSkinList().forEach(name -> {
                    list.add(name);
                });
            }

        } else if (args.length >= 5) {
            plugin.getServer().getOnlinePlayers().forEach(player -> {
                list.add(PlmLibListener.PLAYER_REAL_NAME.get(player.getUniqueId()));
            });
        }

        return super.getMatchingSubCommands(list, args[args.length-1]);
    }
}
