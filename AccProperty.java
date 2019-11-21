package fitz;

public class AccProperty {
    private int  id;
    private String text;

    public  AccProperty(int id, String text){
        this.id = id;
        this.text  = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getText();
    }
}
