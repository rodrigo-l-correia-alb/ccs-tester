package pt.ptinovacao.InterfaceHTTP;

public enum AgreementType {
    DATA("571a4569705297995a006226"),
    VOICE("58b98b1b70529793085e5ace"),
    SMS("5ad246344bfb5abaf1235d6c");

    public final String label;

    AgreementType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        if (label == DATA.label) {
            return "DATA";
        } else if (label == SMS.label) {
            return "SMS";
        } else {
            return "VOICE";
        }
    }
}
