package ru.shirk.fights.storages.files;

import ru.shirk.fights.Fights;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;

public class ConfigurationManager {

    private final HashMap<String, Configuration> Configs;

    public ConfigurationManager() {
        this.Configs = new HashMap<>();
    }


    public Configuration getConfig(String name) {
        if (this.Configs.containsKey(name)) {
            return this.Configs.get(name);
        }
        Configuration config = new Configuration(Fights.getInstance(), name);
        this.Configs.put(name, config);
        return config;
    }

    public void reloadConfigs() {
        for (Configuration c : Configs.values()) c.reload();
    }


    public void createFile(String name) {
        InputStream is = null;
        OutputStream os = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            is = classLoader.getResourceAsStream(name.replace('\\', '/'));


            if (is == null) {
                Fights.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [No source file in the plugin jar]");
            } else {
                Fights.getInstance().getDataFolder().mkdirs();
                os = Files.newOutputStream(new File(Fights.getInstance().getDataFolder(), name).toPath());
                byte[] buffer = new byte[99];
                int length = 0;
                length = is.read(buffer);
                while (length > 0) {
                    os.write(buffer, 0, length);
                    length = is.read(buffer);
                }
            }
        } catch (IOException ex) {
            Fights.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [IOException 1]");
            ex.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Fights.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [IOException 2]");
                ex.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                Fights.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [IOException 3]");
                ex.printStackTrace();
            }
        }
    }
}
