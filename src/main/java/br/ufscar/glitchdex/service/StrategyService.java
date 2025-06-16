package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.StrategyMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.repository.StrategyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private final ProjectRepository projectRepository;
    private final StrategyMapper strategyMapper;

    public List<StrategyDTO> findAll() {
        return strategyMapper.toStrategyDTOs(strategyRepository.findAll());
    }

    public List<StrategyDTO> findByProject(Project project) {
        List<Strategy> strategies = strategyRepository.findByProject(project);
        return strategyMapper.toStrategyDTOs(strategies);
    }

    public StrategyDTO findById(Long id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + id));
        return strategyMapper.toStrategyDTO(strategy);
    }

    @Transactional
    public StrategyDTO create(StrategyRequest strategyRequest, User creator) {
        Project project = projectRepository.findById(strategyRequest.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + strategyRequest.getProjectId()));

        Strategy strategy = new Strategy();
        strategy.setName(strategyRequest.getName());
        strategy.setDescription(strategyRequest.getDescription());
        strategy.setProject(project);
        strategy.setCreator(creator);

        Strategy savedStrategy = strategyRepository.save(strategy);
        return strategyMapper.toStrategyDTO(savedStrategy);
    }

    @Transactional
    public StrategyDTO update(Long id, StrategyRequest strategyRequest) {
        Strategy existingStrategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + id));

        Project project = projectRepository.findById(strategyRequest.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + strategyRequest.getProjectId()));

        existingStrategy.setName(strategyRequest.getName());
        existingStrategy.setDescription(strategyRequest.getDescription());
        existingStrategy.setProject(project);

        Strategy updatedStrategy = strategyRepository.save(existingStrategy);
        return strategyMapper.toStrategyDTO(updatedStrategy);
    }

    @Transactional
    public void delete(Long id) {
        if (!strategyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Strategy not found with id: " + id);
        }
        strategyRepository.deleteById(id);
    }
}