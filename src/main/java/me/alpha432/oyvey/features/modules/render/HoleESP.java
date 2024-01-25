package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.ColorSetting;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public
class HoleESP extends Module {
    private static HoleESP INSTANCE;

    public Setting<Integer> range = this.register(new Setting<Integer>("Range", 5, 1, 20));
    public Setting<Boolean> hideOwn = this.register(new Setting<Boolean>("Hide Own", false));
    public Setting<RenderMode> mode = this.register(new Setting<RenderMode>("Render", RenderMode.Pretty));

    public ColorSetting bedrockColor = new ColorSetting("Bedrock Color", new OyColor(0, 255, 0, 100));
    public ColorSetting bedrockColor2 = new ColorSetting("Bedrock Outline", new OyColor(0, 255, 0, 100));
    public ColorSetting obsidianColor = new ColorSetting("Obsidian Color", new OyColor(255, 0, 0, 100));
    public ColorSetting obsidianColor2 = new ColorSetting("Obsidian Outline", new OyColor(255, 0, 0, 100));

    public Setting<Double> lineWidth = this.register(new Setting<Double>("Line Width", 1.0, -1.0, 2.0, s -> mode.equals(RenderMode.Outline)));
    public Setting<Double> Height = this.register(new Setting<Double>("Height", 1.0, -1.0, 2.0, s -> mode.equals(RenderMode.Fade)));
    public Setting<Boolean> invertFill = this.register(new Setting<Boolean>("Invert Fill", false, s -> mode.equals(RenderMode.Fade)));
    public Setting<Boolean> invertLine = this.register(new Setting<Boolean>("Invert Line", false, s -> mode.equals(RenderMode.Fade)));
    public Setting<RmodeEnum> RMode = this.register(new Setting<RmodeEnum>("Color Mode", RmodeEnum.Rainbow));
    public Setting<SinModeEnum> SinMode = this.register(new Setting<SinModeEnum>("Sine Mode", SinModeEnum.Special, s -> RMode.equals(RmodeEnum.Sin)));
    public Setting<Integer> RDelay = this.register(new Setting<Integer>("Rainbow Delay", 500, 0, 2500));
    public Setting<Integer> FillUp = this.register(new Setting<Integer>("Fill Up", 80, 0, 255, s -> mode.equals(RenderMode.Fade)));
    public Setting<Integer> FillDown = this.register(new Setting<Integer>("Fill Down", 0, 0, 255, s -> mode.equals(RenderMode.Fade)));
    public Setting<Integer> LineFillUp = this.register(new Setting<Integer>("Liner Fill Up", 80, 0, 255, s -> mode.equals(RenderMode.Fade)));
    public Setting<Integer> LineFillDown = this.register(new Setting<Integer>("Liner Fill Down", 0, 0, 255, s -> mode.equals(RenderMode.Fade)));

    public Setting<CustomHolesEnum> customHoles = this.register(new Setting<CustomHolesEnum>("Show", CustomHolesEnum.Single));
    private final ConcurrentHashMap<BlockPos, OyPair<OyColor, Boolean>> holes = new ConcurrentHashMap<>();

    public enum RenderMode {
        Pretty,
        Solid,
        Outline,
        Gradient,
        Fade
    }

    public enum CustomHolesEnum {
        Single,
        Double,
        Custom
    }

    public enum SinModeEnum {
        Special,
        Hue,
        Saturation,
        Brightness
    }

    public enum RmodeEnum {
        Rainbow,
        Sin
    }

    private HoleESP() {
        super("HoleESP","Shows safe spots.", Module.Category.RENDER ,false ,false,false);
    }

    public static HoleESP getInstance() {
        if (INSTANCE == null) INSTANCE = new HoleESP();
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.holes.isEmpty()) return;
        this.holes.forEach(this::renderHoles);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        this.holes.clear();
        int range = this.range.getValue();

        HashSet<BlockPos> possibleHoles = new HashSet<>();
        List<BlockPos> blockPosList = EntityUtil.getSphere(PlayerUtil.getPlayerPos(), range, range, false, true, 0);

        for (BlockPos pos : blockPosList) {
            if (!EntityUtil.posEqualsBlock(pos, Blocks.AIR)) continue;
            if (EntityUtil.posEqualsBlock(pos.add(0, -1, 0), Blocks.AIR)) continue;
            if (!EntityUtil.posEqualsBlock(pos.add(0, 1, 0), Blocks.AIR)) continue;

            if (EntityUtil.posEqualsBlock(pos.add(0, 2, 0), Blocks.AIR)) possibleHoles.add(pos);
        }

        for (BlockPos pos : possibleHoles) {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, false);
            HoleUtil.HoleType holeType = holeInfo.getType();

            if (holeType != HoleUtil.HoleType.NONE) {
                HoleUtil.BlockSafety holeSafety = holeInfo.getSafety();
                AxisAlignedBB centreBlocks = holeInfo.getCentre();

                if (centreBlocks == null)
                    continue;

                OyColor colour;
                if (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE) {
                    colour = bedrockColor.getValue();
                } else {
                    colour = obsidianColor.getValue();
                }
                boolean safe = (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE);
                if (customHoles.equals(CustomHolesEnum.Custom) && (holeType == HoleUtil.HoleType.CUSTOM || holeType == HoleUtil.HoleType.DOUBLE)) {
                    OyPair<OyColor, Boolean> p = new OyPair<>(colour, safe);
                    holes.put(pos, p);
                } else if (customHoles.equals(CustomHolesEnum.Double) && holeType == HoleUtil.HoleType.DOUBLE) {
                    OyPair<OyColor, Boolean> p = new OyPair<>(colour, safe);
                    holes.put(pos, p);
                } else if (holeType == HoleUtil.HoleType.SINGLE) {
                    OyPair<OyColor, Boolean> p = new OyPair<>(colour, safe);
                    holes.put(pos, p);
                }
            }
        }
    }

    private void renderHoles(BlockPos hole, OyPair<OyColor, Boolean> pair) {
        boolean safe = pair.getValue();

        if (hideOwn.getValue() && hole.equals(PlayerUtil.getPlayerPos())) return;

        if (!mode.equals(RenderMode.Gradient) && !mode.equals(RenderMode.Fade)) {
            boolean outline = false;
            boolean solid = false;

            switch (mode.getValue()) {
                case Pretty:
                    outline = true;
                    solid = true;
                    break;
                case Solid:
                    outline = false;
                    solid = true;
                    break;
                case Outline:
                    outline = true;
                    solid = false;
                    break;
            }
            RenderUtil.drawBoxESP(hole,
                    safe ? bedrockColor.getValue() : obsidianColor.getValue(),
                    safe ? bedrockColor2.getValue() : obsidianColor2.getValue(),
                    lineWidth.getValue(),
                    outline, solid, (float) (Height.getValue() - 1));

        } else {
            if (mode.equals(RenderMode.Gradient)) {
                RenderUtil.drawGlowBox(hole, Height.getValue() - 1, lineWidth.getValue().floatValue(), safe ? bedrockColor.getValue() : obsidianColor.getValue(), safe ? bedrockColor2.getValue() : obsidianColor2.getValue());
            } else {
                RenderUtil.drawOpenGradientBox(hole, (!invertFill.getValue()) ? getGColor(safe, false, false) : getGColor(safe, true, false),
                        (!invertFill.getValue()) ? getGColor(safe, true, false) : getGColor(safe, false, false), 0);
                RenderUtil.drawGradientBlockOutline(hole, (invertLine.getValue()) ? getGColor(safe, false, true) : getGColor(safe, true, true),
                        (invertLine.getValue()) ? getGColor(safe, true, true) : getGColor(safe, false, true), 2f, 0);
            }
        }
    }

    private Color getGColor(boolean safe, boolean top, boolean line) {
        Color rVal;
        ColorUtil.type type = null;
        switch (SinMode.getValue()){
            case Special:
                type = ColorUtil.type.SPECIAL;
                break;
            case Saturation:
                type = ColorUtil.type.SATURATION;
                break;
            case Brightness:
                type = ColorUtil.type.BRIGHTNESS;
                break;
        }

        if (!safe) {
            if (obsidianColor.getRainbow()) {
                if (RMode.equals(RmodeEnum.Rainbow)) {
                    if (top){
                        rVal = ColorUtil.releasedDynamicRainbow(0, (line) ? LineFillUp.getValue() : FillUp.getValue());
                    } else {
                        rVal = ColorUtil.releasedDynamicRainbow(RDelay.getValue(), (line) ? LineFillDown.getValue() : FillDown.getValue());
                    }
                }else {
                    if(SinMode.equals(SinModeEnum.Hue)){
                        if (top) {
                            rVal = ColorUtil.getSinState(obsidianColor.getColor(),obsidianColor2.getColor() ,1000,(line) ? LineFillUp.getValue() : FillUp.getValue());
                        }else {
                            rVal = ColorUtil.getSinState(obsidianColor.getColor(),obsidianColor2.getColor() ,RDelay.getValue(), (line) ? LineFillDown.getValue() : FillDown.getValue());
                        }
                    }else {
                        if (top) {
                            rVal = ColorUtil.getSinState(obsidianColor.getColor(), 1000, (line) ? LineFillUp.getValue() : FillUp.getValue(), type);
                        } else {
                            rVal = ColorUtil.getSinState(obsidianColor.getColor(), RDelay.getValue(), (line) ? LineFillDown.getValue() : FillDown.getValue(), type);
                        }
                    }
                }
            } else {
                if (top) {
                    rVal = new OyColor(obsidianColor.getColor().getRed(), obsidianColor.getColor().getGreen(), obsidianColor.getColor().getBlue(), (line) ? LineFillUp.getValue() : FillUp.getValue());
                } else {
                    rVal = new OyColor(obsidianColor.getColor().getRed(), obsidianColor.getColor().getGreen(), obsidianColor.getColor().getBlue(), (line) ? LineFillDown.getValue() : FillDown.getValue());
                }
            }
        } else {
            if (bedrockColor.getRainbow()) {
                if (RMode.equals(RmodeEnum.Rainbow)) {
                    if (top) {
                        rVal = ColorUtil.releasedDynamicRainbow(0, (line) ? LineFillUp.getValue() : FillUp.getValue());
                    } else {
                        rVal = ColorUtil.releasedDynamicRainbow(RDelay.getValue(), (line) ? LineFillDown.getValue() : FillDown.getValue());
                    }
                }else {
                    if(SinMode.equals(SinModeEnum.Hue)){
                        if (top) {
                            rVal = ColorUtil.getSinState(bedrockColor.getColor(), bedrockColor2.getColor() ,1000,(line) ? LineFillUp.getValue() : FillUp.getValue());
                        }else {
                            rVal = ColorUtil.getSinState(bedrockColor.getColor(), bedrockColor2.getColor() ,RDelay.getValue(), (line) ? LineFillDown.getValue() : FillDown.getValue());
                        }
                    }else {
                        if (top) {
                            rVal = ColorUtil.getSinState(bedrockColor.getColor(), 1000, (line) ? LineFillUp.getValue() : FillUp.getValue(), type);
                        } else {
                            rVal = ColorUtil.getSinState(bedrockColor.getColor(), RDelay.getValue(), (line) ? LineFillDown.getValue() : FillDown.getValue(), type);
                        }
                    }
                }
            } else {
                if (top) {
                    rVal = new OyColor(bedrockColor.getColor().getRed(), bedrockColor.getColor().getGreen(), bedrockColor.getColor().getBlue(), (line) ? LineFillUp.getValue() : FillUp.getValue());
                } else {
                    rVal = new OyColor(bedrockColor.getColor().getRed(), bedrockColor.getColor().getGreen(), bedrockColor.getColor().getBlue(), (line) ? LineFillDown.getValue() : FillDown.getValue());
                }
            }
        }
        return rVal;
    }

    @Override
    public String getDisplayInfo() {
        return "" + holes.size();
    }
}