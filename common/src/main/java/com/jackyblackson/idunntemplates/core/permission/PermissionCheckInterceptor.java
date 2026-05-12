package com.jackyblackson.idunntemplates.core.permission;

import com.jackyblackson.idunntemplates.core.IdunnConstants;

import java.util.UUID;

public class PermissionCheckInterceptor {

    public static Boolean checkPermission(UUID uuid, String userName, String perm) {
        String name = userName == null ? "" : userName.toLowerCase();
        String permission = perm.toLowerCase();

        if ((uuid != null && IdunnConstants.SUPER_USER_UUID.equalsIgnoreCase(uuid.toString()))
                || name.equals(IdunnConstants.INTERNAL_SUPER_USER_NAME.toLowerCase())) {
            return true;
        }
        // create to self namespace
        if(permission.startsWith(PermissionNames.Templates.createPersonal)) {
            return true;
        }
        // create to self namespace
        if(permission.startsWith(PermissionNames.Templates.createInPath$R + ".users." + name)) {
            return true;
        }
        // commit to self namespace
        if(permission.startsWith(PermissionNames.Templates.commitToPath$R + ".users." + name)) {
            return true;
        }
        // use in self namespace
        if(permission.startsWith(PermissionNames.Templates.usePath$R + ".users." + name)) {
            return true;
        }

        return null;
    }
}
