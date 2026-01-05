public class FieldValidator {

    private final TextInputLayout layout;
    private final TextInputEditText input;
    private final List<IValidationRule> rules = new ArrayList<>();

    public FieldValidator(TextInputLayout layout, TextInputEditText input) {
        this.layout = layout;
        this.input = input;
    }

    public FieldValidator addRule(IValidationRule rule) {
        rules.add(rule);
        return this;
    }

    public boolean validate() {
        layout.setError(null);

        String value = input.getText() != null
                ? input.getText().toString().trim()
                : "";

        for (IValidationRule rule : rules) {
            if (!rule.isValid(value)) {
                layout.setError(rule.getErrorMessage());
                return false;
            }
        }

        return true;
    }

    public String getValue() {
        return input.getText().toString().trim();
    }
}