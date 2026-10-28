package by.vstu.vsrpp.exam.studgroup.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.vsrpp.exam.studgroup.dto.AddStudentsRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupResponse;
import by.vstu.vsrpp.exam.studgroup.dto.GroupStatusStatistics;
import by.vstu.vsrpp.exam.studgroup.dto.GroupStudentCount;
import by.vstu.vsrpp.exam.studgroup.entity.Group;
import by.vstu.vsrpp.exam.studgroup.entity.Student;
import by.vstu.vsrpp.exam.studgroup.exception.DuplicateEntityException;
import by.vstu.vsrpp.exam.studgroup.exception.GroupValidationException;
import by.vstu.vsrpp.exam.studgroup.mappers.GroupMapper;
import by.vstu.vsrpp.exam.studgroup.repository.GroupRepository;
import by.vstu.vsrpp.exam.studgroup.repository.StudentRepository;
import by.vstu.vsrpp.exam.studgroup.service.GroupService;

@Service
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private GroupMapper groupMapper;

    @Override
    @Transactional
    public GroupResponse create(GroupRequest request) {
        // Новая группа всегда создаётся без студентов, поэтому ограничение
        // @ValidStudentCount(min = 1) здесь не проверяется: студенты
        // добавляются позже через addStudents, где и проходит валидация.
        checkNazvanieFree(request.getNazvanie(), null);
        Group saved = repository.save(groupMapper.toEntity(request));
        return groupMapper.toResponse(saved);
    }

    @Override
    public List<GroupResponse> findAll() {
        return groupMapper.toResponseList(repository.findAll());
    }

    @Override
    public GroupResponse findById(int id) {
        Group group = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + id + " не найдена"));
        return groupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public GroupResponse update(int id, GroupRequest request) {
        Group group = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + id + " не найдена"));

        // Обновляем поля существующей сущности (количество студентов не меняется)
        checkNazvanieFree(request.getNazvanie(), id);
        groupMapper.updateEntityFromDto(request, group);

        Group updated = repository.save(group);
        return groupMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Группа с id " + id + " не найдена");
        }
        repository.deleteById(id);
    }

    // Задание 5: перед удалением группы переносим её студентов в другую группу.
    @Override
    @Transactional
    public void deleteWithTransfer(int groupId, int targetGroupId) {
        Group group = repository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + groupId + " не найдена"));
        Group target = repository.findById(targetGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + targetGroupId + " не найдена"));
        if (groupId == targetGroupId) {
            throw new IllegalArgumentException("Нельзя перенести студентов в ту же группу, что удаляется");
        }

        List<Student> students = studentRepository.findByGroupShifr(groupId);
        if (!students.isEmpty()) {
            // Временное добавление для проверки, что целевая группа вместит переносимых
            // студентов (ограничение @ValidStudentCount на сущности Group)
            List<Student> tempStudents = new ArrayList<>(
                    target.getStudents() != null ? target.getStudents() : Collections.<Student>emptyList());
            tempStudents.addAll(students);
            List<Student> originalTargetStudents = target.getStudents();
            target.setStudents(tempStudents);
            try {
                Set<ConstraintViolation<Group>> combinedViolations = validator.validate(target);
                if (!combinedViolations.isEmpty()) {
                    throw new GroupValidationException(combinedViolations.stream()
                            .map(ConstraintViolation::getMessage).collect(Collectors.toList()));
                }
            } finally {
                target.setStudents(originalTargetStudents);
            }
            for (Student student : students) {
                student.setGroup(target);
            }
            studentRepository.saveAll(students);
        }
        repository.delete(group);
    }

    @Override
    @Transactional
    public GroupResponse addStudents(int groupId, AddStudentsRequest request) {
        Group group = repository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + groupId + " не найдена"));

        List<Student> newStudents = groupMapper.toEntityList(request.getStudents());

        // Временное добавление для валидации
        List<Student> tempStudents = new ArrayList<>(group.getStudents() != null ? group.getStudents() : Collections.emptyList());
        tempStudents.addAll(newStudents);
        group.setStudents(tempStudents);

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        if (!violations.isEmpty()) {
            throw new GroupValidationException(
                violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList())
            );
        }
        newStudents.forEach(student -> student.setGroup(group));
        studentRepository.saveAll(newStudents);
        Group saved = repository.save(group);
        return groupMapper.toResponse(saved);
    }

    @Override
    public List<GroupStatusStatistics> findStatusStatistics() {        // Все группы, включая пустые — у них пустая карта статусов
        Map<Integer, GroupStatusStatistics> byShifr = new LinkedHashMap<>();
        for (Group group : repository.findAll()) {
            byShifr.put(group.getShifr(), new GroupStatusStatistics(group.getShifr(), group.getNazvanie()));
        }
        for (Object[] row : repository.countStudentsByGroupAndStatus()) {
            Integer shifr = (Integer) row[0];
            String nazvanie = (String) row[1];
            String status = row[2] != null ? (String) row[2] : "unknown";
            Long count = (Long) row[3];
            GroupStatusStatistics stats = byShifr.get(shifr);
            if (stats == null) {
                stats = new GroupStatusStatistics(shifr, nazvanie);
                byShifr.put(shifr, stats);
            }
            stats.addCount(status, count);
        }
        return new ArrayList<>(byShifr.values());
    }

    // Задание 1: группы, в которых зарегистрировано менее 2 студентов
    @Override
    public List<GroupStudentCount> findGroupsWithFewerStudentsThan(long maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("Количество студентов должно быть неотрицательным");
        }
        return toGroupStudentCounts(repository.findGroupsWithStudentCountLessThan(maxCount));
    }

    // Задание 2: группы, созданные более года назад (по statusDate)
    @Override
    public List<GroupStudentCount> findGroupsOlderThanOneYear() {
        LocalDate cutoff = LocalDate.now().minusYears(1);
        return toGroupStudentCounts(repository.findGroupsCreatedBefore(cutoff));
    }

    private List<GroupStudentCount> toGroupStudentCounts(List<Object[]> rows) {
        List<GroupStudentCount> result = new ArrayList<>();
        for (Object[] row : rows) {
            String nazvanie = (String) row[0];
            Long count = (Long) row[1];
            result.add(new GroupStudentCount(nazvanie, count == null ? 0L : count));
        }
        return result;
    }

    // Название группы уникально (UNIQUE в БД); проверяем заранее,
    // чтобы клиент получил 409 с понятным сообщением вместо 500
    private void checkNazvanieFree(String nazvanie, Integer currentShifr) {
        boolean taken = currentShifr == null
                ? repository.existsByNazvanie(nazvanie)
                : repository.existsByNazvanieAndShifrNot(nazvanie, currentShifr);
        if (taken) {
            throw new DuplicateEntityException("Группа с названием '" + nazvanie + "' уже существует");
        }
    }
}
