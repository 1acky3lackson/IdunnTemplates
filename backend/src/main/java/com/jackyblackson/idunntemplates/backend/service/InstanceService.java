package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.store.repository.InstanceRepository;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;

    @Autowired
    public InstanceService(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Instance> getInstanceById(String id) {
        return instanceRepository.findById(id);
    }
}
