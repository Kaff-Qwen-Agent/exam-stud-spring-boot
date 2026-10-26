package by.vstu.vsrpp.exam.studgroup.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class StudentRequest {

	@NotNull
	private Long nomerZachetki;

	// Идентификатор (шифр) группы студента; обязателен при создании,
	// в addStudents группа определяется путём запроса
	private Integer shifrGruppyi;

	@NotBlank
	@Size(max = 30)
	private String familiya;

	@NotBlank
	@Size(max = 30)
	private String imya;

	@NotBlank
	@Size(max = 30)
	private String otchestvo;

	@NotBlank
	@Size(max = 30)
	private String gorod;

	@NotBlank
	@Size(max = 80)
	private String adres;

	@NotBlank
	@Size(max = 30)
	private String tel;

	@Size(max = 50)
	private String status;

	public Long getNomerZachetki() {
		return nomerZachetki;
	}

	public void setNomerZachetki(Long nomerZachetki) {
		this.nomerZachetki = nomerZachetki;
	}

	public Integer getShifrGruppyi() {
		return shifrGruppyi;
	}

	public void setShifrGruppyi(Integer shifrGruppyi) {
		this.shifrGruppyi = shifrGruppyi;
	}

	public String getFamiliya() {
		return familiya;
	}

	public void setFamiliya(String familiya) {
		this.familiya = familiya;
	}

	public String getImya() {
		return imya;
	}

	public void setImya(String imya) {
		this.imya = imya;
	}

	public String getOtchestvo() {
		return otchestvo;
	}

	public void setOtchestvo(String otchestvo) {
		this.otchestvo = otchestvo;
	}

	public String getGorod() {
		return gorod;
	}

	public void setGorod(String gorod) {
		this.gorod = gorod;
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
