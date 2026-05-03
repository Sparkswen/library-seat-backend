package com.jlau.libraryseat.service;

import com.jlau.libraryseat.entity.Announcement;
import com.jlau.libraryseat.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    public Announcement create(Announcement announcement) {
        announcement.setStatus("DRAFT");
        return announcementRepository.save(announcement);
    }

    public Announcement publish(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        a.setStatus("PUBLISHED");
        a.setPublishTime(LocalDateTime.now());
        return announcementRepository.save(a);
    }

    public Announcement withdraw(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        a.setStatus("WITHDRAWN");
        return announcementRepository.save(a);
    }

    public Announcement update(Long id, Announcement announcement) {
        Announcement existing = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        existing.setType(announcement.getType());
        existing.setTopFlag(announcement.getTopFlag());
        return announcementRepository.save(existing);
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }

    public List<Announcement> getPublished() {
        return announcementRepository.findPublished();
    }

    public Page<Announcement> getAll(int page, int size) {
        return announcementRepository.findAllByOrderByCreateTimeDesc(PageRequest.of(page, size));
    }

    public Announcement getById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
    }

    public List<Announcement> getLatest() {
        return announcementRepository.findTop5ByStatusOrderByTopFlagDescPublishTimeDesc("PUBLISHED");
    }
}