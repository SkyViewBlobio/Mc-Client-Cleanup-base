package me.alpha432.oyvey.util;

import javax.annotation.Nullable;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class Song {
    public static final ISound sound;
    private static final ResourceLocation song;

    static {
        song = new ResourceLocation("textures/song.ogg");
        sound = new ISound(){
            private final int pitch = 1;
            private final int volume = 1;

            public ResourceLocation getSoundLocation() {
                return song;
            }

            @Nullable
            public SoundEventAccessor createAccessor(SoundHandler soundHandler) {
                return new SoundEventAccessor(song, "awesome");
            }

            public Sound getSound() {
                return new Sound("song", 1.0f, 1.0f, 1, Sound.Type.SOUND_EVENT, false);
            }

            public SoundCategory getCategory() {
                return SoundCategory.VOICE;
            }

            public boolean canRepeat() {
                return true;
            }

            public int getRepeatDelay() {
                return 2;
            }

            public float getVolume() {
                return 1.0f;
            }

            public float getPitch() {
                return 1.0f;
            }

            public float getXPosF() {
                return 1.0f;
            }

            public float getYPosF() {
                return 0.0f;
            }

            public float getZPosF() {
                return 0.0f;
            }

            public ISound.AttenuationType getAttenuationType() {
                return ISound.AttenuationType.LINEAR;
            }
        };
    }
}