package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.MoveEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.UpdateWalkingPlayerEvent;
import me.alpha432.oyvey.features.modules.player.Freecam;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtilPhob;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import me.alpha432.oyvey.features.modules.Module;

public class StrafePhob extends Module {
    private static StrafePhob INSTANCE;
    private final Setting < Mode > mode = this.register ( new Setting <> ( "Modes" , Mode.NCP ) );
    private final Setting < Boolean > limiter = this.register ( new Setting <> ( "GRound" , true ) );
    private final Setting < Boolean > bhop2 = this.register ( new Setting <> ( "Hop" , true ) );
    private final Setting < Boolean > limiter2 = this.register ( new Setting <> ( "Bhop" , false ) );
    private final Setting < Boolean > noLag = this.register ( new Setting <> ( "AntiIusse" , false ) );
    private final Setting < Integer > specialMoveSpeed = this.register ( new Setting <> ( "Speed" , 100 , 0 , 150 ) );
    private final Setting < Integer > potionSpeed = this.register ( new Setting <> ( "Speed1" , 130 , 0 , 150 ) );
    private final Setting < Integer > potionSpeed2 = this.register ( new Setting <> ( "Speed2" , 125 , 0 , 150 ) );
    private final Setting < Integer > dFactor = this.register ( new Setting <> ( "DFactor" , 159 , 100 , 200 ) );
    private final Setting < Integer > acceleration = this.register ( new Setting <> ( "acceleration[Increased]" , 2149 , 1000 , 2500 ) );
    private final Setting < Float > speedLimit = this.register ( new Setting <> ( "Limit" , 35.0f , 20.0f , 60.0f ) );
    private final Setting < Float > speedLimit2 = this.register ( new Setting <> ( "Limit2" , 60.0f , 20.0f , 60.0f ) );
    private final Setting < Integer > yOffset = this.register ( new Setting <> ( "YlvlCheck" , 400 , 350 , 500 ) );
    private final Setting < Boolean > potion = this.register ( new Setting <> ( "Potion" , false ) );
    private final Setting < Boolean > wait = this.register ( new Setting <> ( "Wait" , true ) );
    private final Setting < Boolean > hopWait = this.register ( new Setting <> ( "HopWait" , true ) );
    private final Setting < Integer > startStage = this.register ( new Setting <> ( "Stage" , 2 , 0 , 4 ) );
    private final Setting < Boolean > setPos = this.register ( new Setting <> ( "SetPos" , true ) );
    private final Setting < Boolean > setNull = this.register ( new Setting <> ( "SetNull" , false ) );
    private final Setting < Integer > setGroundLimit = this.register ( new Setting <> ( "GroundLimit" , 138 , 0 , 1000 ) );
    private final Setting < Integer > groundFactor = this.register ( new Setting <> ( "GroundFactor" , 13 , 0 , 50 ) );
    private final Setting < Integer > step = this.register ( new Setting < Object > ( "SetStep" , 1 , 0 , 2 , v -> this.mode.getValue ( ) == Mode.BHOP ) );
    private final Setting < Boolean > setGroundNoLag = this.register ( new Setting <> ( "NoGroundLag" , true ) );
    private final Timer timer = new Timer();
    private int stage = 1;
    private double moveSpeed;
    private double lastDist;
    private int cooldownHops;
    private boolean waitForGround;
    private int hops;

    public StrafePhob ( ) {
        super ( "Strafe" , "Strafe like a black guy" , Module.Category.MOVEMENT , true , false , false );
        INSTANCE = this;
    }

    public static
    StrafePhob getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new StrafePhob ( );
        }
        return INSTANCE;
    }

    public static
    double getBaseMoveSpeed ( ) {
        double baseSpeed = 0.272;
        if ( StrafePhob.mc.player.isPotionActive ( MobEffects.SPEED ) ) {
            int amplifier = Objects.requireNonNull ( StrafePhob.mc.player.getActivePotionEffect ( MobEffects.SPEED ) ).getAmplifier ( );
            baseSpeed *= 1.0 + 0.2 * (double) amplifier;
        }
        return baseSpeed;
    }

    public static
    double round ( double value , int places ) {
        if ( places < 0 ) {
            throw new IllegalArgumentException ( );
        }
        BigDecimal bigDecimal = new BigDecimal ( value ).setScale ( places , RoundingMode.HALF_UP );
        return bigDecimal.doubleValue ( );
    }

    @Override
    public
    void onEnable ( ) {
        if ( ! StrafePhob.mc.player.onGround ) {
            this.waitForGround = true;
        }
        this.hops = 0;
        this.timer.reset ( );
        this.moveSpeed = StrafePhob.getBaseMoveSpeed ( );
    }

    @Override
    public
    void onDisable ( ) {
        this.hops = 0;
        this.moveSpeed = 0.0;
        this.stage = this.startStage.getValue ( );
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        if ( event.getStage ( ) == 0 ) {
            this.lastDist = Math.sqrt ( ( StrafePhob.mc.player.posX - StrafePhob.mc.player.prevPosX ) * ( StrafePhob.mc.player.posX - StrafePhob.mc.player.prevPosX ) + ( StrafePhob.mc.player.posZ - StrafePhob.mc.player.prevPosZ ) * ( StrafePhob.mc.player.posZ - StrafePhob.mc.player.prevPosZ ) );
        }
    }

    @SubscribeEvent
    public void onMove ( MoveEvent event ) {
        if ( event.getStage ( ) != 0 || this.shouldReturn ( ) ) {
            return;
        }
        if ( ! StrafePhob.mc.player.onGround ) {
            if ( this.wait.getValue ( ) && this.waitForGround ) {
                return;
            }
        } else {
            this.waitForGround = false;
        }
        if ( this.mode.getValue ( ) == Mode.NCP ) {
            this.doNCP ( event );
        } else if ( this.mode.getValue ( ) == Mode.BHOP ) {
            float moveForward = StrafePhob.mc.player.movementInput.moveForward;
            float moveStrafePhob = StrafePhob.mc.player.movementInput.moveStrafe;
            float rotationYaw = StrafePhob.mc.player.rotationYaw;
            if ( this.step.getValue ( ) == 1 ) {
                StrafePhob.mc.player.stepHeight = 0.6f;
            }
            if ( this.limiter2.getValue ( ) && StrafePhob.mc.player.onGround && OyVey.speedManager.getSpeedKpH ( ) < (double) this.speedLimit2.getValue ( ) ) {
                this.stage = 2;
            }
            if ( this.limiter.getValue ( ) && StrafePhob.round ( StrafePhob.mc.player.posY - (double) ( (int) StrafePhob.mc.player.posY ) , 3 ) == StrafePhob.round ( (double) this.setGroundLimit.getValue ( ) / 1000.0 , 3 ) && ( ! this.setGroundNoLag.getValue ( ) || EntityUtilPhob.isEntityMoving ( StrafePhob.mc.player ) ) ) {
                if ( this.setNull.getValue ( ) ) {
                    StrafePhob.mc.player.motionY = 0.0;
                } else {
                    StrafePhob.mc.player.motionY -= (double) this.groundFactor.getValue ( ) / 100.0;
                    event.setY ( event.getY ( ) - (double) this.groundFactor.getValue ( ) / 100.0 );
                    if ( this.setPos.getValue ( ) ) {
                        StrafePhob.mc.player.posY -= (double) this.groundFactor.getValue ( ) / 100.0;
                    }
                }
            }
            if ( this.stage == 1 && EntityUtilPhob.isMoving ( ) ) {
                this.stage = 2;
                this.moveSpeed = (double) this.getMultiplier ( ) * StrafePhob.getBaseMoveSpeed ( ) - 0.01;
            } else if ( this.stage == 2 && EntityUtilPhob.isMoving ( ) ) {
                this.stage = 3;
                StrafePhob.mc.player.motionY = (double) this.yOffset.getValue ( ) / 1000.0;
                event.setY ( (double) this.yOffset.getValue ( ) / 1000.0 );
                if ( this.cooldownHops > 0 ) {
                    -- this.cooldownHops;
                }
                ++ this.hops;
                double accel = this.acceleration.getValue ( ) == 2149 ? 2.149802 : (double) this.acceleration.getValue ( ) / 1000.0;
                this.moveSpeed *= accel;
            } else if ( this.stage == 3 ) {
                this.stage = 4;
                double difference = 0.66 * ( this.lastDist - StrafePhob.getBaseMoveSpeed ( ) );
                this.moveSpeed = this.lastDist - difference;
            } else {
                if ( StrafePhob.mc.world.getCollisionBoxes ( StrafePhob.mc.player , StrafePhob.mc.player.getEntityBoundingBox ( ).offset ( 0.0 , StrafePhob.mc.player.motionY , 0.0 ) ).size ( ) > 0 || StrafePhob.mc.player.collidedVertically && this.stage > 0 ) {
                    this.stage = this.bhop2.getValue ( ) && OyVey.speedManager.getSpeedKpH ( ) >= (double) this.speedLimit.getValue ( ) ? 0 : ( StrafePhob.mc.player.moveForward != 0.0f || StrafePhob.mc.player.moveStrafing != 0.0f ? 1 : 0 );
                }
                this.moveSpeed = this.lastDist - this.lastDist / (double) this.dFactor.getValue ( );
            }
            this.moveSpeed = Math.max ( this.moveSpeed , StrafePhob.getBaseMoveSpeed ( ) );
            if ( this.hopWait.getValue ( ) && this.limiter2.getValue ( ) && this.hops < 2 ) {
                this.moveSpeed = EntityUtilPhob.getMaxSpeed ( );
            }
            if ( moveForward == 0.0f && moveStrafePhob == 0.0f ) {
                event.setX ( 0.0 );
                event.setZ ( 0.0 );
                this.moveSpeed = 0.0;
            } else if ( moveForward != 0.0f ) {
                if ( moveStrafePhob >= 1.0f ) {
                    rotationYaw += moveForward > 0.0f ? - 45.0f : 45.0f;
                    moveStrafePhob = 0.0f;
                } else if ( moveStrafePhob <= - 1.0f ) {
                    rotationYaw += moveForward > 0.0f ? 45.0f : - 45.0f;
                    moveStrafePhob = 0.0f;
                }
                if ( moveForward > 0.0f ) {
                    moveForward = 1.0f;
                } else if ( moveForward < 0.0f ) {
                    moveForward = - 1.0f;
                }
            }
            double motionX = Math.cos ( Math.toRadians ( rotationYaw + 90.0f ) );
            double motionZ = Math.sin ( Math.toRadians ( rotationYaw + 90.0f ) );
            if ( this.cooldownHops == 0 ) {
                event.setX ( (double) moveForward * this.moveSpeed * motionX + (double) moveStrafePhob * this.moveSpeed * motionZ );
                event.setZ ( (double) moveForward * this.moveSpeed * motionZ - (double) moveStrafePhob * this.moveSpeed * motionX );
            }
            if ( this.step.getValue ( ) == 2 ) {
                StrafePhob.mc.player.stepHeight = 0.6f;
            }
            if ( moveForward == 0.0f && moveStrafePhob == 0.0f ) {
                this.timer.reset ( );
                event.setX ( 0.0 );
                event.setZ ( 0.0 );
            }
        }
    }

    private
    void doNCP ( MoveEvent event ) {
        if ( ! this.limiter.getValue ( ) && StrafePhob.mc.player.onGround ) {
            this.stage = 2;
        }
        switch (this.stage) {
            case 0: {
                ++ this.stage;
                this.lastDist = 0.0;
                break;
            }
            case 2: {
                double motionY = 0.40123128;
                if ( StrafePhob.mc.player.moveForward == 0.0f && StrafePhob.mc.player.moveStrafing == 0.0f || ! StrafePhob.mc.player.onGround )
                    break;
                if ( StrafePhob.mc.player.isPotionActive ( MobEffects.JUMP_BOOST ) ) {
                    motionY += (float) ( Objects.requireNonNull ( StrafePhob.mc.player.getActivePotionEffect ( MobEffects.JUMP_BOOST ) ).getAmplifier ( ) + 1 ) * 0.1f;
                }
                StrafePhob.mc.player.motionY = motionY;
                event.setY ( StrafePhob.mc.player.motionY );
                this.moveSpeed *= 2.149;
                break;
            }
            case 3: {
                this.moveSpeed = this.lastDist - 0.76 * ( this.lastDist - StrafePhob.getBaseMoveSpeed ( ) );
                break;
            }
            default: {
                if ( StrafePhob.mc.world.getCollisionBoxes ( StrafePhob.mc.player , StrafePhob.mc.player.getEntityBoundingBox ( ).offset ( 0.0 , StrafePhob.mc.player.motionY , 0.0 ) ).size ( ) > 0 || StrafePhob.mc.player.collidedVertically && this.stage > 0 ) {
                    this.stage = this.bhop2.getValue ( ) && OyVey.speedManager.getSpeedKpH ( ) >= (double) this.speedLimit.getValue ( ) ? 0 : ( StrafePhob.mc.player.moveForward != 0.0f || StrafePhob.mc.player.moveStrafing != 0.0f ? 1 : 0 );
                }
                this.moveSpeed = this.lastDist - this.lastDist / 159.0;
            }
        }
        this.moveSpeed = Math.max ( this.moveSpeed , StrafePhob.getBaseMoveSpeed ( ) );
        double forward = StrafePhob.mc.player.movementInput.moveForward;
        double strafe = StrafePhob.mc.player.movementInput.moveStrafe;
        double yaw = StrafePhob.mc.player.rotationYaw;
        if ( forward == 0.0 && strafe == 0.0 ) {
            event.setX ( 0.0 );
            event.setZ ( 0.0 );
        } else if ( forward != 0.0 && strafe != 0.0 ) {
            forward *= Math.sin ( 0.7853981633974483 );
            strafe *= Math.cos ( 0.7853981633974483 );
        }
        event.setX ( ( forward * this.moveSpeed * - Math.sin ( Math.toRadians ( yaw ) ) + strafe * this.moveSpeed * Math.cos ( Math.toRadians ( yaw ) ) ) * 0.99 );
        event.setZ ( ( forward * this.moveSpeed * Math.cos ( Math.toRadians ( yaw ) ) - strafe * this.moveSpeed * - Math.sin ( Math.toRadians ( yaw ) ) ) * 0.99 );
        ++ this.stage;
    }

    private
    float getMultiplier ( ) {
        float baseSpeed = this.specialMoveSpeed.getValue ( );
        if ( this.potion.getValue ( ) && StrafePhob.mc.player.isPotionActive ( MobEffects.SPEED ) ) {
            int amplifier = Objects.requireNonNull ( StrafePhob.mc.player.getActivePotionEffect ( MobEffects.SPEED ) ).getAmplifier ( ) + 1;
            baseSpeed = amplifier >= 2 ? (float) this.potionSpeed2.getValue ( ) : (float) this.potionSpeed.getValue ( );
        }
        return baseSpeed / 100.0f;
    }

    private
    boolean shouldReturn ( ) {
        return OyVey.moduleManager.isModuleEnabled (String.valueOf(Freecam.class)) || OyVey.moduleManager.isModuleEnabled (String.valueOf(Phase.class)) || OyVey.moduleManager.isModuleEnabled (String.valueOf(ElytraFlightPhob.class)) || OyVey.moduleManager.isModuleEnabled (String.valueOf(Flight.class));
    }

    @SubscribeEvent
    public void onPacketReceive ( PacketEvent.Receive event ) {
        if ( event.getPacket ( ) instanceof SPacketPlayerPosLook && this.noLag.getValue ( ) ) {
            this.stage = this.mode.getValue ( ) == Mode.BHOP && ( this.limiter2.getValue ( ) || this.bhop2.getValue ( ) ) ? 1 : 4;
        }
    }

    @Override
    public
    String getDisplayInfo ( ) {
        if ( this.mode.getValue ( ) != Mode.NONE ) {
            if ( this.mode.getValue ( ) == Mode.NCP ) {
                return this.mode.currentEnumName ( ).toUpperCase ( );
            }
            return this.mode.currentEnumName ( );
        }
        return null;
    }

    public
    enum Mode {
        NONE,
        NCP,
        BHOP

    }
}
