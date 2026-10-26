package by.vstu.vsrpp.exam.studgroup.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import by.vstu.vsrpp.exam.studgroup.dto.StudentRequest;
import by.vstu.vsrpp.exam.studgroup.dto.StudentResponse;

public interface StudentService {
    StudentResponse create(StudentRequest request);

    Page<StudentResponse> findAll(Long from, Long to, Pageable pageable);

    StudentResponse findById(Long id);
    StudentResponse update(Long id, StudentRequest request);
    void delete(Long id);
}