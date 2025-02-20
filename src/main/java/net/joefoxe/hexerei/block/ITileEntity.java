package net.joefoxe.hexerei.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ITileEntity<T extends BlockEntity> {

    Class<T> getTileEntityClass();

    default void sync(){}

    default void withTileEntityDo(BlockGetter world, BlockPos pos, Consumer<T> action) {
        getTileEntityOptional(world, pos).ifPresent(action);
    }

    default InteractionResult onTileEntityUse(BlockGetter world, BlockPos pos, Function<T, InteractionResult> action) {
        return getTileEntityOptional(world, pos).map(action)
                .orElse(InteractionResult.PASS);
    }

    default Optional<T> getTileEntityOptional(BlockGetter world, BlockPos pos) {
        return Optional.ofNullable(getBlockEntity(world, pos));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default T getBlockEntity(BlockGetter worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        Class<T> expectedClass = getTileEntityClass();

        if (tileEntity == null)
            return null;
        if (!expectedClass.isInstance(tileEntity))
            return null;

        return (T) tileEntity;
    }

}