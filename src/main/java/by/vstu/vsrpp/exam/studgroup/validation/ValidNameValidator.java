package by.vstu.vsrpp.exam.studgroup.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidNameValidator implements ConstraintValidator<ValidName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null/пустое значение или наличие пробелов — некорректное имя
        return value != null && !value.trim().isEmpty() && !value.contains(" ");
    }
}
