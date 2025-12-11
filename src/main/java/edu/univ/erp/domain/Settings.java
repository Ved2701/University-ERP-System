package edu.univ.erp.domain;

public class Settings {
    private boolean maintenance;
    public Settings() {}
    public Settings(boolean maintenance) { this.maintenance = maintenance; }
    public boolean isMaintenance() { return maintenance; }
    public void setMaintenance(boolean maintenance) { this.maintenance = maintenance; }
    @Override
    public String toString() {
        return "Settings[maintenance=" + maintenance + "]";
    }
}
