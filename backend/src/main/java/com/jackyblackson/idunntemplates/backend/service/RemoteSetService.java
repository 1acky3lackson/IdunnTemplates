package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.Namespace;
import com.jackyblackson.idunntemplates.backend.domain.RemoteSet;
import com.jackyblackson.idunntemplates.backend.domain.RemoteSetSource;
import com.jackyblackson.idunntemplates.backend.store.repository.NamespaceRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.RemoteSetRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.RemoteSetSourceRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RemoteSetService {

    private final RemoteSetRepository remoteSetRepository;
    private final NamespaceRepository namespaceRepository;
    private final RemoteSetSourceRepository remoteSetSourceRepository;
    private final TemplateRepository templateRepository;

    @Autowired
    public RemoteSetService(RemoteSetRepository remoteSetRepository,
                            NamespaceRepository namespaceRepository,
                            RemoteSetSourceRepository remoteSetSourceRepository,
                            TemplateRepository templateRepository) {
        this.remoteSetRepository = remoteSetRepository;
        this.namespaceRepository = namespaceRepository;
        this.remoteSetSourceRepository = remoteSetSourceRepository;
        this.templateRepository = templateRepository;
    }

    @Transactional
    public RemoteSet createSet(String name, String namespaceName, UUID creatorUuid, String creatorUsername) {
        Namespace namespace = namespaceRepository.findById(namespaceName).orElse(null);

        if (namespace == null) {
            String expectedNamespace = "player." + creatorUsername;
            if (namespaceName.equals(expectedNamespace)) {
                namespace = new Namespace(namespaceName, creatorUuid);
                namespaceRepository.save(namespace);
            } else {
                throw new IllegalArgumentException("Namespace not found and cannot be auto-created: " + namespaceName);
            }
        }

        RemoteSet remoteSet = new RemoteSet(name, creatorUuid, namespace);
        return remoteSetRepository.save(remoteSet);
    }

    public Optional<RemoteSet> getSetById(Long id) {
        return remoteSetRepository.findById(id);
    }

    @Transactional
    public void deleteSet(Long id) {
        remoteSetRepository.deleteById(id);
    }

    @Transactional
    public RemoteSetSource addSource(Long setId, RemoteSetSource.SourceType type, String path, Long targetSetId, Double weight) {
        RemoteSet remoteSet = remoteSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("RemoteSet not found: " + setId));

        RemoteSetSource source;
        if (type == RemoteSetSource.SourceType.PATH) {
            source = new RemoteSetSource(remoteSet, weight, path);
        } else if (type == RemoteSetSource.SourceType.REMOTE_SET) {
            if (targetSetId == null) {
                throw new IllegalArgumentException("Target Set ID required for REMOTE_SET source type");
            }
            RemoteSet targetSet = remoteSetRepository.findById(targetSetId)
                    .orElseThrow(() -> new IllegalArgumentException("Target RemoteSet not found: " + targetSetId));

            if (targetSet.getId().equals(setId)) {
                throw new IllegalArgumentException("Cannot add self as source");
            }

            source = new RemoteSetSource(remoteSet, weight, targetSet);
        } else {
            throw new IllegalArgumentException("Invalid source type");
        }

        return remoteSetSourceRepository.save(source);
    }

    @Transactional
    public void removeSource(Long sourceId) {
        remoteSetSourceRepository.deleteById(sourceId);
    }

    @Transactional
    public RemoteSetSource updateSourceWeight(Long sourceId, Double weight) {
        RemoteSetSource source = remoteSetSourceRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        source.setWeight(weight);
        return remoteSetSourceRepository.save(source);
    }

    public Optional<RemoteSetSource> getSourceById(Long id) {
        return remoteSetSourceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<RemoteSetSource> getSources(Long setId) {
        RemoteSet remoteSet = remoteSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("RemoteSet not found: " + setId));
        List<RemoteSetSource> sources = remoteSet.getSources();
        sources.size();
        return sources;
    }

    @Transactional(readOnly = true)
    public Optional<Template> getRandomTemplate(Long setId) {
        RemoteSet remoteSet = remoteSetRepository.findById(setId).orElse(null);
        if (remoteSet == null) return Optional.empty();

        return getRandomTemplateFromSet(remoteSet, new HashSet<>());
    }

    private Optional<Template> getRandomTemplateFromSet(RemoteSet set, Set<Long> visitedSets) {
        if (visitedSets.contains(set.getId())) return Optional.empty();
        visitedSets.add(set.getId());

        List<RemoteSetSource> sources = set.getSources();
        if (sources.isEmpty()) return Optional.empty();

        double totalWeight = sources.stream().mapToDouble(RemoteSetSource::getWeight).sum();
        if (totalWeight <= 0) return Optional.empty();

        double randomValue = Math.random() * totalWeight;
        double currentWeight = 0;
        RemoteSetSource selectedSource = null;
        for (RemoteSetSource source : sources) {
            currentWeight += source.getWeight();
            if (randomValue <= currentWeight) {
                selectedSource = source;
                break;
            }
        }

        if (selectedSource == null && !sources.isEmpty()) selectedSource = sources.get(sources.size() - 1);

        if (selectedSource != null) {
            if (selectedSource.getType() == RemoteSetSource.SourceType.PATH) {
                String path = selectedSource.getPath();
                if (path == null) return Optional.empty();

                long total = templateRepository.findByPathStartingWith(path, PageRequest.of(0, 1)).getTotalElements();
                if (total == 0) return Optional.empty();

                int idx = (int) (Math.random() * total);
                return templateRepository.findByPathStartingWith(path, PageRequest.of(idx, 1)).stream().findFirst();

            } else if (selectedSource.getType() == RemoteSetSource.SourceType.REMOTE_SET) {
                if (selectedSource.getTargetSet() != null) {
                    return getRandomTemplateFromSet(selectedSource.getTargetSet(), visitedSets);
                }
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Template> getPreviewTemplates(Long setId) {
        RemoteSet remoteSet = remoteSetRepository.findById(setId).orElse(null);
        if (remoteSet == null) return Collections.emptyList();

        long seed = (remoteSet.getName() + remoteSet.getNamespace().getName()).hashCode();
        seed += remoteSet.getSources().stream().mapToLong(RemoteSetSource::getId).sum();

        Random random = new Random(seed);
        List<Template> pool = new ArrayList<>();

        collectPreviewPool(remoteSet, pool, new HashSet<>(), 5);

        Collections.shuffle(pool, random);
        return pool.stream().limit(8).collect(Collectors.toList());
    }

    private void collectPreviewPool(RemoteSet set, List<Template> pool, Set<Long> visited, int limitPerSource) {
         if (visited.contains(set.getId())) return;
         visited.add(set.getId());

         for (RemoteSetSource source : set.getSources()) {
             if (source.getType() == RemoteSetSource.SourceType.PATH) {
                 String path = source.getPath();
                 if (path != null) {
                     pool.addAll(templateRepository.findByPathStartingWith(path, PageRequest.of(0, limitPerSource)).getContent());
                 }
             } else if (source.getType() == RemoteSetSource.SourceType.REMOTE_SET) {
                 if (source.getTargetSet() != null) {
                     collectPreviewPool(source.getTargetSet(), pool, visited, limitPerSource);
                 }
             }
         }
    }

    @Transactional(readOnly = true)
    public Map<String, List<RemoteSet>> getDependents(Long setId) {
        Map<String, List<RemoteSet>> result = new HashMap<>();
        List<RemoteSet> direct = new ArrayList<>();
        List<RemoteSet> indirect = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        List<RemoteSetSource> sources = remoteSetSourceRepository.findByTargetSet_Id(setId);
        for (RemoteSetSource s : sources) {
            if (s.getRemoteSet() != null && visited.add(s.getRemoteSet().getId())) {
                direct.add(s.getRemoteSet());
            }
        }

        Queue<Long> queue = new LinkedList<>();
        direct.forEach(rs -> queue.add(rs.getId()));

        while(!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<RemoteSetSource> parentSources = remoteSetSourceRepository.findByTargetSet_Id(currentId);
            for (RemoteSetSource ps : parentSources) {
                RemoteSet parent = ps.getRemoteSet();
                if (parent != null && visited.add(parent.getId())) {
                    indirect.add(parent);
                    queue.add(parent.getId());
                }
            }
        }

        result.put("direct", direct);
        result.put("indirect", indirect);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<RemoteSet> searchSets(String name, String namespace, Pageable pageable) {
        return remoteSetRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (namespace != null && !namespace.isEmpty()) {
                predicates.add(cb.like(root.get("namespace").get("name"), "%" + namespace + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}
