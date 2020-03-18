package site.yazago.oauth.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "UserUUIDGenerator")
    @GenericGenerator(name = "UserUUIDGenerator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String username;

    private String email;

    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean accountConfirmed;

    private boolean accountExpired;

    private boolean accountLocked;

    private boolean credentialsExpired;

    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USER2AUTHORITY_GROUP",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "GROUP_ID",
                    referencedColumnName = "ID"))
    @OrderBy
    private Set<AuthorityGroup> groups;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user2authority",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "AUTHORITY_ID",
                    referencedColumnName = "ID"))
    @OrderBy
    private Set<Authority> userAuthorities;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Authority> result = new HashSet<>();
        if (groups != null) {
            groups.stream()
                    .flatMap(authoritiesGroup -> authoritiesGroup.getAuthorities().stream())
                    .forEach(result::add);
        }

        if (userAuthorities != null) {
            result.addAll(userAuthorities);
        }
        return result;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return !isAccountExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return !isCredentialsExpired();
    }
}
