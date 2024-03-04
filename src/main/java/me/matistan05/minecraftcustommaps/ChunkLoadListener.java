package me.matistan05.minecraftcustommaps;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ChunkLoadListener implements Listener {

    private JavaPlugin plugin;
    private Set<String> loadedChunks;
    private HashMap<String, NamespacedKey> keysCache;

    public ChunkLoadListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loadedChunks = new HashSet<>();
        this.keysCache = new HashMap<>();
        cacheKeys();
    }

    private void cacheKeys() {
        // 缓存NamespacedKey，避免重复创建
        keysCache.put("uuid", new NamespacedKey(plugin, "uuid"));
        keysCache.put("imagei", new NamespacedKey(plugin, "imagei"));
        keysCache.put("imagej", new NamespacedKey(plugin, "imagej"));
        keysCache.put("imagescale", new NamespacedKey(plugin, "imagescale"));
        keysCache.put("path", new NamespacedKey(plugin, "path"));
        // ... 其他key
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String chunkKey = chunk.getX() + "," + chunk.getZ();

        if (!event.isNewChunk() && !loadedChunks.contains(chunkKey)) {
            loadedChunks.add(chunkKey);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                HashMap<String, BufferedImage> imageCache = new HashMap<>();

                for (Entity entity : chunk.getEntities()) {
                    if (!(entity instanceof ItemFrame)) continue;

                    ItemFrame itemFrame = (ItemFrame) entity;
                    PersistentDataContainer container = itemFrame.getPersistentDataContainer();
                    if (!container.has(keysCache.get("uuid"), PersistentDataType.STRING)) continue;

                    String uuid = container.get(keysCache.get("uuid"), PersistentDataType.STRING);
                    int imageI = container.get(keysCache.get("imagei"), PersistentDataType.INTEGER);
                    int imageJ = container.get(keysCache.get("imagej"), PersistentDataType.INTEGER);

                    BufferedImage image = imageCache.computeIfAbsent(uuid, k -> loadImage(container));
                    if (image == null) continue;

                    final BufferedImage finalImage = image;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MapView mapView = Bukkit.createMap(itemFrame.getWorld());
                        mapView.getRenderers().clear();
                        CustomMapRenderer customMapRenderer = new CustomMapRenderer(imageI, imageJ, finalImage);
                        mapView.addRenderer(customMapRenderer);

                        ItemStack map = itemFrame.getItem();
                        MapMeta mapMeta = (MapMeta) map.getItemMeta();
                        mapMeta.setMapView(mapView);
                        map.setItemMeta(mapMeta);
                        itemFrame.setItem(map);
                    });
                }
            });
        }
    }

    private BufferedImage loadImage(PersistentDataContainer container) {
        float imageScale = container.get(keysCache.get("imagescale"), PersistentDataType.FLOAT);
        String imagePath = container.get(keysCache.get("path"), PersistentDataType.STRING);
        BufferedImage image = null;
        try {
            URL url = new URL(imagePath);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000); // 设置连接超时时间为1秒
            image = ImageIO.read(connection.getInputStream());
            if (imageScale != 0) {
                image = scaleImage(image, imageScale);
            }
        } catch (IOException e) {
            Bukkit.getLogger().info("自定义地图加载超时");
        }
        return image;
    }

    private BufferedImage scaleImage(BufferedImage originalImage, float scale) {
        int newWidth = (int) Math.ceil(originalImage.getWidth() * scale);
        int newHeight = (int) Math.ceil(originalImage.getHeight() * scale);
        Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
