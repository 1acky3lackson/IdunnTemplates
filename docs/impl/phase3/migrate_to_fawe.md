Migrate to FAWE and Fix SimulatePaste

1.  **build.gradle**: Switched from `worldedit-bukkit` to `FastAsyncWorldEdit-Core` and `FastAsyncWorldEdit-Bukkit`.
2.  **DiffCalculator.java**:
    -   Refactored `simulatePaste` to use the correct no-arg `CapturingExtent` constructor.
    -   Removed unused `CustomTransform` class.
    -   Ensured standard `Operations.complete()` usage.
3.  **CapturingExtent.java**: (From previous step) Extends `AbstractDelegateExtent` to prevent FAWE from bypassing it.

This ensures the plugin is compatible with servers running FAWE and that the diff calculator functions correctly.