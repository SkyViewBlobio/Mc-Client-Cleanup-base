package me.alpha432.oyvey.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Bind;
import org.lwjgl.input.Keyboard;

public class BindCommand
        extends Command {
    public BindCommand() {
        super("bind", new String[]{"<module>", "<bind>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            BindCommand.sendMessage("Please specify a module.");
            return;
        }
        String moduleName = commands[0];
        String rkey = commands[1];
        Module module = OyVey.moduleManager.getModuleByName(moduleName);
        if (module == null) {
            BindCommand.sendMessage("Unknown module '" + moduleName + "'!");
            BindCommand.sendMessage("Available modules starting with '"+moduleName+"':");
            for (String modules : OyVey.moduleManager.getModules()) {
                if (modules.toLowerCase().startsWith(moduleName.toLowerCase())) {
                    BindCommand.sendSilentMessage(ChatFormatting.DARK_PURPLE + " - "
                        + ChatFormatting.LIGHT_PURPLE + modules);
                }
            }
            return;
        }
        if (rkey == null) {
            BindCommand.sendMessage(module.getName() + " is bound to " + ChatFormatting.LIGHT_PURPLE + module.getBind().toString());
            return;
        }
        int key = Keyboard.getKeyIndex(rkey.toUpperCase());
        if (rkey.equalsIgnoreCase("none")) {
            key = -1;
        }
        if (key == 0) {
            BindCommand.sendMessage("Unknown key '" + rkey + "'!");
            return;
        }
        module.bind.setValue(new Bind(key));
        BindCommand.sendMessage("Bind for " + ChatFormatting.DARK_PURPLE + module.getName() + ChatFormatting.WHITE + " set to " + ChatFormatting.LIGHT_PURPLE + rkey.toUpperCase());
    }
}

