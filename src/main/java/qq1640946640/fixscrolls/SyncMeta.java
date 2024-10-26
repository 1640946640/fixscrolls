package qq1640946640.fixscrolls;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

class SyncMeta extends BukkitRunnable {
    /*功能：该代码实现了一个同步更新物品的元数据的功能。
    定义了一个名为SyncMeta的类，继承自BukkitRunnable类。
    类中声明了两个私有属性i和m，分别表示物品（ItemStack）和元数据（ItemMeta）。
    定义了一个构造方法SyncMeta，接受两个参数item和meta，并将其分别赋值给属性i和m。
    实现了run方法，该方法在运行时会将物品i的元数据设置为m。*/
    private ItemStack i;

    private ItemMeta m;

    SyncMeta(ItemStack item, ItemMeta meta) {
        this.i = item;
        this.m = meta;
    }

    public void run() {
        this.i.setItemMeta(this.m);
    }
}