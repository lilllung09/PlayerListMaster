package com.gmail.lilllung09.plm.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class NameTagManager {

    private final Plugin plugin;
    private ProtocolManager protocolManager;

    public NameTagManager(Plugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void changeNameTag(Player player, String nickName, List<Player> observers) {
        //PlayerListMaster.PLAYER_NAME.put(player.getUniqueId(), nickName);

        if (!PlmLibListener.PLAYER_REAL_NAME.containsKey(player.getUniqueId())) {
            PlmLibListener.PLAYER_REAL_NAME.put(player.getUniqueId(), player.getName());
        }
        if (!PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.containsKey(player.getUniqueId())) {
            PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.put(player.getUniqueId(), new HashMap<>());
        }

        Map<UUID, Map<String, Object>> currentsObservers = PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.get(player.getUniqueId());
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            observers.forEach(observer -> {
                if (!currentsObservers.containsKey(observer.getUniqueId())) {
                    currentsObservers.put(observer.getUniqueId(), new HashMap<>());
                }
                Map<String, Object> skinName = currentsObservers.get(observer.getUniqueId());
                skinName.put("NAME", nickName);

                PacketContainer removePlayerPacket = removePlayerPacket(player);
                PacketContainer addPlayerPacket = addPlayerPacket(player, (WrappedSignedProperty) skinName.get("SKIN"), nickName);
                try {
                    protocolManager.sendServerPacket(observer, removePlayerPacket);
                    protocolManager.sendServerPacket(observer, addPlayerPacket);
                } catch (InvocationTargetException e) {
                    //e.printStackTrace();
                }
            });
            protocolManager.updateEntity(player, observers);
        }, 0L);
    }

    private PacketContainer removePlayerPacket(Player p) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        PlayerInfoData playerInfoData = new PlayerInfoData(
                WrappedGameProfile.fromPlayer(p)
                , 10
                , EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())
                , WrappedChatComponent.fromText(p.getName()));

        List<PlayerInfoData> plist = new ArrayList<>();
        plist.add(playerInfoData);

        packet.getPlayerInfoDataLists().write(0, plist);

        return packet;
    }
    private PacketContainer addPlayerPacket(Player p, WrappedSignedProperty texture, String nickName) {
        //패킷 타입 지정
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        //대체 프로필 nickName 이름으로 생성
        WrappedGameProfile newProfile = WrappedGameProfile.fromPlayer(p).withName(nickName);

        //대체 스킨 texture 으로 변경
        if (texture != null) {
            Set<WrappedSignedProperty> props = new HashSet<>();
            props.add(texture);
            newProfile.getProperties().replaceValues("textures", props);
        }

        //패킷에 실을 형태로 변환
        PlayerInfoData playerInfoData = new PlayerInfoData(
                newProfile
                , 10
                , EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())
                , WrappedChatComponent.fromText(nickName));

        List<PlayerInfoData> list = new ArrayList<>();
        list.add(playerInfoData);

        //패킷에 실음
        packet.getPlayerInfoDataLists().write(0, list);

        return packet;
    }

}
