package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.repository.StrategyRepository;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import com.sksamuel.scrimage.webp.Gif2WebpWriter;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Service for processing images asynchronously.
 * Provides functionality to convert uploaded images to the WebP format.
 */
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);
    private final StrategyRepository strategyRepository;
    private final FileStorageService fileStorageService;


    /**
     * Asynchronously converts an uploaded image to WebP format and updates the corresponding strategy.
     * Handles both static images and animated GIFs.
     *
     * @param strategyId        The ID of the strategy to update with the new image URL.
     * @param temporaryFilename The filename of the temporary image to be converted.
     */
    @Async
    @Transactional
    public void convertToWebpAndUpdateStrategy(Long strategyId, String temporaryFilename) {
        logger.info("Starting WebP conversion for strategy {} and file {}", strategyId, temporaryFilename);

        try {
            Strategy strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Strategy not found for image processing: " + strategyId));

            Path tempFile = fileStorageService.loadAsResource(temporaryFilename).getFile().toPath();

            String webpFilename = UUID.randomUUID() + ".webp";
            Path webpFile = tempFile.getParent().resolve(webpFilename);

            if (temporaryFilename.toLowerCase().endsWith(".gif")) {
                AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(tempFile.toFile()));
                gif.output(new Gif2WebpWriter().withQ(75), webpFile.toFile());
                logger.info("Successfully converted animated GIF {} to animated WebP {}", temporaryFilename, webpFilename);
            } else {
                ImmutableImage.loader().fromPath(tempFile)
                        .output(new WebpWriter().withQ(85), webpFile.toFile());
                logger.info("Successfully converted {} to {}", temporaryFilename, webpFilename);
            }

            strategy.setImageUrl("/files/" + webpFilename);
            strategyRepository.save(strategy);

            Files.delete(tempFile);
            logger.info("Deleted temporary file: {}", temporaryFilename);

        } catch (IOException e) {
            logger.error("Failed to process and convert image for strategy " + strategyId, e);
        }
    }
}