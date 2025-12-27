Add List Instances and TP Command with Tab Completion

1.  **IdunnCommand.java**:
    -   Implemented `TabExecutor` interface.
    -   Added `instances <templatePath>` subcommand: Lists loaded instances for a template.
    -   Added `tp <instanceId>` subcommand: Teleports to an instance (supports partial ID).
    -   Added `onTabComplete`: Provides suggestions for subcommands, template paths, and instance IDs.
    -   Refactored `onCommand` to use `switch-case`.

This enhances the CLI usability significantly.