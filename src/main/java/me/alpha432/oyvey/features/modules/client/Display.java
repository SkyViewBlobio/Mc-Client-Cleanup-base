package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class Display extends Module {

    private static Display INSTANCE = new Display();
    public Setting<String> title = this.register(new Setting("Title", "Xulu+ v"+ OyVey.MODVER));
    public Setting<Boolean> version = this.register(new Setting("version", true));

    public Display(){
        super("Title", "Sets the title of your game", Category.CLIENT, true, false, false);
        this.setInstance();
    }
    public static Display getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Display();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        org.lwjgl.opengl.Display.setTitle(this.title.getValue());
    }
}