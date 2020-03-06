public class SemanticCheckError extends RuntimeException{

    private String message;

    public SemanticCheckError(String temp){
        message = temp;
    }

    public String getMessage() {
        return message;
    }
}
