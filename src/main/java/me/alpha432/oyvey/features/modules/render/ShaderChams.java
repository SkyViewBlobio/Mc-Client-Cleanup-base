package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.shader.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ShaderChams extends Module {
    private static ShaderChams INSTANCE;

    //public Setting<ShaderModes> shaderMode = this.register(new Setting<>("Shader Mode", ShaderModes.AQUA));
    public Setting<ShaderMode> shader = this.register(new Setting<>("Shader Mode", ShaderMode.AQUA));

    public Setting<Boolean> targetParent = this.register(new Setting<>("Targets", true));
    public Setting<Boolean> players = this.register(new Setting<>("Players", true, s -> this.targetParent.getValue()));
    public Setting<Boolean> crystals = this.register(new Setting<>("Crystals", true, s -> this.targetParent.getValue()));
    public Setting<Boolean> mobs = this.register(new Setting<>("Mobs", false, s -> this.targetParent.getValue()));
    public Setting<Boolean> animals = this.register(new Setting<>("Animals", false, s -> this.targetParent.getValue()));

    private ShaderChams() {
        super("ShaderChams [broken]", "Europa except it does still suck", Category.RENDER, true, false, false);
    }

    public static ShaderChams getInstance() {
        if (INSTANCE == null) INSTANCE = new ShaderChams();
        return INSTANCE;
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if (nullCheck()) return;

        FramebufferShader framebufferShader = this.shader.getValue().getShader();
        if (framebufferShader == null) return;

        /*
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
         */

        List<Entity> entityList = new ArrayList<>();

        for (final Entity entity : mc.world.loadedEntityList) {
            if (entity.equals(mc.player)) continue;
            if (entity.equals(mc.getRenderViewEntity())) continue;
            // remove stuff that is broken
            if (entity instanceof EntityItem
                    || entity instanceof EntityItemFrame
                    || entity instanceof EntityThrowable) continue;

            if (entity instanceof EntityPlayer && !this.players.getValue()) continue;
            if (entity instanceof EntityEnderCrystal && !this.crystals.getValue()) continue;
            if (entity instanceof EntityMob && !this.mobs.getValue()) continue;
            if ( (entity instanceof EntityAnimal
                    || entity instanceof EntitySlime
                    || entity instanceof EntityWaterMob) && !this.animals.getValue()) continue;

            if (true) {
                entityList.add(entity);
            }
        }
        framebufferShader.startDraw(event.getPartialTicks());
        for (Entity entity : entityList) {
            final Vec3d vector = MathUtil.getInterpolatedRenderPos(entity, event.getPartialTicks());
            Objects.requireNonNull(mc.getRenderManager().getEntityRenderObject(entity))
                    .doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.getPartialTicks());
        }
        framebufferShader.stopDraw();

        //GlStateManager.color(1.0f, 1.0f, 1.0f);
        /*
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
         */
    }
}