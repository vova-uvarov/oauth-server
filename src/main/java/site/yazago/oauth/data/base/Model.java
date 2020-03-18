package site.yazago.oauth.data.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Model {

    @CreatedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDateTime insTime;

    @LastModifiedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDateTime modifTime;
}
