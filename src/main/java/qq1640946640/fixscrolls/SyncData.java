package qq1640946640.fixscrolls;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

class SyncData extends BukkitRunnable {
    /*该代码实现的功能是创建一个名为SyncData的类，继承自BukkitRunnable类，用于同步更新物品栏中物品的耐久度。

    具体步骤如下：

    创建一个名为SyncData的类，继承自BukkitRunnable类。
    类中定义了私有属性ItemStack i，用于存储物品对象；私有属性short d，用于存储物品的耐久度。
    创建SyncData类的构造函数，接收两个参数：一个ItemStack类型的物品对象和一个short类型的耐久度。
    在构造函数中，将传入的物品对象和耐久度分别赋值给类的属性i和d。
    实现了run方法，用于在运行时更新物品对象的耐久度，即将物品对象的耐久度设置为属性d所存储的值。*/
    private ItemStack i;

    private short d;

    SyncData(ItemStack item, short dur) {
        this.i = item;
        this.d = dur;
    }

    public void run() {
        this.i.setDurability(this.d);
    }
}
