package me.alpha432.oyvey.features.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Timer;
import me.alpha432.oyvey.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.*;

public class HUD extends Module {
    private static final ResourceLocation box = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static final ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
    private static RenderItem itemRender;
    private static HUD INSTANCE = new HUD();
    private final Setting<Boolean> grayNess;
    private final Setting<Boolean> renderingUp;
    private final Setting waterMark;

    {
        waterMark = register(new Setting("watermark", Boolean.FALSE, "displays watermark"));
    }

    private final Setting waterMark2;

    private final Setting waterMark3;

    {
        waterMark2 = register(new Setting("welcomer", Boolean.FALSE, "displays watermark"));
        waterMark3 = register(new Setting("totemstext", Boolean.FALSE, "displays watermark"));
    }

    public Setting waterMarkY = register(new Setting("watermarkposy", 2, 0, 20, v -> (Boolean) waterMark.getValue()));
    public Setting waterMark2Y = register(new Setting("welcomerposy", 11, 0, 100, v -> (Boolean) waterMark2.getValue()));
    public Setting waterMark3Y = register(new Setting("totemstexty", 59, 0, 100, v -> (Boolean) waterMark3.getValue()));
    private final Setting<Boolean> arrayList = register(new Setting("ArrayList", Boolean.FALSE, "Lists the active modules."));
    private final Setting<Boolean> pvp = register(new Setting("info", false));
    private final Setting<Boolean> performance = register(new Setting("performanceinfo", false));
    private final Setting<Boolean> coords = register(new Setting("coords", Boolean.FALSE, "Your current coordinates"));
    private final Setting<Boolean> direction = register(new Setting("direction", Boolean.FALSE, "The Direction you are facing."));
    private final Setting<Boolean> armor = register(new Setting("armor", Boolean.FALSE, "ArmorHUD"));
    private final Setting<Boolean> totems = register(new Setting("totems", Boolean.FALSE, "TotemHUD"));
    private final Setting<Boolean> greeter = register(new Setting("AlternativeWatermark", Boolean.FALSE, "The time"));
    private final Setting<Boolean> speed = register(new Setting("speed", Boolean.FALSE, "Your Speed"));
    private final Setting<Boolean> potions = register(new Setting("potions", Boolean.FALSE, "Your Speed"));
    private final Setting<Boolean> ping = register(new Setting("ping", Boolean.FALSE, "Your response time to the server."));
    private final Setting<Boolean> tps = register(new Setting("tps", Boolean.FALSE, "Ticks per second of the server."));
    private final Setting<Boolean> fps = register(new Setting("fps", Boolean.FALSE, "Your frames per second."));
    private final Setting<Boolean> lag = register(new Setting("lagnotifier", Boolean.FALSE, "The time"));
    private final Timer timer = new Timer();
    private final Map<String, Integer> players = new HashMap<>();
    public Setting<String> command = register(new Setting("command", "Xulu+"));
    public Setting bracketColor = register(new Setting("bracketcolor", TextUtil.Color.LIGHT_PURPLE));
    public Setting commandColor = register(new Setting("namecolor", TextUtil.Color.DARK_PURPLE));
    public Setting<Boolean> rainbowPrefix = this.register(new Setting<Boolean>("rainbowprefix", false));
    public Setting<String> commandBracket = register(new Setting("bracket1", "["));
    public Setting<String> commandBracket2 = register(new Setting("nracket2", "]"));
    public Setting<Boolean> notifyToggles = register(new Setting("chatnotify", Boolean.FALSE, "notifys in chat"));
    public Setting<Boolean> magenDavid = register(new Setting("icon", Boolean.FALSE, "nigged"));
    public Setting<Integer> animationHorizontalTime = register(new Setting("animationhtime", 500, 1, 1000, v -> arrayList.getValue()));
    public Setting<Integer> animationVerticalTime = register(new Setting("animationvtime", 50, 1, 500, v -> arrayList.getValue()));
    public Setting<RenderingMode> renderingMode = register(new Setting("ordering", RenderingMode.ABC));
    public Setting<Boolean> time = register(new Setting("time", Boolean.FALSE, "The time"));
    public Setting<Integer> lagTime = register(new Setting("lagtime", 1000, 0, 2000));
    private int color;
    private boolean shouldIncrement;
    private int hitMarkerTimer;

    public HUD() {
        super("HUD", "nice HUD with many functions", Module.Category.CLIENT, true, false, false);
        setInstance();
        grayNess = register(new Setting("gray", Boolean.TRUE));
        renderingUp = register(new Setting("renderingup", Boolean.FALSE, "Orientation of the HUD-Elements."));
    }
    //Instance
    public static HUD getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HUD();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
    //Hit-marker stuff
    public void onUpdate() {
        if (shouldIncrement)
            hitMarkerTimer++;
        if (hitMarkerTimer == 10) {
            hitMarkerTimer = 0;
            shouldIncrement = false;
        }
    }
    //onRender method, here we display specific information on the screen
    public void onRender2D(Render2DEvent event) {
        if (fullNullCheck())
            return;
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        color = ColorUtil.toRGBA((Integer) (ClickGui.getInstance()).red.getValue(), (Integer) (ClickGui.getInstance())
                .green.getValue(), (Integer) (ClickGui.getInstance()).blue.getValue());
        if ((Boolean) waterMark.getValue()) {
            //cosmetics
            String string =(String) "Xulu+"
                    + ChatFormatting.LIGHT_PURPLE
                    + "v"+OyVey.MODVER+ ChatFormatting.WHITE
                    + " | " + ChatFormatting.RESET + ChatFormatting.BOLD
                    + Minecraft.debugFPS + ChatFormatting.WHITE + " Frames"
                    + " | "
                    + ChatFormatting.RESET + ChatFormatting.BOLD
                    + OyVey.serverManager.getPing() + ChatFormatting.WHITE
                    + " Ms"; //again ms? how many times does this client show us the ms

            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(string, 2.0F, (Integer) waterMarkY.getValue(), ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = { 1 };
                    char[] stringToCharArray = string.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 2.0F + f, (Integer) waterMarkY.getValue(),
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(string, 2.0F, (Integer) waterMarkY.getValue(), color, true);
            }
        }
        if(pvp.getValue()) {
            renderPvpInfo();
        }
        if(performance.getValue()) {
            renderPerformanceInfo();
        }

        if ((Boolean) waterMark2.getValue()) {
            String string = (String)"Xulu+ Getting Cracked With Da Homie " + mc.player.getDisplayNameString();;
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(string, 2.0F, (Integer) waterMark2Y.getValue(), ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = { 1 };
                    char[] stringToCharArray = string.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 2.0F + f, (Integer) waterMark2Y.getValue(),
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(string, 2.0F, (Integer) waterMark2Y.getValue(), color, true);
            }
        }

        if ((Boolean) waterMark3.getValue()) {
            String string = (String) ""
                    + ChatFormatting.BOLD
                    + ChatFormatting.WHITE
                    + mc.player.inventory.mainInventory.stream()
                    .filter(itemStack -> (itemStack.getItem()
                            == Items.TOTEM_OF_UNDYING))
                    .mapToInt(ItemStack::getCount).sum()
                    + ChatFormatting.RESET + " totems left";//totem information
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(string, 2.0F, (Integer) waterMark3Y.getValue(),
                            ColorUtil.rainbow((Integer)
                                    (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = { 1 };
                    char[] stringToCharArray = string.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 2.0F + f, (Integer) waterMark3Y.getValue(),
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(string, 2.0F, (Integer) waterMark3Y.getValue(), color, true);
            }
        }

        int[] counter1 = {1};
        int j = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && !renderingUp.getValue()) ? 14 : 0;
        if (arrayList.getValue())
            if (renderingUp.getValue()) {
                if (renderingMode.getValue() == RenderingMode.ABC) {
                    for (int k = 0; k < OyVey.moduleManager.sortedModulesABC.size(); k++) {
                        String str = OyVey.moduleManager.sortedModulesABC.get(k);
                        renderer.drawString(str,
                                (width - 2 - renderer.getStringWidth(str)),
                                (2 + j * 10), (ClickGui.getInstance()).rainbow.getValue() ?
                                        (((ClickGui.getInstance()).rainbowModeA.getValue()
                                                == ClickGui.rainbowModeArray.Up)
                                                ? ColorUtil.rainbow(counter1[0] *
                                                        (ClickGui.getInstance()).rainbowHue.getValue())
                                                .getRGB() : ColorUtil.rainbow((ClickGui.getInstance())
                                                .rainbowHue.getValue()).getRGB()) : color, true);
                        j++;
                        counter1[0] = counter1[0] + 1;
                    }
                } else {
                    for (int k = 0; k < OyVey.moduleManager.sortedModules.size(); k++) {
                        Module module = OyVey.moduleManager.sortedModules.get(k);
                        String str = module.getDisplayName() + ChatFormatting.LIGHT_PURPLE
                                + ((module.getDisplayInfo() != null)
                                ? (" [" + ChatFormatting.LIGHT_PURPLE
                                + module.getDisplayInfo()
                                + ChatFormatting.DARK_PURPLE
                                + "]") : "");
                        renderer.drawString(str, (width - 2 - renderer.getStringWidth(str)),
                                (2 + j * 10), (ClickGui.getInstance()).rainbow.getValue()
                                        ? (((ClickGui.getInstance()).rainbowModeA.getValue()
                                        == ClickGui.rainbowModeArray.Up)
                                        ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance())
                                        .rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance())
                                        .rainbowHue.getValue()).getRGB()) : color, true);
                        j++;
                        counter1[0] = counter1[0] + 1;
                    }
                }
            } else if (renderingMode.getValue() == RenderingMode.ABC) {
                for (int k = 0; k < OyVey.moduleManager.sortedModulesABC.size(); k++) {
                    String str = OyVey.moduleManager.sortedModulesABC.get(k);
                    j += 10;
                    renderer.drawString(str, (width - 2 - renderer.getStringWidth(str)), (height - j),
                            (ClickGui.getInstance()).rainbow.getValue() ?
                                    (((ClickGui.getInstance()).rainbowModeA.getValue()
                                            == ClickGui.rainbowModeArray.Up)
                                            ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance())
                                            .rainbowHue.getValue()).getRGB()
                                            : ColorUtil.rainbow((ClickGui.getInstance())
                                            .rainbowHue.getValue()).getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
            } else {
                for (int k = 0; k < OyVey.moduleManager.sortedModules.size(); k++) {
                    Module module = OyVey.moduleManager.sortedModules.get(k);
                    String str = module.getDisplayName()
                            //display info color
                            + ChatFormatting.LIGHT_PURPLE
                            + ((module.getDisplayInfo() != null) ? (" ["
                            + ChatFormatting.WHITE + module.getDisplayInfo()
                            + ChatFormatting.LIGHT_PURPLE + "]") : "");
                    j += 10;
                    renderer.drawString(str,
                            (width - 2 - renderer.getStringWidth(str))
                            , (height - j), (ClickGui.getInstance())
                                    .rainbow.getValue() ?
                                    (((ClickGui.getInstance()).rainbowModeA.getValue()
                                            == ClickGui.rainbowModeArray.Up) ?
                                            ColorUtil.rainbow(counter1[0]
                                                    * (ClickGui.getInstance())
                                                    .rainbowHue.getValue()).getRGB()
                                            : ColorUtil.rainbow((ClickGui.getInstance())
                                            .rainbowHue.getValue()).getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
            }
        String grayString = grayNess.getValue() ? String.valueOf(ChatFormatting.GRAY) : "";
        int i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && renderingUp.getValue())
                ? 13 : (renderingUp.getValue() ? -2 : 0);
        if (renderingUp.getValue()) {
            if (potions.getValue()) {
                List<PotionEffect> effects = new ArrayList<>((Minecraft.getMinecraft())
                        .player.getActivePotionEffects());
                for (PotionEffect potionEffect : effects) {
                    String str = OyVey.potionManager.getColoredPotionString(potionEffect);
                    i += 10;
                    renderer.drawString(str, (width - renderer.getStringWidth(str) - 2),
                            (height - 2 - i), potionEffect.getPotion().getLiquidColor(), true);
                }
            }
            if (speed.getValue()) {
                //get player speed
                String str = grayString
                        + "Speed "
                        + ChatFormatting.WHITE
                        + OyVey.speedManager.getSpeedKpH()
                        + " km/h";
                i += 10;
                renderer.drawString(str, (width - renderer.getStringWidth(str) - 2),
                        (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue()
                                ? (((ClickGui.getInstance()).rainbowModeA.getValue()
                                == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] *
                                (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                                : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue())
                                .getRGB()) : color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (time.getValue()) {
                //get the time
                String str = grayString
                        + "Time "
                        + ChatFormatting.WHITE
                        + (new SimpleDateFormat("h:mm a")).format(new Date());
                i += 10;
                renderer.drawString(str, (width - renderer.getStringWidth(str) - 2),
                        (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue() ?
                                (((ClickGui.getInstance()).rainbowModeA.getValue()
                                        == ClickGui.rainbowModeArray.Up)
                                        ? ColorUtil.rainbow(counter1[0] *
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                                        : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue())
                                        .getRGB()) : color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (tps.getValue()) {
                String str = grayString
                        + "TPS "
                        + ChatFormatting.WHITE
                        + OyVey.serverManager.getTPS();
                i += 10;
                renderer.drawString(str, (width - renderer.getStringWidth(str)
                        - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue()
                        ? (((ClickGui.getInstance()).rainbowModeA.getValue()
                        == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] *
                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB() : ColorUtil.rainbow
                        ((ClickGui.getInstance()).rainbowHue.getValue()).getRGB()) : color, true);
                counter1[0] = counter1[0] + 1;
            }
            String fpsText = grayString
                    + "FPS "
                    + ChatFormatting.WHITE
                    + Minecraft.debugFPS;
            String str1 = grayString
                    + "Ping "
                    + ChatFormatting.WHITE
                    + OyVey.serverManager.getPing();
            if (renderer.getStringWidth(str1) > renderer.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    i += 10;
                    renderer.drawString(str1, (width - renderer.getStringWidth(str1) - 2),
                            (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue() ?
                                    (((ClickGui.getInstance()).rainbowModeA.getValue()
                                            == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0]
                                            * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                                            : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue())
                                            .getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (fps.getValue()) {
                    i += 10;
                    renderer.drawString(fpsText, (width - renderer.getStringWidth(fpsText)
                            - 2), (height - 2 - i), (ClickGui.getInstance())
                            .rainbow.getValue() ? (((ClickGui.getInstance())
                            .rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up)
                            ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance())
                            .rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance())
                            .rainbowHue.getValue()).getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
            } else {
                if (fps.getValue()) {
                    i += 10;
                    renderer.drawString(fpsText, (width - renderer.getStringWidth(fpsText) - 2),
                            (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue() ? (((ClickGui.getInstance())
                                    .rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up)
                                    ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (ping.getValue()) {
                    i += 10;
                    renderer.drawString(str1, (width - renderer.getStringWidth(str1) - 2),
                            (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue() ? (((ClickGui.getInstance())
                                    .rainbowModeA.getValue()
                                    == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0]
                                    * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                                    : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue())
                                    .getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
            }
        } else {
            if (potions.getValue()) {
                List<PotionEffect> effects = new ArrayList<>((Minecraft.getMinecraft())
                        .player.getActivePotionEffects());
                for (PotionEffect potionEffect : effects) {
                    String str = OyVey.potionManager.getColoredPotionString(potionEffect);
                    renderer.drawString(str, (width - renderer.getStringWidth(str) - 2), (2 + i++ * 10),
                            potionEffect.getPotion().getLiquidColor(), true);
                }
            }
            if (speed.getValue()) {
                String str = grayString
                        + "Speed "
                        + ChatFormatting.WHITE
                        + OyVey.speedManager.getSpeedKpH()
                        + " km/h";
                renderer.drawString(str, (width - renderer.getStringWidth(str)
                        - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue()
                        ? (((ClickGui.getInstance()).rainbowModeA.getValue()
                        == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0]
                        * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                        : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRGB())
                        : color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (time.getValue()) {
                String str = grayString
                        + "Time "
                        + ChatFormatting.WHITE
                        + (new SimpleDateFormat("h:mm a")).format(new Date());
                renderer.drawString(str, (width - renderer.getStringWidth(str) - 2), (2 + i++ * 10),
                        (ClickGui.getInstance()).rainbow.getValue() ?
                                (((ClickGui.getInstance()).rainbowModeA.getValue()
                                        == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] *
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                                        : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue())
                                        .getRGB()) : color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (tps.getValue()) {
                String str = grayString
                        + "TPS "
                        + ChatFormatting.WHITE
                        + OyVey.serverManager.getTPS();
                renderer.drawString(str, (width - renderer.getStringWidth(str)
                        - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue() ? (((ClickGui.getInstance())
                        .rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0]
                        * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB() :
                        ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRGB())
                        : color, true);
                counter1[0] = counter1[0] + 1;
            }
            String fpsText = grayString
                    + "FPS "
                    + ChatFormatting.WHITE
                    + Minecraft.debugFPS;
            String str1 = grayString
                    + "Ping " + ChatFormatting.WHITE
                    + OyVey.serverManager.getPing();
            if (renderer.getStringWidth(str1) > renderer.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    renderer.drawString(str1,
                            (width - renderer.getStringWidth(str1) - 2), (2 + i++ * 10),
                            (ClickGui.getInstance()).rainbow.getValue() ? (((ClickGui.getInstance())
                                    .rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ?
                                    ColorUtil.rainbow(counter1[0] *
                                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB()
                                    : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRGB())
                                    : color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (fps.getValue()) {
                    renderer.drawString(fpsText, (width - renderer.getStringWidth(fpsText)
                            - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue()
                            ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up)
                            ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue())
                            .getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRGB())
                            : color, true);
                    counter1[0] = counter1[0] + 1;
                }
            } else {
                if (fps.getValue()) {
                    renderer.drawString(fpsText, (width - renderer.getStringWidth(fpsText) - 2), (2 + i++ * 10),
                            (ClickGui.getInstance()).rainbow.getValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue()
                                    == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] *
                                    (ClickGui.getInstance())
                                            .rainbowHue.getValue()).getRGB()
                                    : ColorUtil.rainbow((ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (ping.getValue()) {
                    renderer.drawString(str1, (width - renderer.getStringWidth(str1) - 2), (2 + i++ * 10),
                            (ClickGui.getInstance()).rainbow.getValue() ? (((ClickGui.getInstance())
                                    .rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up)
                                    ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB()
                                    : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue())
                                    .getRGB()) : color, true);
                    counter1[0] = counter1[0] + 1;
                }
            }
        }

        boolean inHell = mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("hell");
        int posX = (int) mc.player.posX;
        int posY = (int) mc.player.posY;
        int posZ = (int) mc.player.posZ;
        float nether = !inHell ? 0.125F : 8.0F;
        int hposX = (int) (mc.player.posX * nether);
        int hposZ = (int) (mc.player.posZ * nether);
        i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat) ? 14 : 0;
        String coordinates = ChatFormatting.WHITE
                + "xyz "
                + ChatFormatting.RESET
                + (inHell ? (posX
                + ", "
                + posY
                + ", "
                + posZ
                + ChatFormatting.WHITE
                + " ["
                + ChatFormatting.RESET
                + hposX + ", "
                + hposZ
                + ChatFormatting.LIGHT_PURPLE
                + "]"
                + ChatFormatting.RESET) : (posX
                + ", "
                + posY
                + ", "
                + posZ
                + ChatFormatting.LIGHT_PURPLE
                + " ["
                + ChatFormatting.RESET
                + hposX
                + ", "
                + hposZ
                + ChatFormatting.LIGHT_PURPLE
                + "]"));
        String direction = this.direction.getValue() ? OyVey.rotationManager.getDirection4D(false) : "";
        String coords = this.coords.getValue() ? coordinates : "";
        i += 10;
        if ((ClickGui.getInstance()).rainbow.getValue()) {
            String rainbowCoords = this.coords.getValue() ? ("xyz "
                    + ((posX
                    + ", "
                    + posY
                    + ", "
                    + posZ
                    + " ["
                    + hposX
                    + ", "
                    + hposZ
                    + "]"))) : "";
            if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                renderer.drawString(direction, 2.0F, (height - i - 11), ColorUtil.rainbow((ClickGui.getInstance())
                        .rainbowHue.getValue()).getRGB(), true);
                renderer.drawString(rainbowCoords, 2.0F, (height - i), ColorUtil.rainbow((ClickGui.getInstance())
                        .rainbowHue.getValue()).getRGB(), true);
            } else {
                int[] counter2 = {1};
                char[] stringToCharArray = direction.toCharArray();
                float s = 0.0F;
                for (char c : stringToCharArray) {
                    renderer.drawString(String.valueOf(c), 2.0F + s, (height - i - 11),
                            ColorUtil.rainbow(counter2[0] * (ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB(), true);
                    s += renderer.getStringWidth(String.valueOf(c));
                    counter2[0] = counter2[0] + 1;
                }
                int[] counter3 = {1};
                char[] stringToCharArray2 = rainbowCoords.toCharArray();
                float u = 0.0F;
                for (char c : stringToCharArray2) {
                    renderer.drawString(String.valueOf(c), 2.0F + u, (height - i),
                            ColorUtil.rainbow(counter3[0] * (ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB(), true);
                    u += renderer.getStringWidth(String.valueOf(c));
                    counter3[0] = counter3[0] + 1;
                }
            }
        } else {
            renderer.drawString(direction, 2.0F, (height - i - 11), color, true);
            renderer.drawString(coords, 2.0F, (height - i), color, true);
        }
        if (armor.getValue())
            renderArmorHUD(true);
        if (totems.getValue())
            renderTotemHUD();
        if (greeter.getValue())
            renderGreeter();
        if (lag.getValue())
            renderLag();
    }

    public void renderGreeter() {
        int width = renderer.scaledWidth;
        String text = " Client: Xulu+ Build: v"
                +OyVey.MODVER
                +" Player: ";
        if (greeter.getValue())
            text = text
                    + mc.player.getDisplayNameString();
        if ((ClickGui.getInstance()).rainbow.getValue()) {
            if ((ClickGui.getInstance()).rainbowModeHud.getValue()
                    == ClickGui.rainbowMode.Static) {
                renderer.drawString(text, width / 2.0F - renderer.getStringWidth(text)
                        / 2.0F + 2.0F, 2.0F, ColorUtil.rainbow((ClickGui.getInstance())
                        .rainbowHue.getValue()).getRGB(), true);
            } else {
                int[] counter1 = {1};
                char[] stringToCharArray = text.toCharArray();
                float i = 0.0F;
                for (char c : stringToCharArray) {
                    renderer.drawString(String.valueOf(c), width / 2.0F - renderer.getStringWidth(text)
                            / 2.0F + 2.0F + i, 2.0F, ColorUtil.rainbow(counter1[0] *
                                    (ClickGui.getInstance()).rainbowHue.getValue())
                            .getRGB(), true);
                    i += renderer.getStringWidth(String.valueOf(c));
                    counter1[0] = counter1[0] + 1;
                }
            }
        } else {
            renderer.drawString(text, width
                    / 2.0F - renderer.getStringWidth(text)
                    / 2.0F + 2.0F, 2.0F, color, true);
        }
    }

    public void renderLag() {
        int width = renderer.scaledWidth;
        if (OyVey.serverManager.isServerNotResponding()) {
            String text = ChatFormatting.LIGHT_PURPLE
                    + "server lagging for "
                    + MathUtil.round((float)
                    OyVey.serverManager.serverRespondingTime() / 1000.0F, 1)
                    + "s.";
            renderer.drawString(text, width
                    / 2.0F - renderer.getStringWidth(text)
                    / 2.0F + 2.0F, 20.0F, color, true);
        }
    }

    public void renderTotemHUD() {
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        int totems = mc.player.inventory.mainInventory.stream().filter(itemStack ->
                        (itemStack.getItem() == Items.TOTEM_OF_UNDYING))
                .mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)
            totems += mc.player.getHeldItemOffhand().getCount();
        if (totems > 0) {
            GlStateManager.enableTexture2D();
            int i = width / 2;
            int y = height - 55 - ((mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure()) ? 10 : 0);
            int x = i - 189 + 180 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200.0F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(totem, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, totem, x, y, "");
            RenderUtil.itemRender.zLevel = 0.0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(totems + "", (x + 19 - 2 - renderer.getStringWidth(totems + ""))
                    , (y + 9), 16777215);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }

    public void renderArmorHUD(boolean percent) {
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        GlStateManager.enableTexture2D();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
        for (ItemStack is : mc.player.inventory.armorInventory) {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            renderer.drawStringWithShadow(s, x + 19 - 2 - renderer.getStringWidth(s), y + 9, 0xffffff);
            if (percent) {
                int dmg = 0;
                is.getMaxDamage();
                is.getItemDamage();
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1 - green;
                dmg = 100 - (int) (red * 100);
                renderer.drawStringWithShadow(dmg + "", x + 8 - (float)
                                renderer.getStringWidth(dmg + "") / 2, y - 11,
                        ColorUtil.toRGBA((int) (red * 255), (int) (green * 255), 0));
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    public void renderPvpInfo() {
        //combat module information
        String caOn = ChatFormatting.WHITE + "CA:" + ChatFormatting.GREEN + " ON";
        String caOff = ChatFormatting.WHITE + "CA:" + ChatFormatting.DARK_RED + " OFF";
        String suOn = ChatFormatting.WHITE + "SR:" + ChatFormatting.GREEN + " ON";
        String suOff = ChatFormatting.WHITE + "SR:" + ChatFormatting.DARK_RED + " OFF";
        String speOn = ChatFormatting.WHITE + "SP:" + ChatFormatting.GREEN + " ON";
        String speOff = ChatFormatting.WHITE + "SP:" + ChatFormatting.DARK_RED + " OFF";
        String steOn = ChatFormatting.WHITE + "ST:" + ChatFormatting.GREEN + " ON";
        String steOff = ChatFormatting.WHITE + "ST:" + ChatFormatting.DARK_RED + " OFF";
        String hlOn = ChatFormatting.WHITE + "HF:" + ChatFormatting.GREEN + " ON";
        String hlOff = ChatFormatting.WHITE + "HF:" + ChatFormatting.DARK_RED + " OFF";

        if (OyVey.moduleManager.getModuleByName("CrystalAura[X+Silent]").isEnabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(caOn, 4.0F, 20.0f, ColorUtil.rainbow((Integer) (ClickGui.getInstance())
                            .rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = caOn.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 20.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(caOn, 4.0F, 20.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("Surround").isEnabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(suOn, 4.0F, 30.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = suOn.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 30.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(suOn, 4.0F, 30.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("Speed").isEnabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(speOn, 4.0F, 40.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = speOn.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 40.0f, ColorUtil.rainbow
                                (arrayOfInt[0] * (Integer) (ClickGui.getInstance())
                                        .rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(speOn, 4.0F, 40.0f, color, true);
            }
        }

        if (OyVey.moduleManager.getModuleByName("Step").isEnabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(steOn, 4.0F, 50.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = steOn.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 50.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(steOn, 4.0F, 50.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("HoleFiller").isEnabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(hlOn, 4.0F, 60.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = hlOn.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 60.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(hlOn, 4.0F, 60.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("CrystalAura[X+Silent]").isDisabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(caOff, 4.0F, 20.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = caOff.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 20.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(caOff, 4.0F, 20.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("Surround").isDisabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(suOff, 4.0F, 30.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = suOff.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 30.0F, ColorUtil.rainbow
                                (arrayOfInt[0] * (Integer) (ClickGui.getInstance())
                                        .rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(suOff, 4.0F, 30.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("Speed").isDisabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(speOff, 4.0F, 40.0f, ColorUtil.rainbow((Integer)
                            (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = speOff.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 40.0F,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(speOff, 4.0F, 40.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("Step").isDisabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(steOff, 4.0F, 50.0f, ColorUtil.rainbow((Integer)
                                    (ClickGui.getInstance()).rainbowHue.getValue())
                            .getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = steOff.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 50.0F,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(steOff, 4.0F, 50.0f, color, true);
            }
        }
        if (OyVey.moduleManager.getModuleByName("HoleFiller").isDisabled()) {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(hlOff, 4.0F, 60.0f, ColorUtil.rainbow((Integer)
                                    (ClickGui.getInstance()).rainbowHue.getValue())
                            .getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = hlOff.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 60.0F,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(hlOff, 4.0F, 60.0f, color, true);
            }
        }
    }

    public void renderPerformanceInfo() {
        String fps = ChatFormatting.WHITE
                + "FPS: "
                + ChatFormatting.RESET
                + Minecraft.debugFPS;
        String ping = ChatFormatting.WHITE
                + "Ping: "
                + ChatFormatting.RESET
                + OyVey.serverManager.getPing();
        String tps = ChatFormatting.WHITE
                + "TPS: "
                + ChatFormatting.RESET
                + OyVey.serverManager.getTPS();
        {
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(fps, 4.0F, 70.0f,
                            ColorUtil.rainbow((Integer) (ClickGui.getInstance())
                                    .rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = fps.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 70.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(fps, 4.0F, 70.0f, color, true);
            }
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(ping, 4.0F, 80.0f, ColorUtil.rainbow((Integer)
                                    (ClickGui.getInstance()).rainbowHue.getValue())
                            .getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = ping.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 80.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(ping, 4.0F, 80.0f, color, true);
            }
            if ((Boolean) (ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(tps, 4.0F, 90.0f, ColorUtil.rainbow((Integer) (ClickGui.getInstance())
                            .rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = tps.toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 4.0F + f, 90.0f,
                                ColorUtil.rainbow(arrayOfInt[0] * (Integer)
                                        (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(tps, 4.0F, 90.0f, color, true);
            }
        }
    }


    @SubscribeEvent
    public void onUpdateWalkingPlayer(AttackEntityEvent event) {
        shouldIncrement = true;
    }

    public void onLoad() {
        OyVey.commandManager.setClientMessage(getCommandMessage());
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 &&
                equals(event.getSetting().getFeature()))
            OyVey.commandManager.setClientMessage(getCommandMessage());
    }


    public String getCommandMessage() {
        if (this.rainbowPrefix.getPlannedValue()) {
            StringBuilder stringBuilder = new StringBuilder(this.getRawCommandMessage());
            stringBuilder.insert(0, "§+");
            stringBuilder.append("§r");
            return stringBuilder.toString();
        }
        return TextUtil.coloredString(this.commandBracket.getPlannedValue(),
                (TextUtil.Color) this.bracketColor.getPlannedValue())
                + TextUtil.coloredString(this.command.getPlannedValue(),
                (TextUtil.Color) this.commandColor.getPlannedValue())
                + TextUtil.coloredString(this.commandBracket2.getPlannedValue(),
                (TextUtil.Color) this.bracketColor.getPlannedValue());
    }

    public String getRainbowCommandMessage() {
        StringBuilder stringBuilder = new StringBuilder(this.getRawCommandMessage());
        stringBuilder.insert(0, "§+");
        stringBuilder.append("§r");
        return stringBuilder.toString();
    }

    public String getRawCommandMessage() {
        return this.commandBracket.getValue() + this.command.getValue() + this.commandBracket2.getValue();
    }

    public void drawTextRadar(int yOffset) {
        if (!players.isEmpty()) {
            int y = renderer.getFontHeight() + 7 + yOffset;
            for (Map.Entry<String, Integer> player : players.entrySet()) {
                String text = player.getKey() + " ";
                int textheight = renderer.getFontHeight() + 1;
                renderer.drawString(text, 2.0F, y, color, true);
                y += textheight;
            }
        }
    }

    public enum RenderingMode {
        Length, ABC
    }
}