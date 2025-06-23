package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.StrategyMapper;
import br.ufscar.glitchdex.repository.StrategyRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service class for managing testing strategies.
 * Provides business logic for creating, retrieving, updating, and deleting strategies.
 */
@Service
@RequiredArgsConstructor
public class StrategyService {

    private static final Logger log = LoggerFactory.getLogger(StrategyService.class);
    private final StrategyRepository strategyRepository;
    private final UserRepository userRepository;
    private final StrategyMapper strategyMapper;
    private final FileStorageService fileStorageService;
    private final ImageProcessingService imageProcessingService;
    private final MessageSource messageSource;

    /**
     * Finds all strategies and returns their DTOs.
     *
     * @return A list of all StrategyDTOs.
     */
    public List<StrategyDTO> findAll() {
        log.info("Finding all strategies");
        return strategyMapper.toStrategyDTOs(strategyRepository.findAll());
    }

    /**
     * Finds a strategy by its ID and returns its DTO.
     *
     * @param id The ID of the strategy.
     * @return The StrategyDTO.
     * @throws ResourceNotFoundException if no strategy is found with the given ID.
     */
    public StrategyDTO findById(Long id) {
        log.info("Finding strategy with id: {}", id);
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.strategy.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));
        return strategyMapper.toStrategyDTO(strategy);
    }

    /**
     * Creates a new strategy without an image.
     *
     * @param strategyRequest The request object with the strategy details.
     * @param creatorDto      The DTO of the user creating the strategy.
     * @return The created StrategyDTO.
     */
    @Transactional
    public StrategyDTO create(StrategyRequest strategyRequest, UserDTO creatorDto) {
        return create(strategyRequest, creatorDto, null);
    }

    /**
     * Creates a new strategy with an optional image.
     *
     * @param strategyRequest The request object with the strategy details.
     * @param creatorDto      The DTO of the user creating the strategy.
     * @param imageFile       The image file for the strategy.
     * @return The created StrategyDTO.
     */
    @Transactional
    public StrategyDTO create(StrategyRequest strategyRequest, UserDTO creatorDto, MultipartFile imageFile) {
        log.info("User {} is creating a new strategy with name: {}", creatorDto.getEmail(), strategyRequest.getName());
        User creator = userRepository.findById(creatorDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found", new Object[]{creatorDto.getId()}, LocaleContextHolder.getLocale())));

        Strategy strategy = new Strategy();
        strategy.setName(strategyRequest.getName());
        strategy.setDescription(strategyRequest.getDescription());
        strategy.setExamples(strategyRequest.getExamples());
        strategy.setTips(strategyRequest.getTips());
        strategy.setCreator(creator);

        if (imageFile != null && !imageFile.isEmpty()) {
            String tempFilename = fileStorageService.store(imageFile);
            strategy.setImageUrl("/files/" + tempFilename);
        }

        Strategy savedStrategy = strategyRepository.save(strategy);
        log.info("Strategy '{}' created successfully with id {}", savedStrategy.getName(), savedStrategy.getId());

        if (imageFile != null && !imageFile.isEmpty()) {
            String tempFilename = savedStrategy.getImageUrl().substring(savedStrategy.getImageUrl().lastIndexOf('/') + 1);
            imageProcessingService.convertToWebpAndUpdateStrategy(savedStrategy.getId(), tempFilename);
        }

        return strategyMapper.toStrategyDTO(savedStrategy);
    }

    /**
     * Updates an existing strategy.
     *
     * @param id              The ID of the strategy to update.
     * @param strategyRequest The request object with the updated strategy details.
     * @param imageFile       The new image file for the strategy.
     * @return The updated StrategyDTO.
     */
    @Transactional
    public StrategyDTO update(Long id, StrategyRequest strategyRequest, MultipartFile imageFile) {
        log.info("Updating strategy with id: {}", id);
        Strategy existingStrategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.strategy.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));

        String oldImageUrl = existingStrategy.getImageUrl();

        existingStrategy.setName(strategyRequest.getName());
        existingStrategy.setDescription(strategyRequest.getDescription());
        existingStrategy.setExamples(strategyRequest.getExamples());
        existingStrategy.setTips(strategyRequest.getTips());


        if (imageFile != null && !imageFile.isEmpty()) {
            String tempFilename = fileStorageService.store(imageFile);
            existingStrategy.setImageUrl("/files/" + tempFilename);
        }

        Strategy updatedStrategy = strategyRepository.save(existingStrategy);
        log.info("Strategy with id {} updated successfully", id);

        if (imageFile != null && !imageFile.isEmpty()) {
            String tempFilename = updatedStrategy.getImageUrl().substring(updatedStrategy.getImageUrl().lastIndexOf('/') + 1);
            imageProcessingService.convertToWebpAndUpdateStrategy(updatedStrategy.getId(), tempFilename);
            if (oldImageUrl != null && !oldImageUrl.isBlank()) {
                String oldFilename = oldImageUrl.substring(oldImageUrl.lastIndexOf('/') + 1);
                fileStorageService.delete(oldFilename);
            }
        }
        return strategyMapper.toStrategyDTO(updatedStrategy);
    }

    /**
     * Deletes a strategy by its ID.
     *
     * @param id The ID of the strategy to delete.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting strategy with id: {}", id);
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.strategy.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));

        String imageUrl = strategy.getImageUrl();
        if (imageUrl != null && !imageUrl.isBlank()) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            fileStorageService.delete(filename);
        }

        strategyRepository.delete(strategy);
        log.info("Strategy with id {} deleted successfully", id);
    }
}