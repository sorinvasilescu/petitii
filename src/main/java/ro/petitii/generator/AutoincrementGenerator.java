package ro.petitii.generator;

public class AutoincrementGenerator extends GenericGenerator {
    private long startId;

    public AutoincrementGenerator(String pattern, long startId) {
        super(pattern);
        this.startId = startId;
    }

    @Override
    public String generateId() {
        return String.format(pattern, startId++);
    }
}
