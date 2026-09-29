    package xyz.technoz3n.hacknslash.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.technoz3n.hacknslash.entity.BulletProjectile;

public class BulletProjectileRenderer extends ThrownItemRenderer<BulletProjectile> {
    public BulletProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, 0.5F, true);
    }
}