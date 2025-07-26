package lt.svaskevicius.videometa.service.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lt.svaskevicius.videometa.dal.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CurrentUserDetails implements UserDetails {

  @Getter
  private final String id;
  private final String username;
  private final String password;
  private final boolean active;
  private final List<GrantedAuthority> authorities;

  public CurrentUserDetails(final User user) {
    this.id = user.getId().toString();
    this.username = user.getUsername();
    this.password = user.getPassword();
    this.active = user.isActive();

    this.authorities = Arrays.stream(user.getAuthorities().split(","))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.username;
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
    return this.active;
  }
}
