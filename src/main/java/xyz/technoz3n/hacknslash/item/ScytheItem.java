package xyz.technoz3n.hacknslash.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import xyz.technoz3n.hacknslash.HackNSlash;
import xyz.technoz3n.hacknslash.enchantment.SoulPullEnchantment;

import java.util.Optional;

public class ScytheItem extends SwordItem {

    public ScytheItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return getTier().getSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.AXE_STRIP || super.canPerformAction(stack, toolAction);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos centerPos = context.getClickedPos();
        BlockState centerState = level.getBlockState(centerPos);

        if (centerState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(centerState)) {
            if (!level.isClientSide()) {
                for (BlockPos pos : BlockPos.betweenClosed(
                        centerPos.offset(-1, 0, -1), centerPos.offset(1, 0, 1))) {
                    BlockPos immutablePos = pos.immutable();
                    BlockState state = level.getBlockState(immutablePos);
                    if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                        Block.dropResources(state, level, immutablePos, null,
                                context.getPlayer(), context.getItemInHand());
                        level.setBlock(immutablePos, crop.getStateForAge(0), 2);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockState stripped = centerState.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
        if (stripped != null) {
            Player player = context.getPlayer();
            level.playSound(player, centerPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(centerPos, stripped, 11);
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        attacker.level().playSound(null, attacker.blockPosition(),
                HackNSlash.SCYTHE_SLASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        int pullLevel = stack.getEnchantmentLevel(HackNSlash.SOUL_PULL.get());
        if (pullLevel > 0 && attacker.level() instanceof ServerLevel serverLevel) {
            SoulPullEnchantment.performPull(serverLevel, attacker, target.blockPosition(), pullLevel);
        }

        return result;
    }
}