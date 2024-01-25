package me.alpha432.oyvey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiOptions;

public class DiscordPresence {
    public static DiscordRichPresence presence;
    private static final DiscordRPC rpc;
    private static Thread thread;

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        rpc = DiscordRPC.INSTANCE;
        presence = new DiscordRichPresence();
    }

    public static void start(){
        final DiscordEventHandlers handlers = new DiscordEventHandlers();

        DiscordPresence.rpc.Discord_Initialize("879361669250318389", handlers, true, "");
        DiscordPresence.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        DiscordPresence.presence.details = generateDetails();
        DiscordPresence.presence.state = "Starting up...";
        DiscordPresence.presence.largeImageKey = "aa";
        DiscordPresence.presence.largeImageText = "Xulu+ "+OyVey.MODVER;
        DiscordPresence.rpc.Discord_UpdatePresence(presence);

        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                DiscordPresence.rpc.Discord_RunCallbacks();

                DiscordPresence.presence.details = generateDetails();
                DiscordPresence.presence.state = "FixedByZocker_160 (^;";
                DiscordPresence.rpc.Discord_UpdatePresence(presence);

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ex) {}
            }
        }, "RPC-Callback-Handler");
        thread.start();
    }

    public static void shutdown() {
        if (DiscordPresence.thread != null && !DiscordPresence.thread.isInterrupted()) {
            DiscordPresence.thread.interrupt();
        }
        DiscordPresence.rpc.Discord_Shutdown();
    }

    private static String generateDetails() {
        // developer mode
        if (OyVey.MODVER.contains("DEV")) {
            return "Deving New Version "+OyVey.MODVER;
        }

        if (mc.currentScreen instanceof GuiMainMenu) {
            return "Vibin As Always";
        } else {
            StringBuilder stmp = new StringBuilder();
            stmp.setLength(0);
            stmp.append("Owning ");

            if (mc.getCurrentServerData() == null)
                return stmp.append("SadNiggaHours").toString();

            String sIP = mc.getCurrentServerData().serverIP;
            if (!sIP.isEmpty())
                stmp.append("Fags On ").append(sIP).append(".");
            else
                stmp.append("BigBoyMode");

            return stmp.toString();
        }
    }
}