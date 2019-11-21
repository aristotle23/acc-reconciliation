package fitz;

public enum Color {
    RED("#ea4335"), GREY("#e0e0e0"), WHITE("#ffffff"),PINK("#ed96a4"),GREEN("#3aee5b"),BLUE("#177efe");

    private final String value;

    Color(String s) {
        this.value = s;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

