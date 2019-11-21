package fitz;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class summaryProperty {
    private StringProperty detail = new SimpleStringProperty();
    private DoubleProperty debit = new SimpleDoubleProperty();
    private DoubleProperty credit = new SimpleDoubleProperty();
    private StringProperty color = new SimpleStringProperty();
    summaryProperty(String detail, double debit, double credit, String color){
        setCredit(credit);
        setDebit(debit);
        setDetail(detail);
        setColor(color);
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public String getColor() {
        return color.get();
    }

    public void setDebit(double debit) {
        this.debit.set(debit);
    }

    public void setDetail(String detail) {
        this.detail.set(detail);
    }

    public void setCredit(double credit) {
        this.credit.set(credit);
    }

    public double getDebit() {
        return debit.get();
    }

    public String getDetail() {
        return detail.get();
    }

    public double getCredit() {
        return credit.get();
    }

    public DoubleProperty debitProperty() {
        return debit;
    }

    public StringProperty detailProperty() {
        return detail;
    }

    public DoubleProperty creditProperty() {
        return credit;
    }

    public StringProperty colorProperty() {
        return color;
    }
}
