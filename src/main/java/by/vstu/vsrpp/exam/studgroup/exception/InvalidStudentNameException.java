package by.vstu.vsrpp.exam.studgroup.exception;

// Задание 4: некорректное имя студента (пустое значение или наличие пробелов).
// Запись не сохраняется, клиент получает JSON с сообщением "Нужно указать имя".
public class InvalidStudentNameException extends RuntimeException {

    public InvalidStudentNameException(String message) {
        super(message);
    }
}
