package qq1640946640.fixscrolls;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RunnablePool {
    private static final ConcurrentLinkedQueue<SyncData> syncDataPool = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<SyncItem> syncItemPool = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<SyncMeta> syncMetaPool = new ConcurrentLinkedQueue<>();
    
    public static SyncData getSyncData(ItemStack item, short dur) {
        SyncData syncData = syncDataPool.poll();
        if (syncData == null) {
            syncData = new SyncData(item, dur);
        } else {
            syncData.setItem(item);
            syncData.setDur(dur);
        }
        return syncData;
    }
    
    public static SyncItem getSyncItem(Player p, ItemStack item, Material dur) {
        SyncItem syncItem = syncItemPool.poll();
        if (syncItem == null) {
            syncItem = new SyncItem(p, item, dur);
        } else {
            syncItem.setPlayer(p);
            syncItem.setItem(item);
            syncItem.setMaterial(dur);
        }
        return syncItem;
    }
    
    public static SyncMeta getSyncMeta(ItemStack item, ItemMeta meta) {
        SyncMeta syncMeta = syncMetaPool.poll();
        if (syncMeta == null) {
            syncMeta = new SyncMeta(item, meta);
        } else {
            syncMeta.setItem(item);
            syncMeta.setMeta(meta);
        }
        return syncMeta;
    }
    
    public static void releaseSyncData(SyncData syncData) {
        if (syncData != null) {
            syncData.setItem(null);
            syncDataPool.offer(syncData);
        }
    }
    
    public static void releaseSyncItem(SyncItem syncItem) {
        if (syncItem != null) {
            syncItem.setPlayer(null);
            syncItem.setItem(null);
            syncItemPool.offer(syncItem);
        }
    }
    
    public static void releaseSyncMeta(SyncMeta syncMeta) {
        if (syncMeta != null) {
            syncMeta.setItem(null);
            syncMeta.setMeta(null);
            syncMetaPool.offer(syncMeta);
        }
    }
}