package me.alpha432.oyvey.mixin.mixins;

import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ EntityLivingBase.class })
public interface IEntityLivingBase
{
    @Invoker("getArmSwingAnimationEnd")
    int getArmSwingAnimationEnd();
}
