package by.vstu.vsrpp.exam.studgroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import by.vstu.vsrpp.exam.studgroup.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

	// Количество студентов каждого статуса по каждой группе
	@Query("select s.group.shifr, s.group.nazvanie, s.status, count(s) "
			+ "from Student s group by s.group.shifr, s.group.nazvanie, s.status")
	List<Object[]> countStudentsByGroupAndStatus();

	boolean existsByNazvanie(String nazvanie);

	boolean existsByNazvanieAndShifrNot(String nazvanie, int shifr);
}
