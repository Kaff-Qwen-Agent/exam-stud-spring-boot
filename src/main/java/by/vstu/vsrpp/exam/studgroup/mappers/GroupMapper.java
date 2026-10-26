package by.vstu.vsrpp.exam.studgroup.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import by.vstu.vsrpp.exam.studgroup.dto.GroupRequest;
import by.vstu.vsrpp.exam.studgroup.dto.GroupResponse;
import by.vstu.vsrpp.exam.studgroup.dto.StudentRequest;
import by.vstu.vsrpp.exam.studgroup.entity.Group;
import by.vstu.vsrpp.exam.studgroup.entity.Student;

@Mapper(componentModel = "spring")
public interface GroupMapper {


    // Одиночный метод — обязателен, так как toEntityList опирается на него
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "statusDate", ignore = true)
    Student toEntity(StudentRequest request);

    // Списковый метод — реализация будет сгенерирована автоматически
    List<Student> toEntityList(List<StudentRequest> requests);

    GroupResponse toResponse(Group group);
    java.util.List<GroupResponse> toResponseList(java.util.List<Group> groups);

    // NEW: Для CREATE
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDate", ignore = true)
    Group toEntity(GroupRequest request);

    // NEW: Для UPDATE (обновляет существующую сущность)
    // shifr — первичный ключ, его менять нельзя
    @Mapping(target = "shifr", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDate", ignore = true)
    void updateEntityFromDto(GroupRequest request, @MappingTarget Group group);
}