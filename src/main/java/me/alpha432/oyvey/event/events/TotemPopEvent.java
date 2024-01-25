package me.alpha432.oyvey.event.events;

import net.minecraft.entity.player.EntityPlayer;
import me.alpha432.oyvey.event.EventStage;

public class TotemPopEvent extends EventStage
{
    private EntityPlayer entity;

    public TotemPopEvent(final EntityPlayer entity) {
        this.entity = entity;
    }

    public EntityPlayer getEntity() {
        return this.entity;
    }
}
