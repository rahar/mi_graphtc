package ae.masdar.datamining;


public class LimitExceededException extends Exception {
    @Override
    public String getMessage() {
        return "Request limit exceeded";
    }
}
