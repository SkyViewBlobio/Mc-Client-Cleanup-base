package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.Util;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;

public class GUIBlur extends Module implements Util
{
    public GUIBlur() {
        super("XuluBlur", "Blurs Your Gui like in xulu", Category.CLIENT, true, false, false);
    }

    @Override
    public void onDisable() {
        if (GUIBlur.mc.world != null) {
            GUIBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    @Override
    public void onUpdate() {
        if (GUIBlur.mc.world != null) {
            if (ClickGui.getInstance().isEnabled() || GUIBlur.mc.currentScreen instanceof GuiContainer || GUIBlur.mc.currentScreen instanceof GuiChat || GUIBlur.mc.currentScreen instanceof GuiConfirmOpenLink || GUIBlur.mc.currentScreen instanceof GuiEditSign || GUIBlur.mc.currentScreen instanceof GuiGameOver || GUIBlur.mc.currentScreen instanceof GuiOptions || GUIBlur.mc.currentScreen instanceof GuiIngameMenu || GUIBlur.mc.currentScreen instanceof GuiVideoSettings || GUIBlur.mc.currentScreen instanceof GuiScreenOptionsSounds || GUIBlur.mc.currentScreen instanceof GuiControls || GUIBlur.mc.currentScreen instanceof GuiCustomizeSkin || GUIBlur.mc.currentScreen instanceof GuiModList) {
                if (OpenGlHelper.shadersSupported && GUIBlur.mc.getRenderViewEntity() instanceof EntityPlayer) {
                    if (GUIBlur.mc.entityRenderer.getShaderGroup() != null) {
                        GUIBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                    }
                    try {
                        GUIBlur.mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (GUIBlur.mc.entityRenderer.getShaderGroup() != null && GUIBlur.mc.currentScreen == null) {
                    GUIBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                }
            }
            else if (GUIBlur.mc.entityRenderer.getShaderGroup() != null) {
                GUIBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            }
        }
    }
}