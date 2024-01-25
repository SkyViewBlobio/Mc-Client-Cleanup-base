package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.TextUtil;

public class Description extends Module {
    public Description() {
        super("Info", "a neat way to see what each module does", Category.Flex, true, false, false);
    }
    @Override
    public void onEnable() {
        Command.sendMessage(TextUtil.GREEN + "Info ON");
    }

    @Override
    public void onDisable() {
        Command.sendMessage(TextUtil.RED + "Info OFF");
    }
}