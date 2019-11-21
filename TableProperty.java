package fitz;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableProperty {
    private StringProperty date = new SimpleStringProperty();
    private StringProperty detail = new SimpleStringProperty();
    private StringProperty debit = new SimpleStringProperty();
    private StringProperty credit = new SimpleStringProperty();
    private StringProperty color = new SimpleStringProperty();
    private StringProperty ref = new SimpleStringProperty();

    public TableProperty(String date, String detail, String debit, String credit,String color, String ref){
        setDate(date);
        setDetail(detail);
        setDebit(debit);
        setCredit(credit);
        setColor(color);
        setRef(ref);
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public void setDetail(String detail) {
        this.detail.set(detail);
    }

    public void setDebit(String debit) {
        this.debit.set(debit);
    }

    public void setCredit(String credit) {
        this.credit.set(credit);
    }

    public void setRef(String ref) {
        this.ref.set(ref);
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public String getDate() {
        return date.get();
    }

    public String getDetail() {
        return detail.get();
    }

    public String getDebit() {
        return debit.get();
    }

    public String getCredit() {
        return credit.get();
    }

    public String getColor() {
        return color.get();
    }

    public String getRef() {
        return ref.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty detailProperty() {
        return detail;
    }

    public StringProperty debitProperty() {
        return debit;
    }

    public StringProperty creditProperty() {
        return credit;
    }

    public StringProperty colorProperty() {
        return color;
    }

    public StringProperty refProperty() {
        return ref;
    }
}
