package dev.iseeethan.voxyworldgen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.iseeethan.voxyworldgen.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VoxyWorldGenConfig {
    private static final Logger LOGGER = LogManager.getLogger("VoxyWorldGenConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID + ".json");
    
    // Singleton instance
    private static VoxyWorldGenConfig INSTANCE;
    
    // Cache for YACL availability check
    private static Boolean yaclLoaded = null;
    
    /**
     * Generation pattern styles for chunk loading.
     */
    public enum GenerationStyle {
        SPIRAL_OUT("Spiral Out", "Generates chunks in a spiral pattern starting from the center and moving outward"),
        SPIRAL_IN("Spiral In", "Generates chunks in a spiral pattern starting from the edge and moving inward"),
        CONCENTRIC("Concentric Rings", "Generates chunks in concentric square rings around the center"),
        ORIGINAL("Original", "The original line-based generation pattern (fastest, but less uniform)"),
        RANDOM("Random", "Generates chunks in a random order within the generation radius");
        
        private final String displayName;
        private final String description;
        
        GenerationStyle(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Config values with defaults
    private boolean enabled = true;
    private int playerDistance = 25;
    private int spawnDistance = 100;
    private int chunksPerTick = 4;
    private boolean prioritizeNearPlayer = true;
    private GenerationStyle generationStyle = GenerationStyle.SPIRAL_OUT;
    
    // Static accessors for easy access throughout the mod
    public static boolean isEnabled() {
        return getInstance().enabled;
    }
    
    public static int getPlayerDistance() {
        return getInstance().playerDistance;
    }
    
    public static int getSpawnDistance() {
        return getInstance().spawnDistance;
    }
    
    public static int getChunksPerTick() {
        return getInstance().chunksPerTick;
    }
    
    public static boolean shouldPrioritizeNearPlayer() {
        return getInstance().prioritizeNearPlayer;
    }
    
    public static GenerationStyle getGenerationStyle() {
        return getInstance().generationStyle;
    }
    
    // Instance accessors for YACL bindings
    public boolean isEnabledValue() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getPlayerDistanceValue() {
        return playerDistance;
    }
    
    public void setPlayerDistance(int playerDistance) {
        this.playerDistance = playerDistance;
    }
    
    public int getSpawnDistanceValue() {
        return spawnDistance;
    }
    
    public void setSpawnDistance(int spawnDistance) {
        this.spawnDistance = spawnDistance;
    }
    
    public int getChunksPerTickValue() {
        return chunksPerTick;
    }
    
    public void setChunksPerTick(int chunksPerTick) {
        this.chunksPerTick = chunksPerTick;
    }
    
    public boolean shouldPrioritizeNearPlayerValue() {
        return prioritizeNearPlayer;
    }
    
    public void setPrioritizeNearPlayer(boolean prioritizeNearPlayer) {
        this.prioritizeNearPlayer = prioritizeNearPlayer;
    }
    
    public GenerationStyle getGenerationStyleValue() {
        return generationStyle;
    }
    
    public void setGenerationStyle(GenerationStyle generationStyle) {
        this.generationStyle = generationStyle;
    }
    
    /**
     * Get the singleton config instance, loading from file if needed.
     */
    public static VoxyWorldGenConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }
    
    /**
     * Initialize the config - call this during mod initialization.
     */
    public static void create() {
        INSTANCE = load();
        LOGGER.info("VoxyWorldGen config loaded successfully!");
    }
    
    /**
     * Check if YACL is available at runtime.
     */
    public static boolean isYaclLoaded() {
        if (yaclLoaded == null) {
            yaclLoaded = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
        }
        return yaclLoaded;
    }
    
    /**
     * Load config from file, or create default if it doesn't exist.
     */
    private static VoxyWorldGenConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                VoxyWorldGenConfig config = GSON.fromJson(json, VoxyWorldGenConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load config file, using defaults", e);
            }
        }
        
        // Create new config with defaults and save it
        VoxyWorldGenConfig config = new VoxyWorldGenConfig();
        config.save();
        return config;
    }
    
    /**
     * Save the current config to file.
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
            LOGGER.debug("Config saved successfully");
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }
    
    public static Screen createConfigScreen(Screen parent) {
        if (!isYaclLoaded()) {
            LOGGER.warn("YACL is not installed - config screen is not available. Install YACL for a GUI config screen.");
            return null;
        }
        
        // Defer to the YACL-specific class to avoid loading YACL classes when it's not present
        return YaclConfigScreen.create(parent);
    }
}
