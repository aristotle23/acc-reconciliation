package fitz;

import org.apache.commons.lang3.SystemUtils;

public enum AppDefault {
    EMBED_DBLOC(SystemUtils.getUserHome().getAbsolutePath()+"/.embedDb");

    private final String param;

    AppDefault(String param) {
        this.param = param;
    }
    @Override
    public String toString(){
        return  param;
    }
}
