package io.github.aquerr.worldrebuilder.storage.serializer;

import io.github.aquerr.worldrebuilder.strategy.RebuildBlocksStrategy;
import io.github.aquerr.worldrebuilder.strategy.WRBlockState;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;

public class WRTypeTokens
{
    public static final TypeToken<BlockSnapshot> BLOCK_SNAPSHOT_TYPE_TOKEN = TypeToken.get(BlockSnapshot.class);
    public static final TypeToken<Collection<BlockSnapshot>> BLOCK_SNAPSHOT_COLLECTION_TYPE_TOKEN = new TypeToken<Collection<BlockSnapshot>>() {};
    public static final TypeToken<List<EntitySnapshot>> ENTITY_SNAPSHOT_LIST_TYPE_TOKEN = new TypeToken<List<EntitySnapshot>>() {};
    public static final TypeToken<Vector3i> VECTOR3I_TYPE_TOKEN = TypeToken.get(Vector3i.class);
    public static final TypeToken<RebuildBlocksStrategy> BLOCK_REBUILD_STRATEGY = TypeToken.get(RebuildBlocksStrategy.class);
    public static final TypeToken<WRBlockState> WR_BLOCK_STATE_TYPE_TOKEN = TypeToken.get(WRBlockState.class);
    public static final TypeToken<List<WRBlockState>> WR_BLOCK_STATE_LIST_TYPE_TOKEN = new TypeToken<List<WRBlockState>>() {};
}
