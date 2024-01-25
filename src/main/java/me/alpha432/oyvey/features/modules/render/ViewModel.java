package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.RenderItemEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public
class ViewModel extends Module {
    private static ViewModel INSTANCE = new ViewModel ( );
    public Setting < Settings > settings = this.register ( new Setting <> ( "settings" , Settings.TRANSLATE ) );
    public Setting < Boolean > noEatAnimation = this.register ( new Setting <> ( "noeatanimation" , false , v -> settings.getValue ( ) == Settings.TWEAKS ) );
    public Setting < Double > eatX = this.register ( new Setting <> ( "eatx" , 1.0 , - 2.0 , 5.0 , v -> settings.getValue ( ) == Settings.TWEAKS && ! this.noEatAnimation.getValue ( ) ) );
    public Setting < Double > eatY = this.register ( new Setting <> ( "eaty" , 1.0 , - 2.0 , 5.0 , v -> settings.getValue ( ) == Settings.TWEAKS && ! this.noEatAnimation.getValue ( ) ) );
    public Setting< Boolean > doBob = this.register ( new Setting <> ( "itembob" , true , v -> settings.getValue ( ) == Settings.TWEAKS ) );
    public Setting < Double > mainX = this.register ( new Setting <> ( "mainx" , 1.2 , - 2.0 , 4.0 , v -> settings.getValue ( ) == Settings.TRANSLATE ) );
    public Setting < Double > mainY = this.register ( new Setting <> ( "mainy" , - 0.95 , - 3.0 , 3.0 , v -> settings.getValue ( ) == Settings.TRANSLATE ) );
    public Setting < Double > mainZ = this.register ( new Setting <> ( "mainz" , - 1.45 , - 5.0 , 5.0 , v -> settings.getValue ( ) == Settings.TRANSLATE ) );
    public Setting < Double > offX = this.register ( new Setting <> ( "offx" , 1.2 , - 2.0 , 4.0 , v -> settings.getValue ( ) == Settings.TRANSLATE ) );
    public Setting < Double > offY = this.register ( new Setting <> ( "offy" , - 0.95 , - 3.0 , 3.0 , v -> settings.getValue ( ) == Settings.TRANSLATE ) );
    public Setting < Double > offZ = this.register ( new Setting <> ( "offz" , - 1.45 , - 5.0 , 5.0 , v -> settings.getValue ( ) == Settings.TRANSLATE ) );
    public Setting < Integer > mainRotX = this.register ( new Setting <> ( "mainrotationx" , 0 , - 36 , 36 , v -> settings.getValue ( ) == Settings.ROTATE ) );
    public Setting < Integer > mainRotY = this.register ( new Setting <> ( "mainrotationy" , 0 , - 36 , 36 , v -> settings.getValue ( ) == Settings.ROTATE ) );
    public Setting < Integer > mainRotZ = this.register ( new Setting <> ( "mainrotationz" , 0 , - 36 , 36 , v -> settings.getValue ( ) == Settings.ROTATE ) );
    public Setting < Integer > offRotX = this.register ( new Setting <> ( "offrotationx" , 0 , - 36 , 36 , v -> settings.getValue ( ) == Settings.ROTATE ) );
    public Setting < Integer > offRotY = this.register ( new Setting <> ( "offrotationy" , 0 , - 36 , 36 , v -> settings.getValue ( ) == Settings.ROTATE ) );
    public Setting < Integer > offRotZ = this.register ( new Setting <> ( "offrotationz" , 0 , - 36 , 36 , v -> settings.getValue ( ) == Settings.ROTATE ) );
    public Setting < Double > mainScaleX = this.register ( new Setting <> ( "mainscalex" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    public Setting < Double > mainScaleY = this.register ( new Setting <> ( "mainscaley" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    public Setting < Double > mainScaleZ = this.register ( new Setting <> ( "mainscalez" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    //public Setting < Double > mainItemWidth = this.register ( new Setting <> ( "MainItemWidth" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    public Setting < Double > offScaleX = this.register ( new Setting <> ( "offscalex" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    public Setting < Double > offScaleY = this.register ( new Setting <> ( "offscaley" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    public Setting < Double > offScaleZ = this.register ( new Setting <> ( "offscalez" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );
    //public Setting < Double > offItemWidth = this.register ( new Setting <> ( "OffItemWidth" , 1.0 , 0.1 , 5.0 , v -> settings.getValue ( ) == Settings.SCALE ) );

    public
    ViewModel ( ) {
        super ( "Viewmodel" , "X+" , Category.RENDER , true , false , false );
        this.setInstance ( );
    }

    public static
    ViewModel getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new ViewModel ( );
        }
        return INSTANCE;
    }

    private
    void setInstance ( ) {
        INSTANCE = this;
    }

    @SubscribeEvent
    public
    void onItemRender ( RenderItemEvent event ) {
        event.setMainX ( mainX.getValue ( ) );
        event.setMainY ( mainY.getValue ( ) );
        event.setMainZ ( mainZ.getValue ( ) );

        event.setOffX ( - offX.getValue ( ) );
        event.setOffY ( offY.getValue ( ) );
        event.setOffZ ( offZ.getValue ( ) );

        event.setMainRotX ( mainRotX.getValue ( ) * 5 );
        event.setMainRotY ( mainRotY.getValue ( ) * 5 );
        event.setMainRotZ ( mainRotZ.getValue ( ) * 5 );

        event.setOffRotX ( offRotX.getValue ( ) * 5 );
        event.setOffRotY ( offRotY.getValue ( ) * 5 );
        event.setOffRotZ ( offRotZ.getValue ( ) * 5 );

        event.setOffHandScaleX ( offScaleX.getValue ( ) );
        event.setOffHandScaleY ( offScaleY.getValue ( ) );
        event.setOffHandScaleZ ( offScaleZ.getValue ( ) );

        event.setMainHandScaleX ( mainScaleX.getValue ( ) );
        event.setMainHandScaleY ( mainScaleY.getValue ( ) );
        event.setMainHandScaleZ ( mainScaleZ.getValue ( ) );

        //event.setMainHandItemWidth ( mainItemWidth.getValue ( ) );
        //event.setOffHandItemWidth ( offItemWidth.getValue ( ) );
    }

    private
    enum Settings {
        TRANSLATE,
        ROTATE,
        SCALE,
        TWEAKS
    }
}