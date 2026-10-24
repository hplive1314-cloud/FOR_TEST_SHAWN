package com.qicheng.workspace.auth;

import com.qicheng.workspace.user.AppUser;

public record AppPrincipal(Long id, String username, String displayName) {
    public static AppPrincipal from(AppUser user) {
        return new AppPrincipal(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
