package site.yazago.oauth.data.storage;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.yazago.oauth.data.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, UUID>, JpaSpecificationExecutor<User> {
    User findByUsername(String userName);

    @Query(value = "select distinct u.* from users u where u.id IN (" +
            " select u2a.user_id from user2authority u2a" +
            " join authority a on a.id = u2a.authority_id and a.name=:authority) or u.id IN (" +
            " select u2g.user_id from user2authority_group u2g" +
            " join authority_group ag on ag.id=u2g.group_id" +
            " join auth_group2authority  g2a on g2a.group_id=ag.id" +
            " join authority a on a.id=g2a.authority_id and a.name=:authority)", nativeQuery = true)
    List<User> findByAuthority(@Param("authority") String authority);
}
