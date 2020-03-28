package biothewolff.htmlvalidator.core.validate.util;

import com.sun.istack.internal.Nullable;

public class ValidationError {
    public final ValidationErrorTypes errorType;
    public final String context;

    public ValidationError(ValidationErrorTypes type, @Nullable String context) {
        this.errorType = type;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] %s: %s",
                errorType.toString(),
                errorType.getDisplay(),
                context
        );
    }
}
