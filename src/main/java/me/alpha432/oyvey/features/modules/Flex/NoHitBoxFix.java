package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.features.modules.*;
import me.alpha432.oyvey.features.setting.*;

public class NoHitBoxFix extends Module
{
    private static NoHitBoxFix INSTANCE;
    public Setting<Boolean> pickaxe;
    public Setting<Boolean> crystal;
    public Setting<Boolean> gapple;

    public NoHitBoxFix() {
        super("NoHitBoxFix", "NoHitBox.", Category.Flex, false, false, false);
        this.pickaxe = (Setting<Boolean>)this.register(new Setting("Pickaxe", (Object)true));
        this.crystal = (Setting<Boolean>)this.register(new Setting("Crystal", (Object)true));
        this.gapple = (Setting<Boolean>)this.register(new Setting("Gapple", (Object)true));
        this.setInstance();
    }

    public static NoHitBoxFix getINSTANCE() {
        if (NoHitBoxFix.INSTANCE == null) {
            NoHitBoxFix.INSTANCE = new NoHitBoxFix();
        }
        return NoHitBoxFix.INSTANCE;
    }

    private void setInstance() {
        NoHitBoxFix.INSTANCE = this;
    }

    static {
        NoHitBoxFix.INSTANCE = new NoHitBoxFix();
    }
}
