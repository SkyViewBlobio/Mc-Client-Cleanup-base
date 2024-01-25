package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.SeconddaryInventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionUtils;
import java.util.List;
import java.util.Objects;

public class SelfShoot extends Module {
    private final Setting<Integer> tickDelay = register(new Setting("TickDelay", Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(8)));
    public SelfShoot() {
        super("SelfShoot", "shoots arrows at you", Category.Flex, true, false, false);
    }
    @Override
    public void onUpdate() {
        if(mc.player != null) {
            if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= tickDelay.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.cameraYaw, -90f, mc.player.onGround));
                mc.playerController.onStoppedUsingItem(mc.player);
            }
            List<Integer> arrowSlots = SeconddaryInventoryUtil.getItemInventory(Items.TIPPED_ARROW);
            if(arrowSlots.get(0) == -1) return;
            int speedSlot = -1;
            int strengthSlot = -1;
            for (Integer slot : arrowSlots)
            {
                if ((PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(slot)).getRegistryName()).getPath().contains("swiftness")) speedSlot = slot;
                else if (Objects.requireNonNull(PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(slot)).getRegistryName()).getPath().contains("strength")) strengthSlot = slot;
            }
        }
    }

    @Override
    public void onEnable() {

    }

    private int findBow() {
        return  SeconddaryInventoryUtil.getItemHotbar(Items.BOW);
    }
}