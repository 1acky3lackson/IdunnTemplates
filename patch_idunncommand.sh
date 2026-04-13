cat << 'INNER_EOF' > patch.diff
--- plugin/src/main/java/com/jackyblackson/idunntemplates/command/IdunnCommand.java
+++ plugin/src/main/java/com/jackyblackson/idunntemplates/command/IdunnCommand.java
@@ -74,6 +74,7 @@
         subCommands.put("commit", new SmartCommitCommand(templateManager));
         subCommands.put("resize", new ResizeCommand(resizeManager, resizeConfigManager, languageManager));
         subCommands.put("migrate", new com.jackyblackson.idunntemplates.command.sub.internal.MigrateCommand());
+        subCommands.put("auth", new AuthCommand());
     }

     @Override
INNER_EOF
patch plugin/src/main/java/com/jackyblackson/idunntemplates/command/IdunnCommand.java < patch.diff
