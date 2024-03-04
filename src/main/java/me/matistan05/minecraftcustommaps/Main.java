package me.matistan05.minecraftcustommaps;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import static me.matistan05.minecraftcustommaps.CustomMapCommand.resizeImage;

public final class Main extends JavaPlugin {
    private FileConfiguration mapConfig;
    private Plugin dcMapPlugin;
    private Map<Integer, String> mapImages;
    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("ditu").setExecutor(new CustomMapCommand(this));
        Bukkit.getPluginCommand("ditu").setTabCompleter(new CustomMapCompleter());
        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChunkLoadListener(this), this);
        dcMapPlugin = Bukkit.getPluginManager().getPlugin("dcmap");

//        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
//            @Override
//            public void run() {
//                HashMap<String, BufferedImage> hashMap = new HashMap<>();
//                Bukkit.getLogger().info("开始加载自定义地图");
//                for(World world : Bukkit.getWorlds()) {
//                    //Bukkit.getLogger().info(world.getName());
//                    for(Entity entity : world.getEntities()) {
//                        //Bukkit.getLogger().info(entity.getName());
//                        if(!(entity instanceof ItemFrame)) {
//                            //Bukkit.getLogger().info(world.getName() + "不是展示框");
//                            continue;}
//                        //Bukkit.getLogger().info(world.getName() + "找到展示框");
//                        ItemFrame itemFrame = (ItemFrame) entity;
//                        PersistentDataContainer container = itemFrame.getPersistentDataContainer();
//                        if(!container.has(new NamespacedKey(dcMapPlugin, "uuid"), PersistentDataType.STRING)) {continue;}
//                        String uuid = container.get(new NamespacedKey(dcMapPlugin, "uuid"), PersistentDataType.STRING);
//                        int imageI = container.get(new NamespacedKey(dcMapPlugin, "imagei"), PersistentDataType.INTEGER);
//                        int imageJ = container.get(new NamespacedKey(dcMapPlugin, "imagej"), PersistentDataType.INTEGER);
//                        ItemStack map = itemFrame.getItem();
//                        MapMeta mapMeta = (MapMeta) map.getItemMeta();
//                        MapView mapView = Bukkit.createMap(world);
//                        mapView.getRenderers().clear();
//                        CustomMapRenderer customMapRenderer;
//                        BufferedImage image = null;
//                        if(imageI == 0 && imageJ == 0 || !hashMap.containsKey(uuid)) {
//                            float imageScale = container.get(new NamespacedKey(dcMapPlugin, "imagescale"), PersistentDataType.FLOAT);
//                            String imagePath = container.get(new NamespacedKey(dcMapPlugin, "path"), PersistentDataType.STRING);
//                            try {
//                                URL url = new URL(imagePath);
//                                URLConnection connection = url.openConnection();
//                                connection.setConnectTimeout(1000); // 设置连接超时时间为1秒
//                                image = ImageIO.read(connection.getInputStream());
//                            } catch (IOException ignored) {
//                                Bukkit.getLogger().info(itemFrame.getLocation() + " 自定义地图加载超时");
//                            }
//                            if(image == null) {
//                                continue;
//                            }
//                            if(imageScale == 0) {
//                                image = resizeImage(image);
//                            } else {
//                                Image resultingImage = image.getScaledInstance((int) Math.ceil(image.getWidth() * imageScale), (int) Math.ceil(image.getHeight() * imageScale), Image.SCALE_SMOOTH);
//                                BufferedImage outputImage = new BufferedImage((int) Math.ceil(image.getWidth() * imageScale), (int) Math.ceil(image.getHeight() * imageScale), BufferedImage.TYPE_INT_RGB);
//                                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
//                                image = outputImage;
//                            }
//                            customMapRenderer = new CustomMapRenderer(imageI, imageJ, image);
//                            hashMap.put(uuid, image);
//                        } else {
//                            customMapRenderer = new CustomMapRenderer(imageI, imageJ, hashMap.get(uuid));
//                        }
//                        mapView.addRenderer(customMapRenderer);
//                        mapMeta.setMapView(mapView);
//                        map.setItemMeta(mapMeta);
//                        itemFrame.setItem(map);
//                    }
//                    hashMap.clear();
//                }
//            }
//        }, 200L); // 20L表示延迟20个tick，1秒钟大约有20个tick


    }

}