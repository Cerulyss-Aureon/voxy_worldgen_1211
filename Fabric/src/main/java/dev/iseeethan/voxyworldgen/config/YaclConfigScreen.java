package dev.iseeethan.voxyworldgen.config;

import dev.iseeethan.voxyworldgen.Constants;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class YaclConfigScreen {
    public static Screen create(Screen parent) {
        VoxyWorldGenConfig config = VoxyWorldGenConfig.getInstance();
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
                            defaults.isEnabledValue(),
                            config::isEnabledValue,
                            config::setEnabled
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                    
                    // Chunks per tick
                    .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.chunks_per_tick"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.chunks_per_tick.description")))
                        .binding(
                            defaults.getChunksPerTickValue(),
                            config::getChunksPerTickValue,
                            config::setChunksPerTick
                        )
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(1, 128)
                            .step(1)
                            .formatValue(val -> Component.literal(val + " chunks")))
                        .build())
                    
                    // Generation style enum
                    .option(Option.<VoxyWorldGenConfig.GenerationStyle>createBuilder()
                        .name(Component.translatable("config.voxyworldgen.generation_style"))
                        .description(OptionDescription.of(Component.translatable("config.voxyworldgen.generation_style.description")))
                        .binding(
                            defaults.getGenerationStyleValue(),
                            config::getGenerationStyleValue,
                            config::setGenerationStyle
                        )
                        .controller(opt -> EnumControllerBuilder.create(opt)
                            .enumClass(VoxyWorldGenConfig.GenerationStyle.class)
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
                            defaults.getPlayerDistanceValue(),
                            config::getPlayerDistanceValue,
                            config::setPlayerDistance
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
                            defaults.getSpawnDistanceValue(),
                            config::getSpawnDistanceValue,
                            config::setSpawnDistance
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
                            defaults.shouldPrioritizeNearPlayerValue(),
                            config::shouldPrioritizeNearPlayerValue,
                            config::setPrioritizeNearPlayer
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                        
                    .build())
                    
                .build())
            
            .build()
            .generateScreen(parent);
    }
}
