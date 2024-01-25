package me.alpha432.oyvey.event.events;

import me.alpha432.oyvey.event.EventStage;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class RenderItemInFirstPersonEvent extends EventStage {
        /*private final RenderItem renderItem;
    private final ItemStack stack;
    private final EntityLivingBase entity;
    private final ItemCameraTransforms.TransformType transformType;
    private final boolean leftHanded;*/

    private final EntityLivingBase entity;
    private ItemStack stack;
    private ItemCameraTransforms.TransformType transformType;
    private final boolean leftHanded;

    /*public RenderItemInFirstPersonEvent(RenderItem renderItem, ItemStack stack, EntityLivingBase entity, ItemCameraTransforms.TransformType transformType, boolean leftHanded, Stage stage)
    {
        super(stage);
        this.renderItem = renderItem;
        this.stack = stack;
        this.entity = entity;
        this.transformType = transformType;
        this.leftHanded = leftHanded;
    }*/

    public RenderItemInFirstPersonEvent(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded, int stage) {
        super(stage);
        this.entity = entitylivingbaseIn;
        this.stack = heldStack;
        this.transformType = transform;
        this.leftHanded = leftHanded;
    }

    public ItemCameraTransforms.TransformType getTransformType() {
        return transformType;
    }

    public void setTransformType(ItemCameraTransforms.TransformType transformType) {
        this.transformType = transformType;
    }

    public boolean isLeftHanded() {
        return leftHanded;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }
}
