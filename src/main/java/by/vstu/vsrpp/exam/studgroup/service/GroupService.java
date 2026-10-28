package by.vstu.vsrpp.exam.studgroup.service;

import java.util.List;

import by.vstu.vsrpp.exam.studgroup.dto.AddStudentsRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupResponse;
import by.vstu.vsrpp.exam.studgroup.dto.GroupStatusStatistics;
import by.vstu.vsrpp.exam.studgroup.dto.GroupStudentCount;

public interface GroupService {

    GroupResponse create(GroupRequest request);

    List<GroupResponse> findAll();

    GroupResponse findById(int id);

    GroupResponse update(int id, GroupRequest request);

    void delete(int id);

    // Задание 5: удаление группы с переносом её студентов в другую группу
    void deleteWithTransfer(int groupId, int targetGroupId);

    GroupResponse addStudents(int groupId, AddStudentsRequest request);

    List<GroupStatusStatistics> findStatusStatistics();

    // Задание 1: группы, в которых менее maxCount студентов (название + количество)
    List<GroupStudentCount> findGroupsWithFewerStudentsThan(long maxCount);

    // Задание 2: группы, созданные более года назад (statusDate): название + количество студентов
    List<GroupStudentCount> findGroupsOlderThanOneYear();
}
