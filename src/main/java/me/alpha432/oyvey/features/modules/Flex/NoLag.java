



package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.features.modules.*;
import me.alpha432.oyvey.features.setting.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import me.alpha432.oyvey.util.*;
import java.util.*;
import me.alpha432.oyvey.event.events.*;
import net.minecraft.util.*;
import net.minecraft.init.*;
import me.alpha432.oyvey.features.modules.combat.*;
import net.minecraftforge.fml.common.eventhandler.*;
import com.google.common.collect.*;

public class NoLag extends Module
{
    private static final HashSet<Object> BLACKLIST;
    private static NoLag instance;
    public Setting<Boolean> crystals;
    public Setting<Boolean> armor;
    public Setting<Float> soundRange;

    public NoLag() {
        super("-Procrastinate",  "Tries to Disable Lags",  Category.Flex,  true,  false,  false);
        this.crystals = (Setting<Boolean>)this.register(new Setting("C-AntiLag", true));
        this.armor = (Setting<Boolean>)this.register(new Setting("A-AntiLag", true));
        this.soundRange = (Setting<Float>)this.register(new Setting("LagDisableRange", 12.0f, 0.0f, 12.0f));
        NoLag.instance = this;
    }

    public static NoLag getInstance() {
        if (NoLag.instance == null) {
            NoLag.instance = new NoLag();
        }
        return NoLag.instance;
    }

    public static void removeEntities(final SPacketSoundEffect packet,  final float range) {
        final BlockPos pos = new BlockPos(packet.getX(),  packet.getY(),  packet.getZ());
        final ArrayList<Entity> toRemove = new ArrayList<Entity>();
        if (fullNullCheck()) {
            return;
        }
        for (final Entity entity : NoLag.mc.world.loadedEntityList) {
            if (entity instanceof EntityEnderCrystal) {
                if (entity.getDistanceSq(pos) > MathUtil.square(range)) {
                    continue;
                }
                toRemove.add(entity);
            }
        }
        for (final Entity entity : toRemove) {
            entity.setDead();
        }
    }

    @SubscribeEvent
    public void onPacketReceived(final PacketEvent.Receive event) {
        if (event != null && event.getPacket() != null && NoLag.mc.player != null && NoLag.mc.world != null && event.getPacket() instanceof SPacketSoundEffect) {
            final SPacketSoundEffect packet = (SPacketSoundEffect)event.getPacket();
            if (this.crystals.getValue() && packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE && (AutoCrystal.getInstance().isOff() || (!AutoCrystal.getInstance().sound.getValue() && AutoCrystal.getInstance().threadMode.getValue() != AutoCrystal.ThreadMode.SOUND))) {
                removeEntities(packet,  this.soundRange.getValue());
            }
            if (NoLag.BLACKLIST.contains(packet.getSound()) && this.armor.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    static {
        BLACKLIST = Sets.newHashSet((Object[])new SoundEvent[] { SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,  SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA,  SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,  SoundEvents.ITEM_ARMOR_EQUIP_IRON,  SoundEvents.ITEM_ARMOR_EQUIP_GOLD,  SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,  SoundEvents.ITEM_ARMOR_EQUIP_LEATHER });
    }
}