package io.github.lukegrahamlandry.tribes.tile;

import io.github.lukegrahamlandry.tribes.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AltarBlockEntity extends BlockEntity {
    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(TileEntityInit.ALTAR.get(), pos, state);
    }

    private ResourceLocation bannerKey;

    @Override
    public void setChanged() {
        super.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    // use from block
    public void setBannerKey(ResourceLocation key) {
        this.bannerKey = key;
        this.setChanged();
    }

    // for render
    public ResourceLocation getBannerKey() {
        return this.bannerKey;
    }

    // saving data
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.bannerKey = tag.contains("banner") ? new ResourceLocation(tag.getString("banner")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (this.bannerKey != null) pTag.putString("banner", this.bannerKey.toString());
        else {
            pTag.remove("banner");
        }
    }


    // block update
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);

        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    // chunk load
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
