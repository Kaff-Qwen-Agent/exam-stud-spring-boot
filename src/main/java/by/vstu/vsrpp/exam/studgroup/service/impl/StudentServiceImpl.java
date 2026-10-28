package by.vstu.vsrpp.exam.studgroup.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.vsrpp.exam.studgroup.dto.StudentGroupInfo;
import by.vstu.vsrpp.exam.studgroup.dto.StudentRequest;
import by.vstu.vsrpp.exam.studgroup.dto.StudentResponse;
import by.vstu.vsrpp.exam.studgroup.entity.Group;
import by.vstu.vsrpp.exam.studgroup.entity.Student;
import by.vstu.vsrpp.exam.studgroup.exception.DuplicateEntityException;
import by.vstu.vsrpp.exam.studgroup.exception.InvalidStudentNameException;
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
        // Задание 4: имя (фамилия/имя/отчество) не должно быть пустым и не должно
        // содержать пробелов; иначе запись не сохраняется и возвращается
        // сообщение "Нужно указать имя"
        validateNames(request);
        // nomerZachetki задаётся клиентом; POST по существующему id — конфликт,
        // а не молчаливое обновление (иначе save() делает merge)
        if (repository.existsById(request.getNomerZachetki())) {
            throw new DuplicateEntityException(
                    "Студент с номером зачетки " + request.getNomerZachetki() + " уже существует");
        }
        Student student = mapper.toEntity(request);
        // Группа обязательна: в БД ShifrGruppyi NOT NULL
        student.setGroup(resolveGroup(request.getShifrGruppyi()));
        Student saved = repository.save(student);
        return mapper.toResponse(saved);
    }

    // Проверка ФИО: null/пустое значение или наличие пробелов — ошибка валидации
    private void validateNames(StudentRequest request) {
        checkNamePart(request.getFamiliya());
        checkNamePart(request.getImya());
        checkNamePart(request.getOtchestvo());
    }

    private void checkNamePart(String value) {
        if (value == null || value.trim().isEmpty() || value.contains(" ")) {
            throw new InvalidStudentNameException("Нужно указать имя");
        }
    }

    @Override
    public Page<StudentResponse> findAll(Long from, Long to, Pageable pageable) {
        Page<Student> page;
        if (from != null || to != null) {
            if (from == null || to == null) {
                throw new IllegalArgumentException("Диапазон задан не полностью: нужны from и to одновременно");
            }
            if (from > to) {
                throw new IllegalArgumentException("Начало диапазона (" + from + ") больше конца (" + to + ")");
            }
            page = repository.findByNomerZachetkiBetween(from, to, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return new PageImpl<>(mapper.toResponseList(page.getContent()), pageable, page.getTotalElements());
    }

    @Override
    public StudentResponse findById(Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент с номером зачетки " + id + " не найден"));
        return mapper.toResponse(student);
    }

    // Задание 3: данные о группе указанного студента
    @Override
    public StudentGroupInfo findGroupOfStudent(Long nomerZachetki) {
        Student student = repository.findById(nomerZachetki)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Студент с номером зачетки " + nomerZachetki + " не найден"));
        Group group = student.getGroup();
        return new StudentGroupInfo(student.getFamiliya(),
                group != null ? group.getNazvanie() : null);
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        validateNames(request);
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