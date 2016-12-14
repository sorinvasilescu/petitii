package ro.petitii.generator;

public abstract class GenericGenerator {
    protected String pattern;

    public GenericGenerator(String pattern) {
        this.pattern = pattern;
    }

    public abstract String generateId();
}
