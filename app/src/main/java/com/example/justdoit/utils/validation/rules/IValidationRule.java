public interface IValidationRule {
    boolean isValid(String value);
    String getErrorMessage();
}