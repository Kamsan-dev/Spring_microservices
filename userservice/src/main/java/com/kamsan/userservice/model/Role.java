package com.kamsan.userservice.model;

import com.kamsan.userservice.sharedkernel.domain.AbstractAuditingEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends AbstractAuditingEntity<Long> {
    @Id
    private Long roleId;
    private UUID rolePublicId;
    private String name;
    private String authority;

    @Override
    public Long getId() {
        return this.roleId;
    }
}
