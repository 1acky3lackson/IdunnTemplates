Fix IdunnCommand Commit Logic

1.  **Updated `handleCommit`**:
    -   Now retrieves `TemplateMetadata` to find the source world and coordinates.
    -   Calculates the region using `anchor` (min) and dimensions.
    -   Recovers the clipboard origin offset from the previous schematic version to prevent shifting.
    -   Captures the content directly from the source world.
2.  **Removed Legacy Methods**:
    -   Deleted `findInstanceAt`, `captureInstance`, and `calculateInstanceRegion` as they are no longer needed for committing.

This aligns the commit process with the requirement to update from the template's source location.