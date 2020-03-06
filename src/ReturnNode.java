
class ReturnNode {

    private String register;
    private String type;
    private String message;

    ReturnNode (String _register, String _type, String _message){
        this.register = _register;
        this.type = _type;
        this.message = _message;
    }

    String getRegister() {
        return register;
    }

    String getType() {
        return type;
    }

    String getMessage() {
        return message;
    }

    void setRegister(String name){
        this.register = name;
    }

    void setMessage(String message) { this.message = message; }
}
