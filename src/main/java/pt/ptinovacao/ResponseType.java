package pt.ptinovacao;

public enum ResponseType {
    RETRY("571a456970529noHomeworkWasCopiedHere7995a006226"),
    TERMINATE_SESSION("58b98bnoHomeworkWasCopiedHere1b70529793085e5ace"),
    TOPUP("58b98bnoHomeworkWasActualyCopiedHere1b70529793085e5ace"),
    CONTINUE("5ad24noHomeworkWasCopiedHere6344bfb5abaf1235d6c");

    public final String label;

    ResponseType(String label) {
        this.label = label;
    }

    public static ResponseType getResponseType(String resultCode) {
        if (resultCode == null) {
            return CONTINUE;
        } else {
            switch (resultCode) {
                case "DIAMETER_CREDIT_LIMIT_REACHED":
                    return TERMINATE_SESSION;

                case "DIAMETER_UNABLE_TO_COMPLY":
                    return TERMINATE_SESSION;

                case "DIAMETER_END_USER_SERVICE_DENIED":
                    return TERMINATE_SESSION;

                case "DIAMETER_TOO_BUSY":
                    return RETRY;

                case "DIAMETER_SUCCESS":
                default:
                    return CONTINUE;

            }
        }
    }


    public static ResponseType getResponseType(String resultCode, String gsu, String protocolRequestType) {
        if (resultCode == null) {
            return CONTINUE;
        } else if (resultCode.equals("200") || resultCode.equals("201")) {
            if (gsu.equals("0") || gsu.equals("")) {
                System.out.println("TERMINATE_SESSION");
                return TERMINATE_SESSION;
            } else {
                //System.out.println("CONTINUE");
                return CONTINUE;
            }
        } else if (resultCode.equals("403") && protocolRequestType.equals("INITIAL_REQUEST")) {
            return RETRY;
        } else {
            System.out.println("TERMINATE_SESSION");
            return TERMINATE_SESSION;
        }
    }

}
