package by.vstu.vsrpp.exam.studgroup.service;

import java.util.List;

import by.vstu.vsrpp.exam.studgroup.dto.AddStudentsRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupResponse;
import by.vstu.vsrpp.exam.studgroup.dto.GroupStatusStatistics;
import by.vstu.vsrpp.exam.studgroup.entity.Group;

public interface GroupService {


    GroupResponse create(GroupRequest request);

    List<GroupResponse> findAll();
    GroupResponse findById(int id);

	GroupResponse update(int id, GroupRequest request);

    void delete(int id);
	GroupResponse addStudents(int groupId, AddStudentsRequest request);

    List<GroupStatusStatistics> findStatusStatistics();
}
