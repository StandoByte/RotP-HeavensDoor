package com.yaricktt.heavensdoor.itemtracking;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PageTrackerMap extends WorldSavedData {
    public static final String DATA_NAME = "heavensdoor_invalid_targets_map";
    private final Set<UUID> invalidatedTargets = ConcurrentHashMap.newKeySet();

    public PageTrackerMap(String name) {
        super(name);
    }

    public boolean isInvalidated(UUID targetUUID) {
        return invalidatedTargets.contains(targetUUID);
    }

    public void reviveTarget(UUID targetUUID) {
        if (targetUUID != null && invalidatedTargets.remove(targetUUID)) {
            setDirty();
        }
    }

    public static PageTrackerMap get(MinecraftServer server) {
        if (server == null) {
            throw new IllegalStateException("Cannot get PageTrackerMap without a server instance.");
        }
        DimensionSavedDataManager storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(() -> new PageTrackerMap(DATA_NAME), DATA_NAME);
    }

    public void invalidateTarget(UUID targetUUID) {
        if (targetUUID != null && invalidatedTargets.add(targetUUID)) {
            setDirty();
        }
    }

    @Override
    public void load(CompoundNBT nbt) {
        invalidatedTargets.clear();
        ListNBT list = nbt.getList("InvalidatedTargets", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < list.size(); i++) {
            invalidatedTargets.add(list.getCompound(i).getUUID("uuid"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (UUID uuid : invalidatedTargets) {
            CompoundNBT tag = new CompoundNBT();
            tag.putUUID("uuid", uuid);
            list.add(tag);
        }
        compound.put("InvalidatedTargets", list);
        return compound;
    }
}