package com.retailer.cart.utils;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VisualTestingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(VisualTestingUtil.class);
    private static final String BASELINE_DIR = "src/test/resources/visual-baselines";
    private static final String ACTUAL_DIR = "target/visual-actual";
    private static final String DIFF_DIR = "target/visual-diff";
    private static final double DEFAULT_THRESHOLD = 0.05; // 5% difference threshold
    
    private final WebDriver driver;
    private final AShot aShot;
    
    public VisualTestingUtil(WebDriver driver) {
        this.driver = driver;
        this.aShot = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(100));
        createDirectories();
    }
    
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(BASELINE_DIR));
            Files.createDirectories(Paths.get(ACTUAL_DIR));
            Files.createDirectories(Paths.get(DIFF_DIR));
        } catch (IOException e) {
            logger.error("Failed to create visual testing directories", e);
        }
    }
    
    /**
     * Takes a full page screenshot and compares it with the baseline
     * @param testName unique name for the test
     * @return true if images match within threshold, false otherwise
     */
    public boolean compareFullPageScreenshot(String testName) {
        return compareFullPageScreenshot(testName, DEFAULT_THRESHOLD);
    }
    
    /**
     * Takes a full page screenshot and compares it with the baseline
     * @param testName unique name for the test
     * @param threshold acceptable difference percentage (0.0 to 1.0)
     * @return true if images match within threshold, false otherwise
     */
    public boolean compareFullPageScreenshot(String testName, double threshold) {
        try {
            // Take current screenshot
            Screenshot screenshot = aShot.takeScreenshot(driver);
            BufferedImage actualImage = screenshot.getImage();
            
            // Save actual screenshot
            String actualPath = ACTUAL_DIR + "/" + testName + "_actual.png";
            ImageIO.write(actualImage, "PNG", new File(actualPath));
            
            // Check if baseline exists
            String baselinePath = BASELINE_DIR + "/" + testName + "_baseline.png";
            File baselineFile = new File(baselinePath);
            
            if (!baselineFile.exists()) {
                // First run - save as baseline
                logger.info("No baseline found for {}. Saving current screenshot as baseline.", testName);
                ImageIO.write(actualImage, "PNG", baselineFile);
                return true;
            }
            
            // Load baseline image
            BufferedImage baselineImage = ImageIO.read(baselineFile);
            
            // Compare images
            ImageComparison comparison = new ImageComparison(baselineImage, actualImage);
            ImageComparisonResult result = comparison.compareImages();
            
            // Save diff image if there are differences
            if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                String diffPath = DIFF_DIR + "/" + testName + "_diff.png";
                ImageIO.write(result.getResult(), "PNG", new File(diffPath));
                logger.warn("Visual differences found for {}. Diff saved to: {}", testName, diffPath);
            }
            
            // Calculate difference percentage
            double diffPercentage = result.getDifferencePercent() / 100.0;
            logger.info("Visual comparison for {}: {}% difference (threshold: {}%)", 
                       testName, diffPercentage * 100, threshold * 100);
            
            return diffPercentage <= threshold;
            
        } catch (IOException e) {
            logger.error("Failed to perform visual comparison for " + testName, e);
            return false;
        }
    }
    
    /**
     * Takes a screenshot of a specific element and compares it with the baseline
     * @param elementSelector CSS selector for the element
     * @param testName unique name for the test
     * @return true if images match within threshold, false otherwise
     */
    public boolean compareElementScreenshot(String elementSelector, String testName) {
        return compareElementScreenshot(elementSelector, testName, DEFAULT_THRESHOLD);
    }
    
    /**
     * Takes a screenshot of a specific element and compares it with the baseline
     * @param elementSelector CSS selector for the element
     * @param testName unique name for the test
     * @param threshold acceptable difference percentage (0.0 to 1.0)
     * @return true if images match within threshold, false otherwise
     */
    public boolean compareElementScreenshot(String elementSelector, String testName, double threshold) {
        try {
            // Take element screenshot
            Screenshot screenshot = aShot.takeScreenshot(driver, driver.findElement(org.openqa.selenium.By.cssSelector(elementSelector)));
            BufferedImage actualImage = screenshot.getImage();
            
            // Save actual screenshot
            String actualPath = ACTUAL_DIR + "/" + testName + "_element_actual.png";
            ImageIO.write(actualImage, "PNG", new File(actualPath));
            
            // Check if baseline exists
            String baselinePath = BASELINE_DIR + "/" + testName + "_element_baseline.png";
            File baselineFile = new File(baselinePath);
            
            if (!baselineFile.exists()) {
                // First run - save as baseline
                logger.info("No baseline found for element {}. Saving current screenshot as baseline.", testName);
                ImageIO.write(actualImage, "PNG", baselineFile);
                return true;
            }
            
            // Load baseline image
            BufferedImage baselineImage = ImageIO.read(baselineFile);
            
            // Compare images
            ImageComparison comparison = new ImageComparison(baselineImage, actualImage);
            ImageComparisonResult result = comparison.compareImages();
            
            // Save diff image if there are differences
            if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                String diffPath = DIFF_DIR + "/" + testName + "_element_diff.png";
                ImageIO.write(result.getResult(), "PNG", new File(diffPath));
                logger.warn("Visual differences found for element {}. Diff saved to: {}", testName, diffPath);
            }
            
            // Calculate difference percentage
            double diffPercentage = result.getDifferencePercent() / 100.0;
            logger.info("Visual comparison for element {}: {}% difference (threshold: {}%)", 
                       testName, diffPercentage * 100, threshold * 100);
            
            return diffPercentage <= threshold;
            
        } catch (Exception e) {
            logger.error("Failed to perform element visual comparison for " + testName, e);
            return false;
        }
    }
    
    /**
     * Updates the baseline image for a test
     * @param testName unique name for the test
     */
    public void updateBaseline(String testName) {
        try {
            String actualPath = ACTUAL_DIR + "/" + testName + "_actual.png";
            String baselinePath = BASELINE_DIR + "/" + testName + "_baseline.png";
            
            Path actualFile = Paths.get(actualPath);
            Path baselineFile = Paths.get(baselinePath);
            
            if (Files.exists(actualFile)) {
                Files.copy(actualFile, baselineFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Updated baseline for {}", testName);
            } else {
                logger.warn("No actual image found to update baseline for {}", testName);
            }
            
        } catch (IOException e) {
            logger.error("Failed to update baseline for " + testName, e);
        }
    }
    
    /**
     * Cleans up old visual testing artifacts
     * @param daysOld files older than this many days will be deleted
     */
    public static void cleanupOldArtifacts(int daysOld) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);
            
            deleteOldFiles(Paths.get(ACTUAL_DIR), cutoffTime);
            deleteOldFiles(Paths.get(DIFF_DIR), cutoffTime);
            
        } catch (Exception e) {
            logger.error("Failed to cleanup old visual testing artifacts", e);
        }
    }
    
    private static void deleteOldFiles(Path directory, long cutoffTime) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.debug("Deleted old file: {}", path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete old file: {}", path, e);
                        }
                    });
        }
    }
}