//package mt.com.go.go_hack_v1.apoe;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//public class Heatmap {
//
//    private static final float MAX_HEAT                  = 0.4f;
//    private static final float MIN_HEAT                  = -80f;
//    private static final String path  = "results/test_data";
//
//    public static void generateHeatMapImage(double[][] signalStrengthHeatMap, int step, int accessPointCount) {
//        try {
//            BufferedImage debugImage = new BufferedImage(
//                    signalStrengthHeatMap[0].length,
//                    signalStrengthHeatMap.length,
//                    BufferedImage.TYPE_INT_RGB
//            );
//
//            for (int i = 0; i < signalStrengthHeatMap.length; i++) {
//                for (int j = 0; j < signalStrengthHeatMap[0].length; j++) {
//                    Color color = generateColor(signalStrengthHeatMap[i][j]);
//                    debugImage.setRGB(j, i, color.getRGB());
//                }
//            }
//
//            Path dir = Paths.get("results");
//
//            if (!Files.exists(dir)) {
//                Files.createDirectory(dir);
//            }
//
//            dir = Paths.get( path + "_" + accessPointCount);
//
//            if (!Files.exists(dir)) {
//                Files.createDirectory(dir);
//            }
//
//            File outputfile = new File(dir.toString() + "/HeatMapResult" + step + ".png");
//            ImageIO.write(debugImage, "png", outputfile);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static Color generateColor(double v) {
//        float threshold = MAX_HEAT - MIN_HEAT;
//        float heatPercentage = (float) -v / threshold;
//        if (heatPercentage > 1f) {
//            heatPercentage = 1f;
//        }
//        if (heatPercentage < 0f) {
//            heatPercentage = 0f;
//        }
//
//        float sat = 255;
//        float lum = 255;
//        float hue = 2*150 * (1f - heatPercentage)/2;
//
//        return Color.getHSBColor(hue/255f,sat/255f,lum/255f);
//    }
//
//}
