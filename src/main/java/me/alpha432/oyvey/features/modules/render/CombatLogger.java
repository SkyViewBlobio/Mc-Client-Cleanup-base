package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.ConnectionEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.command.Command3;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.Colors;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.command.CommandGameMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public
class CombatLogger
        extends Module {
    private final Setting < Boolean > colorSync = this.register ( new Setting <> ( "Sync" , false ) );
    private final Setting < Integer > red = this.register ( new Setting <> ( "Red" , 255 , 0 , 255 ) );
    private final Setting < Integer > green = this.register ( new Setting <> ( "Green" , 0 , 0 , 255 ) );
    private final Setting < Integer > blue = this.register ( new Setting <> ( "Blue" , 0 , 0 , 255 ) );
    private final Setting < Integer > alpha = this.register ( new Setting <> ( "Alpha" , 255 , 0 , 255 ) );
    private final Setting < Boolean > scaleing = this.register ( new Setting <> ( "Scale" , false ) );
    private final Setting < Float > scaling = this.register ( new Setting <> ( "Size" , 4.0f , 0.1f , 20.0f ) );
    private final Setting < Float > factor = this.register ( new Setting < Object > ( "Factor" , 0.3f , 0.1f , 1.0f , v -> this.scaleing.getValue ( ) ) );
    private final Setting < Boolean > smartScale = this.register ( new Setting < Object > ( "SmartScale" , Boolean.FALSE , v -> this.scaleing.getValue ( ) ) );
    private final Setting < Boolean > rect = this.register ( new Setting <> ( "Rectangle" , true ) );
    private final Setting < Boolean > coords = this.register ( new Setting <> ( "Coords" , true ) );
    private final Setting < Boolean > notification = this.register ( new Setting <> ( "Notification" , true ) );
    private final List < LogoutPos > spots = new CopyOnWriteArrayList <> ( );
    public Setting < Float > range = this.register ( new Setting <> ( "Range" , 300.0f , 50.0f , 500.0f ) );
    public Setting < Boolean > message = this.register ( new Setting <> ( "Message" , false ) );

    public
    CombatLogger( ) {
        super ( "CombatLogger" , "Will Expose pussies" , Module.Category.RENDER , true , false , false );
    }

    @Override
    public
    void onLogout ( ) {
        this.spots.clear ( );
    }

    @Override
    public
    void onDisable ( ) {
        this.spots.clear ( );
    }

    @Override
    public
    void onRender3D ( Render3DEvent event ) {
        if ( ! this.spots.isEmpty ( ) ) {
            synchronized (this.spots) {
                this.spots.forEach ( spot -> {
                    if ( spot.getEntity ( ) != null ) {
                        AxisAlignedBB bb = RenderUtil.interpolateAxis ( spot.getEntity ( ).getEntityBoundingBox ( ) );
                        RenderUtil.drawBlockOutline ( bb , this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor ( ) : new Color ( this.red.getValue ( ) , this.green.getValue ( ) , this.blue.getValue ( ) , this.alpha.getValue ( ) ) , 1.0f );
                        double x = this.interpolate ( spot.getEntity ( ).lastTickPosX , spot.getEntity ( ).posX , event.getPartialTicks ( ) ) - CombatLogger.mc.getRenderManager ( ).renderPosX;
                        double y = this.interpolate ( spot.getEntity ( ).lastTickPosY , spot.getEntity ( ).posY , event.getPartialTicks ( ) ) - CombatLogger.mc.getRenderManager ( ).renderPosY;
                        double z = this.interpolate ( spot.getEntity ( ).lastTickPosZ , spot.getEntity ( ).posZ , event.getPartialTicks ( ) ) - CombatLogger.mc.getRenderManager ( ).renderPosZ;
                        this.renderNameTag ( spot.getName ( ) , x , y , z , event.getPartialTicks ( ) , spot.getX ( ) , spot.getY ( ) , spot.getZ ( ) );
                    }
                } );
            }
        }
    }

    @Override
    public
    void onUpdate ( ) {
        if ( ! CombatLogger.fullNullCheck ( ) ) {
            this.spots.removeIf ( spot -> CombatLogger.mc.player.getDistanceSq ( spot.getEntity ( ) ) >= MathUtil.square ( this.range.getValue ( ) ) );
        }
    }

    @SubscribeEvent
    public
    void onConnection ( ConnectionEvent event ) {
        if ( event.getStage ( ) == 0 ) {
            UUID uuid = event.getUuid ( );
            EntityPlayer entity = CombatLogger.mc.world.getPlayerEntityByUUID ( uuid );
            if ( entity != null && this.message.getValue ( ) ) {
                Command3.sendMessage ( "\\u00A75" + entity.getName ( ) + " just logged in" + ( this.coords.getValue ( ) ? " at (" + (int) entity.posX + ", " + (int) entity.posY + ", " + (int) entity.posZ + ")!" : "!" ) , this.notification.getValue ( ) );
            }
            this.spots.removeIf ( pos -> pos.getName ( ).equalsIgnoreCase ( event.getName ( ) ) );
        } else if ( event.getStage ( ) == 1 ) {
            EntityPlayer entity = event.getEntity ( );
            UUID uuid = event.getUuid ( );
            String name = event.getName ( );
            if ( this.message.getValue ( ) ) {
                Command3.sendMessage ( "\\u00A7d" + event.getName ( ) + " just logged out" + ( this.coords.getValue ( ) ? " at (" + (int) entity.posX + ", " + (int) entity.posY + ", " + (int) entity.posZ + ")!" : "!" ) , this.notification.getValue ( ) );
            }
            if ( name != null && entity != null && uuid != null ) {
                this.spots.add ( new LogoutPos ( name , uuid , entity ) );
            }
        }
    }

    private
    void renderNameTag ( String name , double x , double yi , double z , float delta , double xPos , double yPos , double zPos ) {
        double y = yi + 0.7;
        Entity camera = mc.getRenderViewEntity ( );
        assert ( camera != null );
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = this.interpolate ( camera.prevPosX , camera.posX , delta );
        camera.posY = this.interpolate ( camera.prevPosY , camera.posY , delta );
        camera.posZ = this.interpolate ( camera.prevPosZ , camera.posZ , delta );
        String displayTag = name + " XYZ: " + (int) xPos + ", " + (int) yPos + ", " + (int) zPos;
        double distance = camera.getDistance ( x + CombatLogger.mc.getRenderManager ( ).viewerPosX , y + CombatLogger.mc.getRenderManager ( ).viewerPosY , z + CombatLogger.mc.getRenderManager ( ).viewerPosZ );
        int width = this.renderer.getStringWidth ( displayTag ) / 2;
        double scale = ( 0.0018 + (double) this.scaling.getValue ( ) * ( distance * (double) this.factor.getValue ( ) ) ) / 1000.0;
        if ( distance <= 8.0 && this.smartScale.getValue ( ) ) {
            scale = 0.0245;
        }
        if ( ! this.scaleing.getValue ( ) ) {
            scale = (double) this.scaling.getValue ( ) / 100.0;
        }
        GlStateManager.pushMatrix ( );
        RenderHelper.enableStandardItemLighting ( );
        GlStateManager.enablePolygonOffset ( );
        GlStateManager.doPolygonOffset ( 1.0f , - 1500000.0f );
        GlStateManager.disableLighting ( );
        GlStateManager.translate ( (float) x , (float) y + 1.4f , (float) z );
        GlStateManager.rotate ( - CombatLogger.mc.getRenderManager ( ).playerViewY , 0.0f , 1.0f , 0.0f );
        GlStateManager.rotate ( CombatLogger.mc.getRenderManager ( ).playerViewX , CombatLogger.mc.gameSettings.thirdPersonView == 2 ? - 1.0f : 1.0f , 0.0f , 0.0f );
        GlStateManager.scale ( - scale , - scale , scale );
        GlStateManager.disableDepth ( );
        GlStateManager.enableBlend ( );
        GlStateManager.enableBlend ( );
        if ( this.rect.getValue ( ) ) {
            RenderUtil.drawRect ( - width - 2 , - ( this.renderer.getFontHeight ( ) + 1 ) , (float) width + 2.0f , 1.5f , 0x55000000 );
        }
        GlStateManager.disableBlend ( );
        this.renderer.drawStringWithShadow ( displayTag , - width , - ( this.renderer.getFontHeight ( ) - 1 ) , this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColorHex ( ) : ColorUtil.toRGBA ( new Color ( this.red.getValue ( ) , this.green.getValue ( ) , this.blue.getValue ( ) , this.alpha.getValue ( ) ) ) );
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth ( );
        GlStateManager.disableBlend ( );
        GlStateManager.disablePolygonOffset ( );
        GlStateManager.doPolygonOffset ( 1.0f , 1500000.0f );
        GlStateManager.popMatrix ( );
    }

    private
    double interpolate ( double previous , double current , float delta ) {
        return previous + ( current - previous ) * (double) delta;
    }

    private static
    class LogoutPos {
        private final String name;
        private final UUID uuid;
        private final EntityPlayer entity;
        private final double x;
        private final double y;
        private final double z;

        public
        LogoutPos ( String name , UUID uuid , EntityPlayer entity ) {
            this.name = name;
            this.uuid = uuid;
            this.entity = entity;
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
        }

        public
        String getName ( ) {
            return this.name;
        }

        public
        UUID getUuid ( ) {
            return this.uuid;
        }

        public
        EntityPlayer getEntity ( ) {
            return this.entity;
        }

        public
        double getX ( ) {
            return this.x;
        }

        public
        double getY ( ) {
            return this.y;
        }

        public
        double getZ ( ) {
            return this.z;
        }
    }
}