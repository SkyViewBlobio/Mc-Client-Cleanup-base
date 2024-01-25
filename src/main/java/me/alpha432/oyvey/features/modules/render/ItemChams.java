package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.ColorSetting;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.mixin.mixins.IEntityRenderer;
import me.alpha432.oyvey.util.*;
import me.alpha432.oyvey.util.shader.*;
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
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemChams extends Module {
    private static ItemChams INSTANCE;

    public Setting<Boolean> chams = new Setting<>("Chams", true);

    public Setting<ShaderMode> shader = this.register(new Setting<>("Shader Mode", ShaderMode.FLOWBLUR));
    public Setting<Boolean> shaderBool = this.register(new Setting<>("Hand Shader", true));
    public Setting<Boolean> shaderItemBool = this.register(new Setting<>("Item Shader", false));
    //public Setting<ShaderMode> shaderItem = this.register(new Setting<>("Item Shader Mode", ShaderMode.AQUA));

    public Setting<Boolean> animation = this.register(new Setting<>("Animation", true));
    public Setting<Integer> animationSpeed = this.register(new Setting<>("Animation Speed", 1, 1, 10));

    // TODO: proper color setting in clickGUI @ddev?
    public ColorSetting color = new ColorSetting("Color", new OyColor(230, 101, 255, 1));
    public Setting<Float> radius = this.register(new Setting<>("Glow Radius", 3.3f, 1f, 10f));
    public Setting<Float> divider = this.register(new Setting<>("Glow Divider", 158.6f, 1f, 1000f));
    public Setting<Float> maxSample = this.register(new Setting<>("Glow MaxSample", 10f, 1f, 20f));

    public Setting<Boolean> targetParent = this.register(new Setting<>("Targets", true));
    public Setting<Boolean> players = this.register(new Setting<>("Players", true, s -> this.targetParent.getValue()));
    public Setting<Boolean> crystals = this.register(new Setting<>("Crystals", true, s -> this.targetParent.getValue()));
    public Setting<Boolean> mobs = this.register(new Setting<>("Mobs", false, s -> this.targetParent.getValue()));
    public Setting<Boolean> animals = this.register(new Setting<>("Animals", false, s -> this.targetParent.getValue()));

    //public Setting<Boolean> useImage = this.register(new Setting<>("Use Image", false));

    private Boolean criticalSection = false;

    private ItemChams() {
        super("ItemChams", "Whateverthefuck", Category.RENDER, true, false, false);
    };

    public static ItemChams getInstance() {
        if (INSTANCE == null) INSTANCE = new ItemChams();
        return INSTANCE;
    }

    @Override
    public void onRenderHand(RenderHandEvent event) {
        if (!this.criticalSection && this.shaderBool.getValue())
            event.setCanceled(true);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (Display.isActive() || Display.isVisible()) {
            if (this.chams.getValue()) {
                /*
                GlStateManager.pushMatrix();
                //GlStateManager.pushAttrib();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableAlpha();
                */

                /*
                ItemShader shader = ItemShader.getInstance();
                shader.blur = this.blur.getValue();
                shader.mix = this.mix.getValue();
                shader.alpha = this.chamColor.getValue().getAlpha() / 255.0f;
                shader.imageMix = this.imageMix.getValue();
                shader.useImage = this.useImage.getValue();
                */

                FramebufferShader shader = this.shader.getValue().getShader();
                if (shader == null) return;

                shader.setShaderParams(
                        this.animation.getValue(), this.animationSpeed.getValue(),
                        this.color.getValue(), this.radius.getValue(),
                        this.divider.getValue(), this.maxSample.getValue());

                List<Entity> entityList = this.getEntityList();

                this.criticalSection = true;

                shader.startDraw(mc.getRenderPartialTicks());

                if (this.shaderItemBool.getValue()) {
                    this.renderItems(entityList);
                }

                // Shader breaks game camera when player is in water (no idea why the fuck that is)
                if (!EntityUtil.posEqualsBlock(PlayerUtil.getPlayerPos().up(), Blocks.WATER) && this.shaderBool.getValue()) {
                    ((IEntityRenderer) mc.entityRenderer).invokeRenderHand(mc.getRenderPartialTicks(), 2);
                }
                shader.stopDraw();

                this.criticalSection = false;

                /*
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.disableDepth();
                //GlStateManager.popAttrib();
                GlStateManager.popMatrix();
                */
            }
        }
    }

    private List<Entity> getEntityList() {
        List<Entity> entityList = new ArrayList<>();

        for (final Entity entity : mc.world.loadedEntityList) {
            boolean add = false;

            if (entity.equals(mc.player)) continue;
            if (entity.equals(mc.getRenderViewEntity())) continue;
            // remove stuff that is broken
            if (entity instanceof EntityItem
                    || entity instanceof EntityItemFrame
                    || entity instanceof EntityThrowable) continue;

            add = entity instanceof EntityPlayer && this.players.getValue() || add;
            add = entity instanceof EntityEnderCrystal && this.crystals.getValue() || add;
            add = entity instanceof EntityMob && this.mobs.getValue() || add;

            add = (entity instanceof EntityAnimal
                    || entity instanceof EntitySlime
                    || entity instanceof EntityWaterMob) && this.animals.getValue() || add;

            if (add) {
                entityList.add(entity);
            }
        }

        return entityList;
    }

    private void renderItems(List<Entity> entityList) {
        for (Entity entity : entityList) {
            final Vec3d vector = MathUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
            Objects.requireNonNull(mc.getRenderManager().getEntityRenderObject(entity))
                    .doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, mc.getRenderPartialTicks());
        }
    }
}
