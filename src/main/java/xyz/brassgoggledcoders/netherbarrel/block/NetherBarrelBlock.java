package xyz.brassgoggledcoders.netherbarrel.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.netherbarrel.blockentity.NetherBarrelBlockEntity;
import xyz.brassgoggledcoders.netherbarrel.capability.DeepItemHandler;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelBlocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class NetherBarrelBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public NetherBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(OPEN, false)
        );
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof NetherBarrelBlockEntity) {
                pPlayer.openMenu((NetherBarrelBlockEntity) blockentity);
                pPlayer.awardStat(Stats.OPEN_BARREL);
                PiglinAi.angerNearbyPiglins(pPlayer, true);
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            pLevel.getBlockEntity(pPos, NetherBarrelBlocks.NETHER_BARREL_ENTITY.get())
                    .map(NetherBarrelBlockEntity::getItemHandler)
                    .ifPresent(itemHandler -> {
                        Containers.dropContents(pLevel, pPos, itemHandler.getStacks());
                        pLevel.updateNeighbourForOutputSignal(pPos, this);
                    });

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof NetherBarrelBlockEntity netherBarrelBlockEntity) {
            netherBarrelBlockEntity.recheckOpen();
        }
    }


    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new NetherBarrelBlockEntity(pPos, pState);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.hasCustomHoverName()) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof NetherBarrelBlockEntity netherBarrelBlock) {
                netherBarrelBlock.setCustomName(pStack.getHoverName());
            }
        }

    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos, NetherBarrelBlocks.NETHER_BARREL_ENTITY.get())
                .map(NetherBarrelBlockEntity::getItemHandler)
                .map(DeepItemHandler::calculateComparator)
                .orElse(0);
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, OPEN);
    }

    @Override
    @NotNull
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getNearestLookingDirection()
                        .getOpposite()
                );
    }
}
