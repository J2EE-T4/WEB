package com.interacthub.security;

import com.interacthub.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String id;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails fromUser(User user) {
        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority(user.getRole()));
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getEmail(), user.getPasswordHash(), auths);
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
