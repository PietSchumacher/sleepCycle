package sleep.exceptions;

public class SleepPersonNotFoundException extends RuntimeException{
    private static final long serialVerisionUID = 1;

    public SleepPersonNotFoundException(String message){
        super(message);
    }
}
