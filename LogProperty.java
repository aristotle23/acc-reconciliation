package fitz;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LogProperty {
    StringProperty timestamp = new SimpleStringProperty();
    StringProperty timeline = new SimpleStringProperty();
    IntegerProperty logId = new SimpleIntegerProperty();

    LogProperty(String timestamp, String timeline, int logId){
        setTimeline(timeline);
        setTimestamp(timestamp);
        setLogId(logId);
    }

    void setLogId(int logId) {
        this.logId.set(logId);
    }

    public int getLogId() {
        return logId.get();
    }

    void setTimestamp(String timestamp) {
        this.timestamp.set(timestamp);
    }

    void setTimeline(String timeline) {
        this.timeline.set(timeline);
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public String getTimeline() {
        return timeline.get();
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }

    public StringProperty timelineProperty() {
        return timeline;
    }

    public IntegerProperty logIdProperty() {
        return logId;
    }
}
