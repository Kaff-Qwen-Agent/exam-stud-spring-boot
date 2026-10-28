package by.vstu.vsrpp.exam.studgroup.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(GroupValidationException.class)
	public ResponseEntity<ValidationErrorResponse> handleGroupValidation(GroupValidationException ex) {
		return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getErrors()));
	}

	// Задание 4: некорректное имя студента — JSON с сообщением "Нужно указать имя"
	@ExceptionHandler(InvalidStudentNameException.class)
	public ResponseEntity<ValidationErrorResponse> handleInvalidStudentName(InvalidStudentNameException ex) {
		return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getMessage()));
	}

	// Дубликат (уникальный ключ, повторный PK) — 409 вместо 500
	@ExceptionHandler(DuplicateEntityException.class)
	public ResponseEntity<ValidationErrorResponse> handleDuplicate(DuplicateEntityException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ValidationErrorResponse(ex.getMessage()));
	}

	// Страховка от нарушений целостности на уровне БД (FK, UNIQUE и т.п.)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ValidationErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ValidationErrorResponse("Нарушение целостности данных: запись уже существует или связана с другими данными"));
	}

	// Неизвестное свойство в ?sort=... — 400 вместо 500
	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<ValidationErrorResponse> handlePropertyReference(PropertyReferenceException ex) {
		return ResponseEntity.badRequest().body(new ValidationErrorResponse("Ошибка параметров запроса: " + ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationErrorResponse> handleRequestValidation(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.toList());
		return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getMessage()));
	}

	public static class ValidationErrorResponse {
		private final List<String> errors;

		public ValidationErrorResponse(List<String> errors) {
			this.errors = errors;
		}

		public ValidationErrorResponse(String error) {
			this.errors = java.util.Collections.singletonList(error);
		}

		public List<String> getErrors() {
			return errors;
		}
	}
}
