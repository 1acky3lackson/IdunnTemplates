cat << 'INNER_EOF' > patch.diff
--- plugin/src/main/java/com/jackyblackson/idunntemplates/command/sub/AuthCommand.java
+++ plugin/src/main/java/com/jackyblackson/idunntemplates/command/sub/AuthCommand.java
@@ -4,6 +4,7 @@
 import com.google.gson.JsonObject;
 import com.jackyblackson.idunntemplates.IdunnTemplates;
 import com.jackyblackson.idunntemplates.core.util.MessageUtil;
+import com.jackyblackson.idunntemplates.command.IdunnSubCommand;
 import org.bukkit.entity.Player;

 import java.net.URI;
INNER_EOF
patch plugin/src/main/java/com/jackyblackson/idunntemplates/command/sub/AuthCommand.java < patch.diff
