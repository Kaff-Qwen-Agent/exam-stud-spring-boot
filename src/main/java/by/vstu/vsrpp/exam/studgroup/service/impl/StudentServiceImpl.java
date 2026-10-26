package by.vstu.vsrpp.exam.studgroup.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.vsrpp.exam.studgroup.dto.StudentRequest;
import by.vstu.vsrpp.exam.studgroup.dto.StudentResponse;
import by.vstu.vsrpp.exam.studgroup.entity.Group;
import by.vstu.vsrpp.exam.studgroup.entity.Student;
import by.vstu.vsrpp.exam.studgroup.mappers.StudentMapper;
import by.vstu.vsrpp.exam.studgroup.repository.GroupRepository;
import by.vstu.vsrpp.exam.studgroup.repository.StudentRepository;
import by.vstu.vsrpp.exam.studgroup.service.StudentService;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private StudentMapper mapper;

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        Student student = mapper.toEntity(request);
        // Группа обязательна: в БД ShifrGruppyi NOT NULL
        student.setGroup(resolveGroup(request.getShifrGruppyi()));
        Student saved = repository.save(student);
        return mapper.toResponse(saved);
    }

    @Override
    public List<StudentResponse> findAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public StudentResponse findById(Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент с номером зачетки " + id + " не найден"));
        return mapper.toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент с номером зачетки " + id + " не найден"));

        mapper.updateEntityFromDto(request, student);
        if (request.getShifrGruppyi() != null) {
            student.setGroup(resolveGroup(request.getShifrGruppyi()));
        }
        Student updated = repository.save(student);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Студент с номером зачетки " + id + " не найден");
        }
        repository.deleteById(id);
    }

    private Group resolveGroup(Integer shifr) {
        if (shifr == null) {
            throw new IllegalArgumentException("Не указана группа студента (shifrGruppyi)");
        }
        return groupRepository.findById(shifr)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + shifr + " не найдена"));
    }
}