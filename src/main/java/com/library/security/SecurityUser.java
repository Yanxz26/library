package com.library.security;

import com.library.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 用户信息封装
 *
 * @author Library Team
 */
@Data
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private SysUser sysUser;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleCode = getRoleCode(sysUser.getRoleId());
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUserAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return sysUser.getStatus() == 1;
    }

    /**
     * 根据角色ID获取角色标识
     */
    private String getRoleCode(Long roleId) {
        if (roleId == 1) {
            return "admin";
        } else if (roleId == 2) {
            return "library";
        } else {
            return "user";
        }
    }
}
