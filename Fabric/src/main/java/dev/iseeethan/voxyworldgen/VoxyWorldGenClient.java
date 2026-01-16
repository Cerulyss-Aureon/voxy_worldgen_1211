package dev.iseeethan.voxyworldgen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Client-side entry point for Voxy World Gen.
 * Handles F3 debug overlay rendering.
 */
@Environment(EnvType.CLIENT)
public class VoxyWorldGenClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // Register HUD render callback for F3 debug overlay
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            renderF3Debug(guiGraphics);
        });
    }
    
    /**
     * Render debug info when F3 is active.
     */
    private void renderF3Debug(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        
        // Only render when debug screen is visible
        if (!mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        
        // Get debug stats
        DebugStats stats = DebugStats.getInstance();
        
        // Prepare the debug lines
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int lineHeight = font.lineHeight + 2;
        
        // Calculate how many lines we need
        int totalLines = 2; // Header + Status
        if (stats.isGenerationEnabled()) {
            totalLines += 4; // Style + Completed + Progress + Remaining
        }
        
        // Start from the bottom
        int y = screenHeight - (totalLines * lineHeight) - 2;
        
        // Header
        String header = "[Voxy World Gen]";
        int headerWidth = font.width(header);
        int headerX = screenWidth - headerWidth - 2;
        
        // Draw header with background
        guiGraphics.fill(headerX - 2, y - 1, screenWidth, y + font.lineHeight, 0x90505050);
        guiGraphics.drawString(font, header, headerX, y, 0xFFFFAA00, false);
        y += lineHeight;
        
        // Status line
        String status = stats.isGenerationEnabled() ? "§aEnabled" : "§cDisabled";
        String statusLine = "Status: " + status;
        int statusWidth = font.width(statusLine);
        int statusX = screenWidth - statusWidth - 2;
        guiGraphics.fill(statusX - 2, y - 1, screenWidth, y + font.lineHeight, 0x90505050);
        guiGraphics.drawString(font, statusLine, statusX, y, 0xFFFFFFFF, false);
        y += lineHeight;
        
        if (stats.isGenerationEnabled()) {
            // Style line
            String styleLine = "Style: §e" + formatStyle(stats.getCurrentStyle());
            int styleWidth = font.width(styleLine);
            int styleX = screenWidth - styleWidth - 2;
            guiGraphics.fill(styleX - 2, y - 1, screenWidth, y + font.lineHeight, 0x90505050);
            guiGraphics.drawString(font, styleLine, styleX, y, 0xFFFFFFFF, false);
            y += lineHeight;
            
            // Total completed
            String completedLine = "Total Completed: §b" + formatNumber(stats.getTotalChunksCompleted());
            int completedWidth = font.width(completedLine);
            int completedX = screenWidth - completedWidth - 2;
            guiGraphics.fill(completedX - 2, y - 1, screenWidth, y + font.lineHeight, 0x90505050);
            guiGraphics.drawString(font, completedLine, completedX, y, 0xFFFFFFFF, false);
            y += lineHeight;
            
            // Current area progress
            int remaining = stats.getChunksRemainingInCurrentArea();
            int total = stats.getTotalChunksInCurrentArea();
            int completed = total - remaining;
            String progressLine = "Area Progress: §a" + formatNumber(completed) + "§7/§b" + formatNumber(total);
            int progressWidth = font.width(progressLine);
            int progressX = screenWidth - progressWidth - 2;
            guiGraphics.fill(progressX - 2, y - 1, screenWidth, y + font.lineHeight, 0x90505050);
            guiGraphics.drawString(font, progressLine, progressX, y, 0xFFFFFFFF, false);
            y += lineHeight;
            
            // Remaining
            String remainingLine = "Remaining: §6" + formatNumber(remaining);
            int remainingWidth = font.width(remainingLine);
            int remainingX = screenWidth - remainingWidth - 2;
            guiGraphics.fill(remainingX - 2, y - 1, screenWidth, y + font.lineHeight, 0x90505050);
            guiGraphics.drawString(font, remainingLine, remainingX, y, 0xFFFFFFFF, false);
        }
    }
    
    /**
     * Format generation style for display, this is overly 
     */
    private String formatStyle(String style) {
        if (style == null || style.isEmpty()) {
            return "Unknown";
        }
        // Convert SPIRAL_OUT to "Spiral Out"
        return style.replace("_", " ")
                   .toLowerCase()
                   .substring(0, 1).toUpperCase() 
                   + style.replace("_", " ").toLowerCase().substring(1);
    }
    
    /**
     * Format large numbers with comma separators.
     */
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }
}
