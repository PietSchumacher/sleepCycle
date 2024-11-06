package sleep.exceptions;

public class SleepSessionNotFoundException extends RuntimeException{
    private static final long serialVerisionUID = 1;

    public SleepSessionNotFoundException(String message){
        super(message);
    }
}
