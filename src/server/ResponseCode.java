package server;

public enum ResponseCode {
    None,
    Success,
    Error,
    LoggedIn;

    public String toString() {
        switch (this) {
            case Success:
                return "+";
            case Error:
                return "-";
            case LoggedIn:
                return "!";
            default:
                return "";
        }
    }
}
