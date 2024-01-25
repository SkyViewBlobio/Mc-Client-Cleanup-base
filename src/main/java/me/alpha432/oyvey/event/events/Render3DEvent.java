package me.alpha432.oyvey.event.events;

import me.alpha432.oyvey.event.EventStage;

public class Render3DEvent
        extends EventStage {
    private float partialTicks;

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
    public void setPartialTicks(final float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
