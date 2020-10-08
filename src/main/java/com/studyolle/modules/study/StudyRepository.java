package com.studyolle.modules.study;

import com.studyolle.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension{

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"})
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(attributePaths = {"managers"})
    Study findStudyWithMangersByPath(String path);

    @EntityGraph(attributePaths = {"members"})
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones"})
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"managers", "members"})
    Study findStudyWithMangersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"tags", "zones"})
    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDatetimeDesc(boolean published, boolean closed);

    Study[] findFirst5ByManagersContainingAndClosedOrderByPublishedDatetimeDesc(Account account, boolean closed);

    Study findFirst5ByMembersContainingAndClosedOrderByPublishedDatetimeDesc(Account account, boolean closed);
}
