package by.vstu.vsrpp.exam.studgroup.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import by.vstu.vsrpp.exam.studgroup.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

// Количество студентов каждого статуса по каждой группе
@Query("select s.group.shifr, s.group.nazvanie, s.status, count(s) "
+ "from Student s group by s.group.shifr, s.group.nazvanie, s.status")
List<Object[]> countStudentsByGroupAndStatus();

// Задание 1: группы, в которых менее maxCount студентов (включая пустые).
// LEFT JOIN нужен, чтобы группы без студентов тоже попадали в результат (count = 0)
@Query("select g.nazvanie, count(s) from Group g LEFT JOIN g.students s "
+ "group by g.shifr, g.nazvanie having count(s) < :maxCount order by g.shifr")
List<Object[]> findGroupsWithStudentCountLessThan(@Param("maxCount") long maxCount);

// Задание 2: группы, созданные (statusDate) раньше заданной даты, и число студентов в них
@Query("select g.nazvanie, count(s) from Group g LEFT JOIN g.students s "
+ "where g.statusDate is not null and g.statusDate < :before "
+ "group by g.shifr, g.nazvanie order by g.shifr")
List<Object[]> findGroupsCreatedBefore(@Param("before") LocalDate before);

boolean existsByNazvanie(String nazvanie);

boolean existsByNazvanieAndShifrNot(String nazvanie, int shifr);
}
