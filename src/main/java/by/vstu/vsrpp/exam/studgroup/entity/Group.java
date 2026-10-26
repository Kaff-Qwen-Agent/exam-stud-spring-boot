package by.vstu.vsrpp.exam.studgroup.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import by.vstu.vsrpp.exam.studgroup.validation.ValidStudentCount;

@Entity
@Table(name = "gruppyi")
// Hibernate persist-валидация отключена в application.properties:
// новая группа законно создаётся с 0 студентами, правило 1..3
// проверяется вручную через Validator в GroupServiceImpl.addStudents
@ValidStudentCount(min = 1, max = 3)
public class Group {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Shifr")
	private int shifr;

	@Column(name = "Nazvanie", unique = true, length = 50, nullable = false)
	private String nazvanie;

	@Column(name = "DataFormir", nullable = false)
	private LocalDate dataFormir;

	@Column(name = "KodPlana", nullable = false)
	private int kodPlana;

	@Column(name = "Status", length = 50)
	private String status;

	@Column(name = "StatusDate")
	private LocalDate statusDate;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "group")
	@JsonBackReference
	private List<Student> students;

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}

	public int getShifr() {
		return shifr;
	}

	public void setShifr(int shifr) {
		this.shifr = shifr;
	}

	public String getNazvanie() {
		return nazvanie;
	}

	public void setNazvanie(String nazvanie) {
		this.nazvanie = nazvanie;
	}

	public LocalDate getDataFormir() {
		return dataFormir;
	}

	public void setDataFormir(LocalDate dataFormir) {
		this.dataFormir = dataFormir;
	}

	public int getKodPlana() {
		return kodPlana;
	}

	public void setKodPlana(int kodPlana) {
		this.kodPlana = kodPlana;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDate getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(LocalDate statusDate) {
		this.statusDate = statusDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Group [name=");
		builder.append(nazvanie);
		builder.append(", students=");
		builder.append(students);
		builder.append("]");
		return builder.toString();
	}

}
