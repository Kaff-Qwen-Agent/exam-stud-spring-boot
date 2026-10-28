package by.vstu.vsrpp.exam.studgroup.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

// Задание 4: части ФИО студента. Пустое значение и наличие пробелов
// считаются неправильными; сообщение — "Нужно указать имя".
@Documented
@Constraint(validatedBy = ValidNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {

    String message() default "Нужно указать имя";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
