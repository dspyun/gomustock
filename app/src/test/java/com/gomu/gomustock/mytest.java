package com.gomu.gomustock;

public class mytest {
    public static void main(String[] args) {

        StringBuffer date = new StringBuffer( "2023.06.18");
        date.deleteCharAt(4);
        date.deleteCharAt(6);
        String date1 = "2023.06.18";
        //date1 = date1.substring(0,4) + date1.substring(5);
        //date1 = date1.substring(0,6) + date1.substring(7);
        date1.replaceAll("\\.","");
        System.out.println("test " + date1.toString());
;
/*
        Label label = new Label("This is JavaFX!");
        FlowPane pane = new FlowPane();
        pane.getChildren().add(label);
        Scene scene = new Scene(pane, 320, 240);
        stage.setScene(scene);
        stage.show();

*/
    }

}
