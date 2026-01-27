package ro.insurance.buildinginsurance.domain.enums;

public enum PolicyStatus {
    DRAFT {
        @Override public boolean canActivate() { return true; }
    },
    ACTIVE {
        @Override public boolean canCancel() { return true; }
    },
    CANCELLED,
    EXPIRED;

    public boolean isDraft(){ return this == DRAFT;}
    public boolean isActive(){ return this == ACTIVE;}

    public boolean canActivate() { return false; }
    public boolean canCancel() { return false; }
}
