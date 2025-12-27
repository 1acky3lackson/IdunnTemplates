Implement Pre-generated Schematic Variations

1.  **FileTemplateStorage.java**:
    -   `saveTemplateVersion` now generates 16 variations (Rot 0/90/180/270 * FlipX T/F * FlipZ T/F).
    -   Saves variations to `variations/<versionId>_<rot>_<flipX>_<flipZ>.schem`.
2.  **InstanceManager.java**:
    -   `placeInstance` loads the exact pre-transformed schematic file.
    -   Removed runtime `AffineTransform` application logic.
3.  **TemplateUpdater.java**:
    -   `updateSingleInstance` loads the corresponding variations for diff calculation.
    -   Uses `loadVariationClipboard` with on-the-fly fallback for backward compatibility.
    -   Passes `Identity` transform to `DiffCalculator`.

This change simplifies runtime logic by handling transformations at save time.