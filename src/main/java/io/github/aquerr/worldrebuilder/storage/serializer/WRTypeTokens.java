package io.github.aquerr.worldrebuilder.storage.serializer;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

public class WRTypeTokens
{
    public static final TypeToken<BlockSnapshot> BLOCK_EXCEPTION_TYPE_TOKEN = TypeToken.get(BlockSnapshot.class);
    public static final TypeToken<List<BlockSnapshot>> BLOCK_EXCEPTION_LIST_TYPE_TOKEN = new TypeToken<List<BlockSnapshot>>() {};
    public static final TypeToken<List<EntitySnapshot>> ENTITY_SNAPSHOT_LIST_TYPE_TOKEN = new TypeToken<List<EntitySnapshot>>() {};
    public static final TypeToken<Vector3i> VECTOR3I_TYPE_TOKEN = TypeToken.get(Vector3i.class);
}
