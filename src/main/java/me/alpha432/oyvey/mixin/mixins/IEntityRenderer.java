package me.alpha432.oyvey.mixin.mixins;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface IEntityRenderer {
    @Invoker("setupCameraTransform")
    void invokeSetupCameraTransform(float partialTicks, int pass);

    @Invoker("renderHand")
    void invokeRenderHand(float paritalTicks, int pass);

    @Accessor("lightmapUpdateNeeded")
    void setLightmapUpdateNeeded(boolean needed);

    /*
    @Invoker("orientCamera")
    void setupOrientCameraInvoker(float partialTicks);

    @Accessor("lightmapUpdateNeeded")
    void setupLightmapUpdateNeededAccessor(boolean needed);
     */
}
