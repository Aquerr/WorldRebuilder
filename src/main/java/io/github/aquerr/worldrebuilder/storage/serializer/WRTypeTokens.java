package io.github.aquerr.worldrebuilder.storage.serializer;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntitySnapshot;

import java.util.List;

public class WRTypeTokens
{
    public static final TypeToken<BlockSnapshot> BLOCK_EXCEPTION = TypeToken.of(BlockSnapshot.class);
    public static final TypeToken<List<BlockSnapshot>> BLOCK_EXCEPTION_LIST = new TypeToken<List<BlockSnapshot>>() {};
    public static final TypeToken<List<EntitySnapshot>> ENTITY_SNAPSHOT_LIST_TYPE_TOKEN = new TypeToken<List<EntitySnapshot>>() {};
}
