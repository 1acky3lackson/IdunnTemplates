package com.jackyblackson.idunntemplates.core.util;

import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.weather.WeatherType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An Extent that captures setBlock calls into a Map.
 * Extends AbstractDelegateExtent to avoid "skip if NullExtent" optimizations.
 */
public class CapturingExtent extends AbstractDelegateExtent implements World {

    private final Map<BlockVector3, BlockState> blocks = new HashMap<>();

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private final UUID uuid = UUID.randomUUID();

    public CapturingExtent(int sizeX, int sizeY, int sizeZ) {
        super(new AbstractDelegateExtent(new NullExtent()));
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }



    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
        // We capture the BlockState.
//        System.out.printf("    -> Extent.setBlock, pos = %s, block = %s%n", position.toString(), block.toString());
        if (block instanceof BlockState) {
            blocks.put(position, (BlockState) block);
        } else if (block instanceof BaseBlock) {
            blocks.put(position, ((BaseBlock) block).toImmutableState());
        }
        return true;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        // We capture the BlockState.
//        System.out.printf("    -> world.setBlock, pos = %s, block = %s%n", position.toString(), block.toString());
        if (block instanceof BlockState) {
            blocks.put(position, (BlockState) block);
        } else if (block instanceof BaseBlock) {
            blocks.put(position, ((BaseBlock) block).toImmutableState());
        }
        return true;
    }

    public BlockState getBlock(BlockVector3 position) {
        return blocks.getOrDefault(position, null);
    }

    public Map<BlockVector3, BlockState> getCapturedBlocks() {
        return blocks;
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        // Respecting WorldEdit's bit-packing limits (26-bit X/Z, 12-bit Y)
        return BlockVector3.at(-sizeX, -sizeY, -sizeZ);
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        // Respecting WorldEdit's bit-packing limits (26-bit X/Z, 12-bit Y)
        return BlockVector3.at(sizeX, sizeY, sizeZ);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        System.out.println("GET FULL BLOCK !!!!!!!!!!!!!!!!!!!!!!!!");
        return super.getFullBlock(position);
    }

    @Override
    protected Operation commitBefore() {
        System.out.println("GET COMMIT BEFORE !!!!!!!!!!!!!!!!!!!!!!!!");
        return super.commitBefore();
    }

    @Override
    public String getName() {
        return "virtual_world_" + uuid;
    }

    @Override
    public String getNameUnsafe() {
        return "virtual_world_" + uuid;
    }

    @Nullable
    @Override
    public Path getStoragePath() {
        return Path.of("/");
    }

    @Override
    public Mask createLiquidMask() {
        return new Mask() {
            @Override
            public boolean test(BlockVector3 vector) {
                return false;
            }

            @Override
            public Mask copy() {
                return this;
            }
        };
    }

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        return false;
    }



    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
        return Set.of();
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        return false;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item, int count) {

    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {

    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {

    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        return true;
    }

    @Override
    public void checkLoadedChunk(BlockVector3 position) {

    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {

    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {

    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        return true;
    }

    @Override
    public boolean playBlockBreakEffect(Vector3 position, BlockType type) {
        return true;
    }

    @Override
    public boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority) {
        return true;
    }

    @Override
    public WeatherType getWeather() {
        return WeatherType.REGISTRY.get("clear");
    }

    @Override
    public long getRemainingWeatherDuration() {
        return 0;
    }

    @Override
    public void setWeather(WeatherType weatherType) {

    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {

    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return BlockVector3.at(0, 0, 0);
    }

    @Override
    public void refreshChunk(int chunkX, int chunkZ) {

    }

    @Override
    public IChunkGet get(int x, int z) {
        return null;
    }

    @Override
    public void sendFakeChunk(@Nullable Player player, ChunkPacket packet) {

    }

    @Override
    public void flush() {

    }
}