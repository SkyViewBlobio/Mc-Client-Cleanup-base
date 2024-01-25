package me.alpha432.oyvey.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MessageManager {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static String prefix = TextFormatting.LIGHT_PURPLE + "[" + TextFormatting.DARK_PURPLE + "Xulu+" + TextFormatting.LIGHT_PURPLE + "]" + TextFormatting.RESET;

    public static void sendClientMessage(String message, boolean forcePermanent) {
        if (MessageManager.mc.player != null) {
            try {
                TextComponentString e = new TextComponentString(MessageManager.prefix + " " + message);
                int i = forcePermanent ? 0 : 12076;

                MessageManager.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(e, i);
            } catch (NullPointerException nullpointerexception) {
                nullpointerexception.printStackTrace();
            }

        }
    }
}