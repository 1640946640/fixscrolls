package qq1640946640.fixscrolls;  // 定义包名

import java.io.File;  // 导入所需的类
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FS extends JavaPlugin implements Listener {  // 定义FS类，继承JavaPlugin类并实现Listener接口
    static FS instance;  // 静态变量instance
    Map<Integer, Short> supportedIDs;  // 存储支持的物品ID
    String itemName;  // 物品名称
    Integer threshold;  // 阈值
    String key;  // 关键字

    public void onEnable() {  // 插件启用时执行的方法
        instance = this;
        loadFiles();
        (new checkTask()).runTaskTimerAsynchronously((Plugin)this, 0L, 100L);
    }

    private void loadFiles() {  // 加载配置文件等操作的方法
        getDataFolder().mkdirs();
        File f = new File(getDataFolder(), "config.yml");
        if (!f.exists())
            saveDefaultConfig();
        reloadConfig();

        this.supportedIDs = new ConcurrentHashMap<>();
        getConfig().getIntegerList("ids").forEach(i -> {
            Material material = Material.getMaterial(i.intValue());
            if (material == null) {
                getLogger().warning("无法加载物品id: " + i);
            } else {
                this.supportedIDs.put(i, Short.valueOf(material.getMaxDurability()));
            }
        });
        System.gc();

        this.itemName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("name"));
        List<String> strings = getConfig().getStringList("lore");
        strings.forEach(str -> {
            if (str.contains("%dur%")) {
                this.key = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', str)).replace("%dur%", "");
            }
        });
        if (this.key == null) {
            getLogger().warning("配置文件没有找到合适的lore，请检查配置文件");
            getServer().getPluginManager().disablePlugin((Plugin)this);
        }
    }

    private String t(String m) {  // 返回修复卷轴的格式化字符串
        return "§7♮§a修复卷轴§7♮ " + ChatColor.translateAlternateColorCodes('&', m);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {  // 处理命令的方法
        if (cmd.getName().equals("fixscroll")) {
            if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(new String[] { "§a==========§b✮§e修复卷轴§b✮§a==========",
                        "§e/fixscroll reload        §6重载插件", "§e/fixscroll give [玩家] [耐久]    §6给玩家一个指定耐久的修复卷轴",
                        "§a=====§b✮§6修复你背包里所有耐久低于80%的物品§b✮§a=====" });
                return true;
            } else if (!sender.isOp()) {
                sender.sendMessage(t("&c你需要OP权限才能使用本指令"));
                return true;
            } else if (args.length < 1) {
                (new checkTask()).runTaskAsynchronously((Plugin)this);
                sender.sendMessage(t("正在手动刷新"));
                sender.sendMessage(t("&c请使用 &e/fixscroll help &c查看插件帮助"));
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                loadFiles();
                sender.sendMessage(t("&a成功重载"));
            } else if (args[0].equalsIgnoreCase("give")) {
                if (args.length < 3) {
                    sender.sendMessage(t("&c错误: 请使用 &6/fixscroll give [玩家] [耐久]"));
                    return true;
                }
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    sender.sendMessage(t("&c错误: 无法找到玩家: &6" + args[1]));
                    return true;
                }
                if (!args[2].matches("^[0-9]+")) {
                    sender.sendMessage(t("&c错误: &6" + args[2] + " &c并不是一个有效的数字"));
                    return true;
                }
                int dur = Integer.valueOf(args[2]).intValue();
                ItemStack item = new ItemStack(getConfig().getInt("item"), 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(this.itemName);
                List<String> lore = new ArrayList<>();
                getConfig().getStringList("lore").forEach(l -> lore.add(ChatColor.translateAlternateColorCodes('&', l.replace("%dur%", args[2]))));
                meta.setLore(lore);
                item.setItemMeta(meta);
                p.getInventory().addItem(new ItemStack[] { item });
                sender.sendMessage(t("&a成功给予玩家 &6" + p.getDisplayName() + " &a一个 " + meta.getDisplayName()));
            }
            return true;
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {  // 玩家交互事件处理方法
        Action action = event.getAction();
        if ((action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR || (
                action == Action.RIGHT_CLICK_BLOCK && !event.isCancelled() &&
                event.getMaterial() != Material.MINECART && event.getMaterial() != Material.BOAT) ||
                action == Action.RIGHT_CLICK_AIR) &&
                computeInterval(event.getPlayer())) {
            event.setCancelled(true);
            if (cache.contains(event.getPlayer())) {
                event.getPlayer().kickPlayer("§c请勿使用连点器！");
                cache.remove(event.getPlayer());
            } else {
                cache.add(event.getPlayer());
                event.getPlayer().sendMessage("§6[防连点器]§c请勿使用连点器，否则将会踢出服务器");
            }
        }
    }

    private static List<Player> cache = new ArrayList<>();  // 缓存玩家列表
    private static Map<Player, Long> cd = new HashMap<>();  // 玩家与时间戳的映射

    private boolean computeInterval(Player p) {  // 计算时间间隔是否满足条件
        if (cd.get(p) == null || System.currentTimeMillis() - ((Long)cd.get(p)).longValue() > this.threshold.intValue()) {
            cd.put(p, Long.valueOf(System.currentTimeMillis()));
            return false;
        }
        cd.put(p, Long.valueOf(System.currentTimeMillis()));
        return true;
    }
}