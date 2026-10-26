package by.vstu.vsrpp.exam.studgroup.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import by.vstu.vsrpp.exam.studgroup.dto.StudentRequest;
import by.vstu.vsrpp.exam.studgroup.dto.StudentResponse;
import by.vstu.vsrpp.exam.studgroup.entity.Student;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentResponse toResponse(Student student);

    List<StudentResponse> toResponseList(List<Student> students);

    @Mapping(target = "group", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDate", ignore = true)
    Student toEntity(StudentRequest request);

    // nomerZachetki — первичный ключ, его менять нельзя
    @Mapping(target = "nomerZachetki", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDate", ignore = true)
    void updateEntityFromDto(StudentRequest request, @MappingTarget Student student);
}