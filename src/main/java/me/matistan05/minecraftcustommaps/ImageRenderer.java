package me.matistan05.minecraftcustommaps;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class ImageRenderer extends MapRenderer {
    private final BufferedImage image;
    public ImageRenderer(BufferedImage bufferedImage1) {
        this.image = bufferedImage1;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        mapCanvas.drawImage(0, 0, this.image);
    }
}
