package server;

/**
 * A list of the possible types of responses the server can provide to the client
 */
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
