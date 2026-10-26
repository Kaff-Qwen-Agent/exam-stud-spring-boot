package by.vstu.vsrpp.exam.studgroup.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import by.vstu.vsrpp.exam.studgroup.dto.AddStudentsRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupResponse;
import by.vstu.vsrpp.exam.studgroup.service.GroupService;

@RestController
@RequestMapping(value = "/api/group", produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupController {

    @Autowired
    private GroupService service;

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getById(@PathVariable int id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> update(@PathVariable int id, @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("addstudents/{groupId}")
    public ResponseEntity<GroupResponse> addStudents(
            @PathVariable int groupId,
            @Valid @RequestBody AddStudentsRequest request) {
        return ResponseEntity.ok(service.addStudents(groupId, request));
    }
}