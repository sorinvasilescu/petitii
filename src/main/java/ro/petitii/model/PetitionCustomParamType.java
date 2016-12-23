package ro.petitii.model;

public enum PetitionCustomParamType {
    entityType("entity_type"),
    informationType("information_type"),
    problemType("problem_type"),
    domain_type("domain_type"),
    title_type("title_type");

    private String dbName;

    PetitionCustomParamType(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    public static PetitionCustomParamType getParamType(String dbName) {
        if (dbName == null) {
            return null;
        }

        for (PetitionCustomParamType type : values()) {
            if (type.dbName.equalsIgnoreCase(dbName)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return dbName;
    }
}
