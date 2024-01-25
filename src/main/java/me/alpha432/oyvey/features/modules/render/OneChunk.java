package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.TextUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OneChunk extends Module {
    private static OneChunk INSTANCE;
    private int renderDistance;

    private OneChunk() {
        super("OneChunk", "Sets your render distance to 1", Category.RENDER, false, false, true);
        this.renderDistance = OneChunk.mc.gameSettings.renderDistanceChunks;
    }

    public static OneChunk getInstance() {
        if (INSTANCE == null) INSTANCE = new OneChunk();
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        this.renderDistance = OneChunk.mc.gameSettings.renderDistanceChunks;
        OneChunk.mc.gameSettings.renderDistanceChunks = 1;
    }

    @Override
    public void onDisable() {
        OneChunk.mc.gameSettings.renderDistanceChunks = this.renderDistance;
    }

    /*
    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2) {
            log.info("AAAA " + event.toString());
        }
    }
    */
}