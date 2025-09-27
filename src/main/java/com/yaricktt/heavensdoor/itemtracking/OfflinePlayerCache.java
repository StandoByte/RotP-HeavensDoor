package com.yaricktt.heavensdoor.itemtracking;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OfflinePlayerCache extends WorldSavedData {
    public static final String DATA_NAME = "heavensdoor_offline_cache";
    private final Map<UUID, CompoundNBT> playerData = new ConcurrentHashMap<>();

    public OfflinePlayerCache(String name) {
        super(name);
    }

    public static OfflinePlayerCache get(MinecraftServer server) {
        DimensionSavedDataManager storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(() -> new OfflinePlayerCache(DATA_NAME), DATA_NAME);
    }

    public void cachePlayerData(UUID playerUUID, int pageCount) {
        CompoundNBT data = new CompoundNBT();
        data.putInt("PageCount", pageCount);
        playerData.put(playerUUID, data);
        setDirty();
    }

    public CompoundNBT retrieveAndClearData(UUID playerUUID) {
        CompoundNBT data = playerData.remove(playerUUID);
        if (data != null) {
            setDirty();
        }
        return data;
    }

    public boolean hasDataFor(UUID playerUUID) {
        return playerData.containsKey(playerUUID);
    }

    public boolean decrementPageCount(UUID playerUUID, int amount) {
        CompoundNBT data = playerData.get(playerUUID);
        if (data == null) return false;

        int currentPageCount = data.getInt("PageCount");
        if (currentPageCount <= 0) return false;

        int newPageCount = Math.max(0, currentPageCount - amount);
        data.putInt("PageCount", newPageCount);
        setDirty();
        return true;
    }

    @Override
    public void load(CompoundNBT nbt) {
        playerData.clear();
        for (String key : nbt.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            playerData.put(uuid, nbt.getCompound(key));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        playerData.forEach((uuid, data) -> {
            compound.put(uuid.toString(), data);
        });
        return compound;
    }
}