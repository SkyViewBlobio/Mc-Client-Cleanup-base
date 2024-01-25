
package me.alpha432.oyvey.mixin.mixins.XuluProtect;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={EntityLivingBase.class})
public interface XuluLivingBase {
    @Invoker(value="getArmSwingAnimationEnd")
    public int getArmSwingAnimationEnd();
}
