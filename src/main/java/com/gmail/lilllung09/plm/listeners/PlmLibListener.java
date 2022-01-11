package com.gmail.lilllung09.plm.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.*;
import com.gmail.lilllung09.plm.PlayerListMaster;
import com.gmail.lilllung09.plm.manager.NameTagManager;
import com.gmail.lilllung09.plm.manager.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;


public class PlmLibListener implements Listener {

    private ProtocolManager protocolManager;
    private NameTagManager nameTagManager;
    private SkinManager skinManager;

    //이 사람이, 이 사람에게, 이름|스킨이, 이렇게보인다
    public static Map<UUID, Map<UUID, Map<String, Object>>> PLAYER_HOW_TO_SEE_PLAYER = new HashMap<>();
    public static Map<UUID, String> PLAYER_REAL_NAME = new HashMap<>();

    @EventHandler
    public void setPlayerRealName(PlayerJoinEvent event) {
        this.addPlayer(event.getPlayer());
    }

    public PlmLibListener(Plugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.nameTagManager = new NameTagManager(plugin);
        this.skinManager = new SkinManager(plugin);

        Bukkit.getOnlinePlayers().forEach(p -> {
            addPlayer(p);
        });
    }

    private void addPlayer(Player p) {
        if (PLAYER_REAL_NAME.containsKey(p.getUniqueId())) {
            this.skinManager.changeSkinRefuse(p);
        } else {
            PLAYER_REAL_NAME.put(p.getUniqueId(), p.getName());
        }
    }

    public SkinManager getSkinManager() {
        return this.skinManager;
    }
    public NameTagManager getNameTagManager() {
        return this.nameTagManager;
    }
    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }
}
