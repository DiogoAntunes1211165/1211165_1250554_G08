package pt.psoft.g1.psoftg1.usermanagement.model.nonrelational;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.NameDocument;
import pt.psoft.g1.psoftg1.usermanagement.model.Password;
import pt.psoft.g1.psoftg1.usermanagement.model.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
public class UserDocument implements UserDetails {

    private static final long serialVersionUID = 1L;

    // database primary key (Mongo usa ObjectId como string)
    @Id
    @Getter
    @Setter
    private String id;

    // optimistic lock concurrency control
    @Version
    @Getter
    @Setter
    private Long version;

    // auditing info
    @CreatedDate
    @Getter
    @Setter
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Getter
    @Setter
    @Field("modified_at")
    private LocalDateTime modifiedAt;

    @CreatedBy
    @Getter
    @Setter
    @Field("created_by")
    private String createdBy;

    @LastModifiedBy
    @Getter
    @Setter
    @Field("modified_by")
    private String modifiedBy;

    @Setter
    @Getter
    private boolean enabled = true;

    // username (email) com validação e índice único
    @Email
    @NotNull
    @NotBlank
    @Indexed(unique = true)
    @Setter
    @Getter
    @Field("username")
    private String username;

    @NotNull
    @NotBlank
    @Getter
    @Field("password")
    private String password;

    @Getter
    @Field("name")
    private NameDocument name;

    @Getter
    @Field("authorities")
    private final Set<Role> authorities = new HashSet<>();

    protected UserDocument() {
        // usado pelo Spring Data
    }

    public UserDocument(final String username, final String password) {
        this.username = username;
        setPassword(password);
    }

    public static UserDocument newUser(final String username, final String password, final String name) {
        final var u = new UserDocument(username, password);
        u.setName(name);
        return u;
    }

    public static UserDocument newUser(final String username, final String password, final String name, final String role) {
        final var u = new UserDocument(username, password);
        u.setName(name);
        u.addAuthority(new Role(role));
        return u;
    }

    public void addAuthority(final Role r) {
        authorities.add(r);
    }

    public void setName(String name) {
        this.name = new NameDocument(name);
    }

    public void setPassword(String password) {
        // valida formato antes de encriptar
        Password passwordCheck = new Password(password);
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.password = passwordEncoder.encode(password);
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }
}
