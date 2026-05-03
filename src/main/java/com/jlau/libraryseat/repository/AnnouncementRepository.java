package com.jlau.libraryseat.repository;

import com.jlau.libraryseat.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' ORDER BY a.topFlag DESC, a.publishTime DESC")
    List<Announcement> findPublished();

    Page<Announcement> findAllByOrderByCreateTimeDesc(Pageable pageable);

    List<Announcement> findTop5ByStatusOrderByTopFlagDescPublishTimeDesc(String status);
}