package com.bid.entity;

import lombok.*;

import jakarta.persistence.*; 
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "users")
public class Role {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @ManyToMany(mappedBy = "userRoles", fetch = FetchType.LAZY) // User 엔티티의 userRoles 필드에 의해 매핑
    private Set<User> users = new HashSet<>();
    
}
