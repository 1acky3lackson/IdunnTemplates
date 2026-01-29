package com.jackyblackson.idunntemplates.core.domain;

import java.util.ArrayList;
import java.util.List;

public class StagedChanges {
    // List of new instances added to this template during the locked session
    private List<Instance> addedInstances = new ArrayList<>();
    
    // List of instance IDs that were removed from this template during the locked session
    private List<String> removedInstanceIds = new ArrayList<>();

    public StagedChanges() {
    }

    public List<Instance> getAddedInstances() {
        if (addedInstances == null) addedInstances = new ArrayList<>();
        return addedInstances;
    }

    public void setAddedInstances(List<Instance> addedInstances) {
        this.addedInstances = addedInstances;
    }

    public List<String> getRemovedInstanceIds() {
        if (removedInstanceIds == null) removedInstanceIds = new ArrayList<>();
        return removedInstanceIds;
    }

    public void setRemovedInstanceIds(List<String> removedInstanceIds) {
        this.removedInstanceIds = removedInstanceIds;
    }
    
    public boolean isEmpty() {
        return getAddedInstances().isEmpty() && getRemovedInstanceIds().isEmpty();
    }
    
    public void clear() {
        getAddedInstances().clear();
        getRemovedInstanceIds().clear();
    }
}
