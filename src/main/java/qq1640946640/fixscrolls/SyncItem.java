package qq1640946640.fixscrolls;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

class SyncItem extends BukkitRunnable {
    /*功能： 这段代码定义了一个名为SyncItem的类，继承自BukkitRunnable，用于在Minecraft插件中同步处理玩家物品的操作。
    定义了一个SyncItem类，包含私有成员变量pl（玩家对象）、i（物品对象）和d（材质对象）。
    编写了一个构造函数SyncItem，接受玩家对象p、物品对象item和材质对象dur作为参数，并将它们分别赋值给成员变量pl、i和d。
    实现了run方法，用于在运行时执行具体的操作：
    如果d表示的材质是空气（Material.AIR），则从玩家的背包中移除物品i。
    否则，将物品i的类型设置为d表示的材质。*/
    private Player pl;

    private ItemStack i;

    private Material d;

    SyncItem(Player p, ItemStack item, Material dur) {
        this.i = item;
        this.pl = p;
        this.d = dur;
    }

    public void run() {
        if (this.d == Material.AIR) {
            this.pl.getInventory().remove(this.i);
        } else {
            this.i.setType(this.d);
        }
    }
}
