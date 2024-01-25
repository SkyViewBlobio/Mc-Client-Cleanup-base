package me.alpha432.oyvey.features.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

import javax.naming.Name;

public class NameChanger extends Module {
    public final Setting<String> NameString = register(new Setting<Object>("Name", "New Name Here"));

    private static NameChanger instance;

    public NameChanger() {
        super("NameChanger", "Changes name", Module.Category.CLIENT, false, false, false);
        instance = this;
    }

    @Override
    public void onEnable() {
        Command.sendMessage(ChatFormatting.DARK_PURPLE + "Success! Name succesfully changed to " + ChatFormatting.LIGHT_PURPLE + NameString.getValue());
    }

    public static NameChanger getInstance() {
        if (instance == null) {
            instance = new NameChanger();
        }
        return instance;
    }
}