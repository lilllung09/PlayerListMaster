package com.gmail.lilllung09.plm.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;


public class SkinManager {

    private final Plugin plugin;

    private JsonObject storedTextures = new JsonObject();

    private ProtocolManager protocolManager;

    private Map<String, World> worldMap = new HashMap<>();

    public SkinManager(Plugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        plugin.getServer().createWorld(new WorldCreator("worldPLM"));

        this.load();
    }

    //ChangeSkin
    public void changeSkinFromServer(Player current, String nickName, List<Player> observers, boolean me) {
        Player sourcePlayer = this.plugin.getServer().getPlayer(nickName);

        if (sourcePlayer != null) {
            WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(sourcePlayer);
            Collection<WrappedSignedProperty> props = gameProfile.getProperties().get("textures");

            this.change(current, props.iterator().next(), observers, me);

        } else {

            this.changeSkinFromMojang(current, nickName, observers, me);
        }

    }
    public void changeSkinFromMojang(Player current, String nickName, List<Player> observers, boolean me) {
        String uuid = getUUIDFromMojang(nickName);

        if (uuid == null) {
            this.log(Level.INFO, nickName + " uuid was not found OR something wrong");
            return;
        }

        this.change(current, getSkinFromMojang(uuid), observers, me);
    }
    public void changeSkinFromStored(Player current, String textureName, List<Player> observers, boolean me) {
        if (!this.storedTextures.has(textureName)) {
            this.log(Level.INFO, "stored skin " + textureName + " was not found OR something wrong");
            return;
        }

        this.change(current, getSkinFromStored(textureName), observers, me);
    }
    public void changeSkinRefuse(Player current) {
        if (!PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.containsKey(current.getUniqueId())) {
            return;
        }

        Map<UUID, Map<String, Object>> currentsObservers = PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.get(current.getUniqueId());
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            List<Player> observers = new ArrayList<>();

            currentsObservers.forEach((observer, skinName) -> {
                Player ob = plugin.getServer().getPlayer(observer);
                observers.add(ob);
                this.changeSub(current, (WrappedSignedProperty) skinName.get("SKIN"), (String) skinName.get("NAME"), ob);
            });
            protocolManager.updateEntity(current, observers);
        }, 0L);

    }
    private void change(Player current, WrappedSignedProperty texture, List<Player> observers, boolean me) {
        if (!PlmLibListener.PLAYER_REAL_NAME.containsKey(current.getUniqueId())) {
            PlmLibListener.PLAYER_REAL_NAME.put(current.getUniqueId(), current.getName());
        }
        if (!PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.containsKey(current.getUniqueId())) {
            PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.put(current.getUniqueId(), new HashMap<>());
        }

        Map<UUID, Map<String, Object>> currentsObservers = PlmLibListener.PLAYER_HOW_TO_SEE_PLAYER.get(current.getUniqueId());
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            observers.forEach(observer -> {
                String nickName;
                if (!currentsObservers.containsKey(observer.getUniqueId())) {
                    currentsObservers.put(observer.getUniqueId(), new HashMap<>());
                }
                Map<String, Object> skinName = currentsObservers.get(observer.getUniqueId());
                skinName.put("SKIN", texture);

                if ((nickName = (String) skinName.get("NAME")) == null) {
                    nickName = PlmLibListener.PLAYER_REAL_NAME.get(current.getUniqueId());
                }

                if (observer != null) {
                    this.changeSub(current, texture, nickName, observer);
                }
            });
            protocolManager.updateEntity(current, observers);
        }, 0L);

        if (me) {
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                Location loc = current.getLocation();

                String worldName = loc.getWorld().getName();

                loc.setWorld(plugin.getServer().getWorld("worldPLM"));
                current.teleport(loc);
                loc.setWorld(plugin.getServer().getWorld(worldName));
                current.teleport(loc);

            }, 1L);
        }
    }
    private void changeSub(Player current, WrappedSignedProperty texture, String nickName, Player observer) {
        PacketContainer removePlayerPacket = removePlayerPacket(current);
        PacketContainer addPlayerPacket = addPlayerPacket(current, texture, nickName);
        try {
            protocolManager.sendServerPacket(observer, removePlayerPacket);
            protocolManager.sendServerPacket(observer, addPlayerPacket);
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
        }
    }


    //StoreSkin
    public void storeSkinFromServer(String target, String textureName) {
        Player sourcePlayer = this.plugin.getServer().getPlayer(target);
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(sourcePlayer);
        Collection<WrappedSignedProperty> props = gameProfile.getProperties().get("textures");

        WrappedSignedProperty prop = props.iterator().next();

        this.store(textureName, prop);
    }
    public void storeSkinFromMojang(String target, String textureName) {
        String uuid = getUUIDFromMojang(target);

        if (uuid == null) {
            this.log(Level.WARNING, target + " was not found OR something wrong");
            return;
        }

        WrappedSignedProperty prop = getSkinFromMojang(uuid);

        this.store(textureName, prop);
    }
    private void store(String textureName, WrappedSignedProperty texture) {
        JsonObject root = new JsonObject();
        root.addProperty("value", texture.getValue());
        root.addProperty("signature", texture.getSignature());

        this.storedTextures.add(textureName, root);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter fw = new FileWriter(this.plugin.getDataFolder().getPath() + "/textures.json", StandardCharsets.UTF_8)) {
            gson.toJson(this.storedTextures, fw);
            fw.flush();

        } catch (Exception e) {
            this.log(Level.WARNING, "textures.json file occurred some errors");
            e.printStackTrace();
        }
    }


    //ReloadStoredSkins
    public void reloadStoredSkins() {
        this.load();
        this.log(Level.INFO, "Stored textures was reloaded");
    }
    private void load() {
        Gson gson = new Gson();

        try (Reader reader = new FileReader(this.plugin.getDataFolder().getPath() + "/textures.json", StandardCharsets.UTF_8)) {
            this.storedTextures = gson.fromJson(reader, JsonObject.class);

        } catch (Exception e) {
            this.log(Level.WARNING, "textures.json file occurred some errors");
            e.printStackTrace();
        }
    }

    //Others
    private WrappedSignedProperty getSkinFromMojang(String uuid) {
        Gson gson = new Gson();

        String data = getResponse("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        JsonObject jsonData = gson.fromJson(data, JsonObject.class);

        JsonObject texturesProperties = jsonData.getAsJsonArray("properties").get(0).getAsJsonObject();

        String value = texturesProperties.get("value").getAsString();
        String signature = texturesProperties.get("signature").getAsString();

        return new WrappedSignedProperty("textures", value, signature);
    }
    private WrappedSignedProperty getSkinFromStored(String textureName) {
        JsonObject data = this.storedTextures.get(textureName).getAsJsonObject();

        String value = data.get("value").getAsString();
        String signature = data.get("signature").getAsString();

        return new WrappedSignedProperty("textures", value, signature);
    }

    public List<String> getStoredSkinList() {
        List<String> skinList = new ArrayList<>();
        this.storedTextures.entrySet().forEach(e -> {
            skinList.add(e.getKey());
        });
        return skinList;
    }

    private String getUUIDFromMojang(String nickName) {
        Gson gson = new Gson();

        String data = getResponse("https://api.mojang.com/users/profiles/minecraft/" + nickName);
        JsonObject jsonData = gson.fromJson(data, JsonObject.class);

        if (jsonData == null) return null;
        if (!jsonData.has("id")) return null;

        return jsonData.get("id").getAsString();
    }
    private String getResponse(String url) {
        String data = null;

        try {
            URL url_ = new URL(url);

            URLConnection con = url_.openConnection();
            InputStream in = con.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            if (br != null) br.close();
            if (in != null) in.close();

            data = sb.toString();

        } catch (Exception e) {
            this.log(Level.WARNING, "Can't get response from Mojang OR something wrong");
            e.printStackTrace();

        }

        return data;
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
        String nick = nickName != null ? nickName : PlmLibListener.PLAYER_REAL_NAME.get(p.getUniqueId());
        WrappedGameProfile newProfile = WrappedGameProfile.fromPlayer(p).withName(nick);

        //대체 스킨 texture 으로 변경
        Set<WrappedSignedProperty> props = new HashSet<>();
        props.add(texture);
        newProfile.getProperties().replaceValues("textures", props);

        //패킷에 실을 형태로 변환
        PlayerInfoData playerInfoData = new PlayerInfoData(
                newProfile
                , 10
                , EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())
                , WrappedChatComponent.fromText(nick));

        List<PlayerInfoData> list = new ArrayList<>();
        list.add(playerInfoData);

        //패킷에 실음
        packet.getPlayerInfoDataLists().write(0, list);

        return packet;
    }

    private PacketContainer destroyEntityPacket(Player p) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        int id = p.getEntityId();
        packet.getIntegerArrays().write(0, new int[]{id});


        return packet;
    }
    private PacketContainer namedEntitySpawnPacket(Player p) {
        Location loc = p.getLocation();

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

        packet.getIntegers().write(0, p.getEntityId());

        packet.getUUIDs().write(0, p.getUniqueId());

        packet.getDoubles().write(0, loc.getX());
        packet.getDoubles().write(1, loc.getY());
        packet.getDoubles().write(2, loc.getZ());

        //pitch
        packet.getBytes().write(1, (byte)(loc.getPitch()));

        return packet;
    }
    private PacketContainer entityHeadRotationPacket(Player p) {
        Location loc = p.getLocation();

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

        packet.getIntegers().writeSafely(0, p.getEntityId());

        //yaw
        Float yaw = loc.getYaw() * 360.0f / 256.0f;
        packet.getBytes().writeSafely(0, yaw.byteValue());

        return packet;
    }


    //The Other - Upload Skin (mineskin api)
    public void uploadSkin(CommandSender p, String skinFileName, String textureName) {
        this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
            p.sendMessage("Upload start...");

            JsonObject data = upload(skinFileName);

            if (data == null) {
                p.sendMessage("Upload " + skinFileName + " was failed. See console if you can");
                return;
            }

            JsonObject texture = data.getAsJsonObject("data")
                    .getAsJsonObject("texture");
            String value = texture.get("value").getAsString();
            String signature = texture.get("signature").getAsString();

            WrappedSignedProperty prop = new WrappedSignedProperty("textures", value, signature);
            store(textureName, prop);

            p.sendMessage("Upload " + skinFileName + " file was succeed. Try /plm skin set server " + textureName);
            //log(Level.INFO, "Uploading succeed by " + Plm.PLAYER_REAL_NAME.get(p.getUniqueId()) + ". File was " + skinFileName);
        }, 0L);
    }
    private JsonObject upload(String skinFileName) {
        String boundary_string = "----WebKitFormBoundaryQGvWeNAiOE4g2VM5";
        File file = new File(this.plugin.getDataFolder().getPath() + "/textures/" + skinFileName + ".png");

        if (!file.exists()) {
            this.log(Level.WARNING, skinFileName + ".png was not found. file must be in \"PLM/textures/\"");
            return null;
        }

        try {
            URL url = new URL("https://api.mineskin.org/generate/upload?visibility=1");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // we want to write out
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary_string);

            // now we write out the multipart to the body
            OutputStream conn_out = conn.getOutputStream();
            BufferedWriter conn_out_writer = new BufferedWriter(new OutputStreamWriter(conn_out));

            // write out multitext body based on w3 standard
            // https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html
            conn_out_writer.write("\r\n--" + boundary_string + "\r\n");
            conn_out_writer.write("Content-Disposition: form-data; " +
                    "name=\"file\"; " +
                    "filename=\""+ file.getName() + "\"\r\n" +
                    "Content-Type: image/png" + "\"\r\n\r\n");
            conn_out_writer.flush();
            // payload from the file
            FileInputStream file_stream = new FileInputStream(file);
            // write direct to outputstream instance, because we write now bytes and not strings
            int read_bytes;
            byte[] buffer = new byte[1024];
            while((read_bytes = file_stream.read(buffer)) != -1) {
                conn_out.write(buffer, 0, read_bytes);
            }
            conn_out.flush();

            // close multipart body
            conn_out_writer.write("\r\n--" + boundary_string + "--\r\n");
            conn_out_writer.flush();

            // close all the streams
            conn_out_writer.close();
            conn_out.close();
            file_stream.close();

            // execute and get response code
            conn.getResponseCode();

            InputStream is;
            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                is = conn.getErrorStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null) {
                sb.append(line);
            }

            Gson gson = new Gson();
            JsonObject data = gson.fromJson(sb.toString(), JsonObject.class);
            if (data == null || data.has("error")) return null;

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    //Logger
    public void log(Level level, String msg) {
        this.plugin.getLogger().log(level, msg);
    }
}
