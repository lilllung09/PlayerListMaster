package com.gmail.lilllung09.plm;

import com.gmail.lilllung09.plm.command.CommandRunner;
import com.gmail.lilllung09.plm.command.CommandTapCompleter;
import com.gmail.lilllung09.plm.listeners.PlmLibListener;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;

public class PlayerListMaster extends JavaPlugin {

    private boolean useProtocolLib;

    @Override
    public void onEnable() {
        useProtocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib");
        if (!useProtocolLib) {
            log(Level.WARNING, "Please install ProtocolLib to be able to use PLM features: " +
                    "https://www.spigotmc.org/resources/protocollib.1997/");
            return;
        }


        File data = getDataFolder();
        if (!data.exists()) {
            try {
                data.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getServer().getWorld("worldPLM") == null) {
            WorldCreator.name("worldPLM")
                    .type(WorldType.FLAT)
                    .generator(new EmptyChunkGenerator())
                    .createWorld();
        }


        PluginManager pluginManager = getServer().getPluginManager();
        //pluginManager.registerEvents(new PressureListener(this), this);

        PlmLibListener plmLibListener = new PlmLibListener(this);
        pluginManager.registerEvents(plmLibListener, this);


        getServer().getPluginCommand("plm").setExecutor(new CommandRunner(this, plmLibListener));
        getCommand("plm").setTabCompleter(new CommandTapCompleter(this, plmLibListener));



    }
    @Override
    public void onDisable() {

    }

    private void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    private class EmptyChunkGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world);
        }

    }
}
