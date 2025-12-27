Fix Duplicate Instance and Partition Key Bug

1.  **FileInstanceRepository.java**:
    -   Updated `saveInstance` to iterate through the existing list and check for ID matches.
    -   If a match is found, the instance is updated (`set(i, instance)`). If not, it is added.
    -   Fixed `getPartitionKeyFromBlock` to shift block coordinates (`x >> 4`, `z >> 4`) to obtain chunk coordinates, ensuring correct partition mapping.

This prevents the creation of duplicate `Instance` records during updates (like commits) and fixes a critical bug where instances could be saved to the wrong partition file.