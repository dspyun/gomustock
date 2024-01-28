
package com.gomu.gomustock;

import java.io.Serializable;

public class FullPopup_Option implements Serializable {
    private static final long serialVersionUID = 1L;

    String contents="";

    String type="";


    public FullPopup_Option(){

    }

    public FullPopup_Option(String contents, String type){
        this.contents = contents;
        this.type = type;
    }

    public FullPopup_Option(String contents){
        this.contents = contents;
    }

    public String getContentsType() {
        return type;
    }

    public String getContents() { return contents; }
}