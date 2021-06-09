package xyz.tangerie.educlan.files;

import org.bukkit.configuration.file.YamlConfiguration;
import xyz.tangerie.educlan.EduClan;

import java.io.File;

import static org.bukkit.Bukkit.getLogger;

public class ConfigFile extends YamlConfiguration {
    private EduClan plugin;
    private File file;
    private String filename;

    public ConfigFile(String path) {
        plugin = (EduClan) EduClan.getPlugin((Class) EduClan.class);
        file = new File(plugin.getDataFolder(), path);
        filename = path;
        saveDefault();
        reload();

        getLogger().info(path + " Loaded");
    }

    public String getFilename() { return filename; }

    public void reload() {
        try {
            super.load(file);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().info(e.getMessage());
        }
    }

    public void save() {
        try {
            super.save(file);
        } catch(Exception e) {
            e.printStackTrace();
            getLogger().info(e.getMessage());
        }
    }

    public void saveDefault() {
        if(!file.exists()) {
            plugin.saveResource(filename, false);
        }
    }
}
