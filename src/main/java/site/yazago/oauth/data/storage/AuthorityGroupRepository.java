package site.yazago.oauth.data.storage;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import site.yazago.oauth.data.AuthorityGroup;

@Repository
public interface AuthorityGroupRepository extends PagingAndSortingRepository<AuthorityGroup, Long> {
}
