package by.vstu.vsrpp.exam.studgroup.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import by.vstu.vsrpp.exam.studgroup.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

	List<Student> findByNomerZachetkiBetween(Long from, Long to);

	Page<Student> findByNomerZachetkiBetween(Long from, Long to, Pageable pageable);
}
