package dev.iseeethan.voxyworldgen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.iseeethan.voxyworldgen.Constants;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration class for Voxy World Gen mod.
 * Uses YACL for a beautiful, real-time updating config GUI.
 */
public class VoxyWorldGenConfig {
    private static final Logger LOGGER = LogManager.getLogger("VoxyWorldGenConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID + ".json");
    
    // Singleton instance
    private static VoxyWorldGenConfig INSTANCE;
    
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
    
    /**
     * Create the YACL config screen with all options on a single page.
     * This creates a beautiful, modern config GUI with real-time updates.
     */
    public static Screen createConfigScreen(Screen parent) {
        VoxyWorldGenConfig config = getInstance();
        VoxyWorldGenConfig defaults = new VoxyWorldGenConfig();
        
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("config.voxyworldgen.title"))
            .save(config::save)
            
            // Single category with all options
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("config.voxyworldgen.category.settings"))
                .tooltip(Component.translatable("config.voxyworldgen.category.settings.tooltip"))
                
                // === GENERAL OPTIONS GROUP ===
                .group(OptionGroup.createBuilder()
                    .name(Component.translatable("config.voxyworldgen.group.general"))
                    .description(OptionDescription.of(Component.translatable("config.voxyworldgen.group.general.description")))
                    
                    // Enabled toggle
                    .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.enabled"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.enabled.description")))
                        .binding(
                            defaults.enabled,
                            () -> config.enabled,
                            newVal -> config.enabled = newVal
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                    
                    // Chunks per tick
                    .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.chunks_per_tick"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.chunks_per_tick.description")))
                        .binding(
                            defaults.chunksPerTick,
                            () -> config.chunksPerTick,
                            newVal -> config.chunksPerTick = newVal
                        )
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(1, 128)
                            .step(1)
                            .formatValue(val -> Component.literal(val + " chunks")))
                        .build())
                    
                    // Generation style enum
                    .option(Option.<GenerationStyle>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.generation_style"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.generation_style.description")))
                        .binding(
                            defaults.generationStyle,
                            () -> config.generationStyle,
                            newVal -> config.generationStyle = newVal
                        )
                        .controller(opt -> EnumControllerBuilder.create(opt)
                            .enumClass(GenerationStyle.class)
                            .formatValue(style -> Component.literal(style.getDisplayName())))
                        .build())
                        
                    .build())
                
                // === DISTANCE OPTIONS GROUP ===
                .group(OptionGroup.createBuilder()
                    .name(Component.translatable("config.voxyworldgen.group.distances"))
                    .description(OptionDescription.of(Component.translatable("config.voxyworldgen.group.distances.description")))
                    
                    // Player distance slider
                    .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.player_distance"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.player_distance.description")))
                        .binding(
                            defaults.playerDistance,
                            () -> config.playerDistance,
                            newVal -> config.playerDistance = newVal
                        )
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(1, 512)
                            .step(1)
                            .formatValue(val -> Component.literal(val + " chunks")))
                        .build())
                    
                    // Spawn distance slider
                    .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.spawn_distance"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.spawn_distance.description")))
                        .binding(
                            defaults.spawnDistance,
                            () -> config.spawnDistance,
                            newVal -> config.spawnDistance = newVal
                        )
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(0, 2048)
                            .step(1)
                            .formatValue(val -> Component.literal(val + " chunks")))
                        .build())
                    
                    // Prioritize near player toggle
                    .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.prioritize_player"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.prioritize_player.description")))
                        .binding(
                            defaults.prioritizeNearPlayer,
                            () -> config.prioritizeNearPlayer,
                            newVal -> config.prioritizeNearPlayer = newVal
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                        
                    .build())
                    
                .build())
            
            .build()
            .generateScreen(parent);
    }
}
