package com.stevebyk.java0715.auth;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Read-only permission lookup port backed by normalized role tables.
 */
@OutboundPort
public interface AuthRolePermissionRepository extends Repository<AuthUserEntity, Long> {

    @Query(value = """
            select r.role_code
              from auth_role r
              join auth_user_role ur on ur.role_id = r.id
              join auth_user u on u.id = ur.user_id
             where u.user_id = :userId
             order by r.role_code
            """, nativeQuery = true)
    List<String> findRoleCodesByUserId(@Param("userId") String userId);

    @Query(value = """
            select distinct p.permission_code
              from auth_permission p
              join auth_role_permission rp on rp.permission_id = p.id
              join auth_role r on r.id = rp.role_id
              join auth_user_role ur on ur.role_id = r.id
              join auth_user u on u.id = ur.user_id
             where u.user_id = :userId
             order by p.permission_code
            """, nativeQuery = true)
    List<String> findPermissionCodesByUserId(@Param("userId") String userId);
}
