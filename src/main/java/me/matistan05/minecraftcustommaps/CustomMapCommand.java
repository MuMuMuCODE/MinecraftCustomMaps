package me.matistan05.minecraftcustommaps;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

public class CustomMapCommand implements CommandExecutor {
    private final Main main;
    public CustomMapCommand(Main main) {
        this.main = main;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {return true;}
        Player p = (Player) sender;
        if(!p.hasPermission("ditu.use")){
            p.sendMessage(ChatColor.RED + "您不是CEO会员");
            return true;
        }
        if(args.length == 0) {
            p.sendMessage(ChatColor.RED + "输入参数有误");
            return true;
        }
        if (args[0].equals("help")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.RED + "输入参数有误");
                return true;
            }
            p.sendMessage(ChatColor.GREEN + "------- " + ChatColor.WHITE + " 梦都自定义地图 " + ChatColor.GREEN + "----------");
            p.sendMessage(ChatColor.BLUE + "有以下指令:");
            p.sendMessage(ChatColor.YELLOW + "/ditu give <URL> " + ChatColor.AQUA + "- 为您提供具有原始质量的 URL 图片的地图");
            p.sendMessage(ChatColor.YELLOW + "/ditu give <URL> automatic" + ChatColor.AQUA + " - 为您提供一张地图，其中包含 URL 中的图片，该图片将自动适应展示框");
            p.sendMessage(ChatColor.YELLOW + "/ditu give <URL> small" + ChatColor.AQUA + " - 为您提供一张地图，其中 URL 中的图片大小已调整为 1x1");
            p.sendMessage(ChatColor.YELLOW + "/ditu give <URL> scale <float>" + ChatColor.AQUA + " - 为您提供一张地图，其中包含 URL 中的图片，其大小乘以给定比例");
//            p.sendMessage(ChatColor.YELLOW + "/ditu fillitemframes <x1> <y1> <z1> <x2> <y2> <z2>" + ChatColor.AQUA + " - 用物品框填充该地形并自动旋转它们");
//            p.sendMessage(ChatColor.YELLOW + "/ditu fillitemframes <x1> <y1> <z1> <x2> <y2> <z2> <direction>" + ChatColor.AQUA + " - 用设定方向的物品框填充该地形");
            p.sendMessage(ChatColor.YELLOW + "/ditu changeproperties original" + ChatColor.AQUA + " - 将持有的地图调整为原始质量");
            p.sendMessage(ChatColor.YELLOW + "/ditu changeproperties automatic" + ChatColor.AQUA + " - 将地图改为自动调整质量");
            p.sendMessage(ChatColor.YELLOW + "/ditu changeproperties small" + ChatColor.AQUA + " - 将持有的地图大小调整为 1x1");
            p.sendMessage(ChatColor.YELLOW + "/ditu changeproperties scale <float>" + ChatColor.AQUA + " - 将持有的地图的大小更改为乘以给定比例");
            p.sendMessage(ChatColor.YELLOW + "/ditu geturl" + ChatColor.AQUA + " - 为您提供所保存地图的 URL");
            p.sendMessage(ChatColor.YELLOW + "/ditu help" + ChatColor.AQUA + " - 显示命令列表");
            p.sendMessage(ChatColor.GREEN + "----------------------------------");
            return true;
        }
        if(args[0].equals("give")) {
            if(args.length == 1 || args.length > 4) {
                p.sendMessage(ChatColor.RED + "错误的参数");
                return true;
            }
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();
            MapView mapView = Bukkit.createMap(p.getWorld());
            BufferedImage image, resizedImage;
            URL url = null;
            try {
                url = new URL(args[1]);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(1000); // 设置连接超时时间为1秒
                image = ImageIO.read(connection.getInputStream());
                resizedImage = resizeImage(image);
            } catch (IOException e) {
                p.sendMessage(ChatColor.RED + "获取图片超时");
                return true;
            }
            if(image == null) {
                p.sendMessage(ChatColor.RED + "这不是图片链接");
                return true;
            }
            int offsetX = 64 - resizedImage.getWidth() / 2;
            int offsetY = 64 - resizedImage.getHeight() / 2;
            for(MapRenderer mapRenderer : mapView.getRenderers()) {
                mapView.removeRenderer(mapRenderer);
            }
            mapView.addRenderer(new MapRenderer() {
                boolean rendered = false;
                @Override
                public void render(MapView renderMap, MapCanvas canvas, Player player) {
                    if(!rendered) {
                        rendered = true;
                        canvas.drawImage(offsetX, offsetY, resizedImage);
                    }
                }
            });
            mapMeta.setMapView(mapView);
            mapMeta.getPersistentDataContainer().set(new NamespacedKey(main, "path"), PersistentDataType.STRING, args[1]);
            if(args.length > 2) {
                if(!args[2].equals("automatic") && !args[2].equals("small") && !args[2].equals("scale")) {
                    p.sendMessage(ChatColor.RED + "错误参数");
                    return true;
                }
            }
            if(args.length == 3) {
                mapMeta.getPersistentDataContainer().set(new NamespacedKey(main, "mode"), PersistentDataType.STRING, args[2]);
                if(args[2].equals("scale")) {
                    p.sendMessage(ChatColor.RED + "您必须输入一个浮点值");
                    return true;
                }
            } else if(args.length == 4) {
                if(!args[2].equals("scale")) {
                    p.sendMessage(ChatColor.RED + "该命令的错误用法");
                    return true;
                }
                mapMeta.getPersistentDataContainer().set(new NamespacedKey(main, "mode"), PersistentDataType.STRING, args[2]);
                try {
                    mapMeta.getPersistentDataContainer().set(new NamespacedKey(main, "scalemode"), PersistentDataType.FLOAT, Float.parseFloat(args[3]));
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "比例尺无效");
                    return true;
                }
            } else {
                mapMeta.getPersistentDataContainer().set(new NamespacedKey(main, "mode"), PersistentDataType.STRING, "original");
            }
            int width = (int) Math.ceil(image.getWidth() / 128d);
            int height = (int) Math.ceil(image.getHeight() / 128d);
            if(args.length != 2) {
                switch (args[2]) {
                    case "small":
                        width = 1;
                        height = 1;
                        break;
                    case "scale":
                        float scale = Float.parseFloat(args[3]);
                        width = (int) Math.ceil(image.getWidth() * scale / 128d);
                        height = (int) Math.ceil(image.getHeight() * scale / 128d);
                        if(scale < 0.01 || width > 10 || height > 10) {
                            p.sendMessage(ChatColor.RED + "比例太小或太大");
                            return true;
                        }
                        break;
                }
            }
            mapMeta.setDisplayName(ChatColor.DARK_PURPLE + "梦都地图 (" + (args.length == 3 ? (args[2].equals("automatic") ? "Max " : "") : "") + width + "x" + height + ")");
            mapMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            List<String> lore = new LinkedList<>();
            lore.add(ChatColor.GRAY + (args.length == 3 ? (args[2].equals("automatic") ? "最多为 " : "") : "") + "需要"+width * height + "个展示框");
            lore.add(ChatColor.BLUE + "如何使用它?");
            lore.add(ChatColor.GRAY + "创建一堵物品展示框墙，然后放置");
            lore.add(ChatColor.GRAY + "这张地图位于左下角的物品框中,");
            lore.add(ChatColor.GRAY + "您的图像将填充至物品展示框");
            lore.add(ChatColor.BLUE + "如何删除它?");
            lore.add(ChatColor.GRAY + "按住 Shift 键并单击其中一张地图");
            lore.add(ChatColor.GRAY + "可将整个图像从墙上移除.");
            mapMeta.setLore(lore);
            map.setItemMeta(mapMeta);
            p.getInventory().addItem(map);
            return true;
        }
//        if(args[0].equals("fillitemframes")) {
//            if(args.length != 7 && args.length != 8) {
//                p.sendMessage(ChatColor.RED + "错误指令用法");
//                return true;
//            }
//            if(args.length == 8 && !args[7].equals("north") && !args[7].equals("south") && !args[7].equals("east") && !args[7].equals("west") && !args[7].equals("up") && !args[7].equals("down")) {
//                p.sendMessage(ChatColor.RED + "错误指令用法");
//                return true;
//            }
//            Location loc1, loc2;
//            try {
//                loc1 = new Location(p.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
//                loc2 = new Location(p.getWorld(), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
//            } catch (Exception e) {
//                p.sendMessage(ChatColor.RED + "这些不是整数");
//                return true;
//            }
//            if((loc1.getBlockX() - loc2.getBlockX()) * (loc1.getBlockY() - loc2.getBlockY()) * (loc1.getBlockZ() - loc2.getBlockZ()) > 10) {
//                p.sendMessage(ChatColor.RED + "指定区域内的块过多（最大10" +
//                        (loc1.getBlockX() - loc2.getBlockX()) * (loc1.getBlockY() - loc2.getBlockY()) * (loc1.getBlockZ() - loc2.getBlockZ()) +
//                        ")");
//                return true;
//            }
//            BlockFace blockFace;
//            for(int x = 0; x <= Math.abs(loc2.getBlockX() - loc1.getBlockX()); x++) {
//                for(int y = 0; y <= Math.abs(loc2.getBlockY() - loc1.getBlockY()); y++) {
//                    for(int z = 0; z <= Math.abs(loc2.getBlockZ() - loc1.getBlockZ()); z++) {
//                        new Location(p.getWorld(), loc1.getBlockX() + x * (loc1.getBlockX() > loc2.getBlockX() ? -1 : 1), loc1.getBlockY() + y * (loc1.getBlockY() > loc2.getBlockY() ? -1 : 1), loc1.getBlockZ() + z * (loc1.getBlockZ() > loc2.getBlockZ() ? -1 : 1)).getBlock().setType(Material.AIR);
//                    }
//                }
//            }
//            if(args.length == 7) {
//                int[] directions = new int[6];
//                for(int x = 0; x <= Math.abs(loc2.getBlockX() - loc1.getBlockX()); x++) {
//                    for(int y = 0; y <= Math.abs(loc2.getBlockY() - loc1.getBlockY()); y++) {
//                        for(int z = 0; z <= Math.abs(loc2.getBlockZ() - loc1.getBlockZ()); z++) {
//                            for(int i = 0; i < 6; i++) {
//                                Location location = new Location(p.getWorld(), loc1.getBlockX() + x * (loc1.getBlockX() > loc2.getBlockX() ? -1 : 1), loc1.getBlockY() + y * (loc1.getBlockY() > loc2.getBlockY() ? -1 : 1), loc1.getBlockZ() + z * (loc1.getBlockZ() > loc2.getBlockZ() ? -1 : 1));
//                                if(isBlockBehind(location, BlockFace.values()[i]) && noItemFrame(location, BlockFace.values()[i])) {
//                                    directions[i]++;
//                                }
//                            }
//                        }
//                    }
//                }
//                int maxValue = 0, maxI = 0;
//                for(int i = 0; i < 6; i++) {
//                    if(directions[i] > maxValue) {
//                        maxValue = directions[i];
//                        maxI = i;
//                    }
//                }
//                blockFace = BlockFace.values()[maxI];
//            } else {
//                blockFace = BlockFace.valueOf(args[7].toUpperCase());
//            }
//            int count = 0;
//            for(int x = 0; x <= Math.abs(loc2.getBlockX() - loc1.getBlockX()); x++) {
//                for(int y = 0; y <= Math.abs(loc2.getBlockY() - loc1.getBlockY()); y++) {
//                    for(int z = 0; z <= Math.abs(loc2.getBlockZ() - loc1.getBlockZ()); z++) {
//                        Location location = new Location(p.getWorld(), loc1.getBlockX() + x * (loc1.getBlockX() > loc2.getBlockX() ? -1 : 1), loc1.getBlockY() + y * (loc1.getBlockY() > loc2.getBlockY() ? -1 : 1), loc1.getBlockZ() + z * (loc1.getBlockZ() > loc2.getBlockZ() ? -1 : 1));
//                        if(noItemFrame(location, blockFace) && isBlockBehind(location, blockFace)) {
//                            ItemFrame itemFrame = (ItemFrame) p.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);
//                            itemFrame.setFacingDirection(blockFace);
//                            count++;
//                        }
//                    }
//                }
//            }
//            if(count > 0) {
//                p.sendMessage(ChatColor.GREEN + "已成功填充 " + count + " 个展示框" + (count > 1 ? "s" : ""));
//            } else {
//                p.sendMessage(ChatColor.RED + "没有物品展示框");
//            }
//            return true;
//        }
        if(args[0].equals("geturl")) {
            if(args.length > 1) {
                p.sendMessage(ChatColor.RED + "错误指令");
                return true;
            }
            if(!p.getInventory().getItemInMainHand().hasItemMeta()) {
                p.sendMessage(ChatColor.RED + "您没有持有自定义地图");
                return true;
            }
            PersistentDataContainer container = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            if(!container.has(new NamespacedKey(main, "path"), PersistentDataType.STRING)) {
                p.sendMessage(ChatColor.RED + "您没有持有自定义地图");
                return true;
            }
            TextComponent message = new TextComponent("单击此处获取该地图的 url");
            message.setColor(ChatColor.GREEN.asBungee());
            message.setBold(true);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, container.get(new NamespacedKey(main, "path"), PersistentDataType.STRING)));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("单击此处获取该地图的 url!").italic(true).color(ChatColor.GRAY.asBungee()).create()));
            p.spigot().sendMessage(message);
            return true;
        }
        if(args[0].equals("changeproperties")) {
            if(args.length == 1 || args.length > 3) {
                p.sendMessage(ChatColor.RED + "该命令的错误用法");
                return true;
            }
            if(((!args[1].equals("automatic") && !args[1].equals("small") && !args[1].equals("original")) && args.length == 2) || ((!args[1].equals("scale")) && args.length == 3)) {
                p.sendMessage(ChatColor.RED + "该命令的错误用法");
                return true;
            }
            if(!p.getInventory().getItemInMainHand().hasItemMeta()) {
                p.sendMessage(ChatColor.RED + "您没有持有自定义地图");
                return true;
            }
            ItemMeta itemMeta = p.getInventory().getItemInMainHand().getItemMeta();
            if(!itemMeta.getPersistentDataContainer().has(new NamespacedKey(main, "path"), PersistentDataType.STRING)) {
                p.sendMessage(ChatColor.RED + "您没有持有自定义地图");
                return true;
            }
            String mode = itemMeta.getPersistentDataContainer().get(new NamespacedKey(main, "mode"), PersistentDataType.STRING);
            if((mode.equals(args[1]) && !args[1].equals("scale"))) {
                p.sendMessage(ChatColor.RED + "该属性已分配给该地图");
                return true;
            }
            if(mode.equals("scale") && !args[1].equals("scale")) {
                itemMeta.getPersistentDataContainer().remove(new NamespacedKey(main, "scalemode"));
            }
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(main, "mode"), PersistentDataType.STRING, args[1]);
            if(args[1].equals("scale")) {
                try {
                    if(mode.equals("scale") && itemMeta.getPersistentDataContainer().get(new NamespacedKey(main, "scalemode"), PersistentDataType.FLOAT) == Float.parseFloat(args[2])) {
                        p.sendMessage(ChatColor.RED + "该属性已分配给该地图");
                        return true;
                    }
                    itemMeta.getPersistentDataContainer().set(new NamespacedKey(main, "scalemode"), PersistentDataType.FLOAT, Float.parseFloat(args[2]));
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "比例无效");
                    return true;
                }
            }
            BufferedImage image = null;
            try {
                image = ImageIO.read(new URL(itemMeta.getPersistentDataContainer().get(new NamespacedKey(main, "path"), PersistentDataType.STRING)));
            } catch (Exception ignored) {}
            int width = (int) Math.ceil(image.getWidth() / 128d);
            int height = (int) Math.ceil(image.getHeight() / 128d);
            switch (args[1]) {
                case "small":
                    width = 1;
                    height = 1;
                    break;
                case "scale":
                    float scale = Float.parseFloat(args[2]);
                    width = (int) Math.ceil(image.getWidth() * scale / 128d);
                    height = (int) Math.ceil(image.getHeight() * scale / 128d);
                    if(scale < 0.01 || width > 10 || height > 10) {
                        p.sendMessage(ChatColor.RED + "比例太小或太大");
                        return true;
                    }
                    break;
            }
            itemMeta.setDisplayName(ChatColor.DARK_PURPLE + "梦都地图 (" + (args[1].equals("automatic") ? "Max " : "") + width + "x" + height + ")");
            List<String> lore = itemMeta.getLore();
            lore.set(0, ChatColor.GRAY + (args[1].equals("automatic") ? "最多为 " : "") + "需要"+width * height + "个物品展示框");
            itemMeta.setLore(lore);
            p.getInventory().getItemInMainHand().setItemMeta(itemMeta);
            p.sendMessage(ChatColor.GREEN + "已成功更改该地图的属性!");
            return true;
        }
        p.sendMessage(ChatColor.RED + "该命令的错误用法");
        return true;
    }
    public static BufferedImage resizeImage(BufferedImage image) {
        BufferedImage outputImage;
        if(image.getHeight() >= image.getWidth()) {
            Image resultingImage =  image.getScaledInstance(128 * image.getWidth() / image.getHeight(), 128, Image.SCALE_SMOOTH);
            outputImage = new BufferedImage(128 * image.getWidth() / image.getHeight(), 128, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        } else {
            Image resultingImage =  image.getScaledInstance(128, 128 * image.getHeight() / image.getWidth(), Image.SCALE_SMOOTH);
            outputImage = new BufferedImage(128, 128 * image.getHeight() / image.getWidth(), BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        }
        return outputImage;
    }
    public boolean noItemFrame(Location location, BlockFace blockFace) {
        for(Entity entity : location.getWorld().getEntities()) {
            if(entity.getLocation().getBlockX() == location.getX() && entity.getLocation().getBlockY() == location.getY() && entity.getLocation().getBlockZ() == location.getZ() && entity instanceof ItemFrame && entity.getFacing().equals(blockFace)) {
                return false;
            }
        }
        return true;
    }
    public boolean isBlockBehind(Location location, BlockFace blockFace) {
        Location blockLocation = new Location(location.getWorld(), location.getBlockX() - blockFace.getModX(), location.getBlockY() - blockFace.getModY(), location.getBlockZ() - blockFace.getModZ());
        Material material = blockLocation.getBlock().getType();
        if(!material.isSolid() || ((material.name().contains("FENCE") || material.name().contains("WALL")) && blockFace.equals(BlockFace.UP))) {
            return false;
        } else {
            return true;
        }
    }
}