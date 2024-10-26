package qq1640946640.fixscrolls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

class checkTask extends BukkitRunnable {
    /*该部分代码实现的功能是检查在线玩家的物品，根据一定规则修复损坏的物品。*/
    public void run() {
        //初始化设置
        List<ItemStack> repairItems = new ArrayList<>();
        ItemStack repairStone = null;
        byte b;
        int i;
        //获取所有在线玩家的列表。
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Player[] arrayOfPlayer = players.toArray(new Player[players.size()]);
        for (i = 0, b = 0; b < arrayOfPlayer.length; ) {
            Player p = arrayOfPlayer[b];
            //清空修复物品列表和修复石变量。
            repairItems.clear();
            repairStone = null;
            ItemStack[] chk = (ItemStack[])ArrayUtils.addAll((Object[])p.getInventory().getContents(),
                    (Object[])p.getInventory().getArmorContents());
            int max = -1;
            byte b1;
            int j;
            //遍历每个物品：
            ItemStack[] arrayOfItemStack1;
            for (j = (arrayOfItemStack1 = chk).length, b1 = 0; b1 < j; ) {
                ItemStack item = arrayOfItemStack1[b1];
                if (item != null) {
                    //如果物品在支持修复的物品列表中，则加入修复物品列表
                    if (FS.instance.supportedIDs.containsKey(Integer.valueOf(item.getTypeId()))) {
                        repairItems.add(item);
                        //如果修复石为空且物品有元数据（lore），则检查元数据中是否包含特定关键字和值，如果是则将修复石设为该物品。
                    } else if (repairStone == null && item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.hasDisplayName() && meta.getDisplayName().equals(FS.instance.itemName) &&
                                meta.hasLore()) {
                            List<String> lore = meta.getLore();
                            //判断物品的lore是否有修复卷轴判断的关键字%dur%
                            if (lore != null) {
                                for (String str : lore) {
                                    if (str != null && ChatColor.stripColor(str).contains(FS.instance.key)) {
                                        try {
                                            max = Integer.valueOf(ChatColor.stripColor(str).replace(FS.instance.key, "")).intValue();
                                            repairStone = item;
                                        } catch (Exception e) {
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                b1++;
            }
            //如果修复卷轴为空或最大值小于0，则继续下一个玩家。
            if (repairStone == null || max < 0) {
                b++;
                continue;
            }
            int consume = 0;//这里设置初始化修复卷轴未来将会消耗的值consume
            for (ItemStack rep : repairItems) {
                /*增强for循环这里对每个功能：遍历一个名为repairItems的列表中的每一个元素。
                对repairItems列表中的每一个元素执行以下操作：
                将当前元素赋值给变量rep，并执行相应的操作。*/
                short full = ((Short)FS.instance.supportedIDs.get(Integer.valueOf(rep.getTypeId()))).shortValue();
                short dmg = rep.getDurability();
                if (consume >= max) {
                    break;
                }
                if (dmg > full * 0.2D) {
                    //当剩余耐久小于80%的情况
                    //相当于就是物品已损耗的耐久 公式是full(满耐久) = dmg + 剩余耐久
                    consume += dmg;
                    //consume就是将要为已损耗的耐久加上的修复卷轴的"将要"消耗的耐久
                    if (consume > max) {
                        //这里是情况分支1:当修复卷轴将要消耗的耐久 "大于" 修复卷轴本身的耐久时候
                        dmg = (short)(dmg - (consume - max));
                        //已损耗的耐久 = 已损耗的耐久 -(修复卷轴将要消耗的耐久 - 修复卷轴剩下的耐久)
                        /*确保当consume超过max时，将多余的值转移到下一个修复的装备上。同时，我添加了consume = max;
                        来确保consume不会超过max的值。
                        这样修复卷轴的剩余耐久值会正确转移到要修复的装备上。*/
                        consume = max;
                    }
                    if (dmg < 0) {
                        //损耗小于0时,直接跳出循环
                        break;
                    }
                    (new SyncData(rep, (short)(rep.getDurability() - dmg))).runTask((Plugin)FS.instance);
                }
            }
            if (consume > 0) {
                List<String> lores = repairStone.getItemMeta().getLore();
                for (int k = 0; k < lores.size(); k++) {
                    String str = lores.get(k);
                    //查看是否有修复卷轴的lore内容关键字,然后对其进行更新
                    if (str.contains(FS.instance.key)) {
                        if (consume >= max) {
                            (new SyncItem(p, repairStone, Material.AIR)).runTask((Plugin)FS.instance);
                            break;
                        }
                        lores.remove(k);
                        lores.add(k, str.replace(String.valueOf(max), String.valueOf(max - consume)));
                        ItemMeta m = repairStone.getItemMeta();
                        m.setLore(lores);
                        repairStone.setItemMeta(m); // 更新修复石的元数据
                        (new SyncMeta(repairStone, m)).runTask((Plugin)FS.instance); // 更新修复石的元数据
                        break;
                    }
                }
            }
            b++;
        }
    }
}