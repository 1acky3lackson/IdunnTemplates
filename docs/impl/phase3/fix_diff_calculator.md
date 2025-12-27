Fix for DiffCalculator returning 0 blocks

1.  **CapturingExtent.java**:
    -   Overridden `getMinimumPoint()` and `getMaximumPoint()` to return infinite bounds (`MIN_VALUE` / `MAX_VALUE`).
    -   This prevents the paste operation from clipping all blocks due to `NullExtent`'s default empty bounds.

2.  **DiffCalculator.java**:
    -   Added logging to `simulatePaste` to verify clipboard volume and bounds.

These changes ensure the simulated paste operation actually writes blocks to the `CapturingExtent` map.