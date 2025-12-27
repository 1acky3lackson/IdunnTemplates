Fix DiffCalculator Empty Map (CapturingExtent)

1.  **CapturingExtent.java**:
    -   Changed inheritance from `NullExtent` to `AbstractDelegateExtent`.
    -   Wrapped a `NullExtent` in the constructor.
    -   This prevents WorldEdit's `PasteBuilder` from optimizing away the paste operation (which it does for `NullExtent` targets), ensuring `setBlock` is actually called and blocks are captured.

This resolves the issue where `simulatePaste` resulted in 0 blocks despite valid inputs.