package site.yazago.oauth.data.storage;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import site.yazago.oauth.data.Authority;

import java.util.Collection;
import java.util.Set;

@Repository
public interface AuthorityRepository extends PagingAndSortingRepository<Authority, Long> {
    Set<Authority> findAlByIdIn(Collection<Long> ids);

}
