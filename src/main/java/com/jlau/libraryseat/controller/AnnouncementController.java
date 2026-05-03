package com.jlau.libraryseat.controller;

import com.jlau.libraryseat.common.Result;
import com.jlau.libraryseat.entity.Announcement;
import com.jlau.libraryseat.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        return Result.success(announcementService.create(announcement));
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        return Result.success(announcementService.update(id, announcement));
    }

    @PostMapping("/{id}/publish")
    public Result<Announcement> publish(@PathVariable Long id) {
        return Result.success(announcementService.publish(id));
    }

    @PostMapping("/{id}/withdraw")
    public Result<Announcement> withdraw(@PathVariable Long id) {
        return Result.success(announcementService.withdraw(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/published")
    public Result<List<Announcement>> getPublished() {
        return Result.success(announcementService.getPublished());
    }

    @GetMapping("/latest")
    public Result<List<Announcement>> getLatest() {
        return Result.success(announcementService.getLatest());
    }

    @GetMapping
    public Result<Page<Announcement>> getAll(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.getAll(page, size));
    }

    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}