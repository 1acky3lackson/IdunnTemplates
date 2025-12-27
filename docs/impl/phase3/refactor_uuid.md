Refactor Template-Instance Relationship to UUID

1.  **Template.java**: Added `path` field.
2.  **Instance.java**: Changed `templatePath` (String) to `templateId` (UUID).
3.  **FileTemplateStorage.java**: Populates `Template.path`.
4.  **TemplateManager.java**: Caches by UUID and User-Friendly Path. Added `reloadTemplates`.
5.  **InstanceManager.java**: `placeInstance` uses `Template` object and ID.
6.  **IdunnCommand.java**: Added `/idunn reload`. Updated Place/Commit to use UUID logic.

**Note:** Existing `Instance` JSON files are incompatible and require manual migration (replace `templatePath` string with `templateId` UUID) or deletion.