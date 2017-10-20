package com.ippon.rome;

import com.sun.glass.ui.Window;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.StandardCopyOption;

public class Main extends Application {

    private final TableView<Reference> table = new TableView<>();
    private final ObservableList<Reference> data
            = FXCollections.observableArrayList(
            new Reference("Jacob", new byte[1]),
            new Reference("Isabella", new byte[1]),
            new Reference("Ethan", new byte[1]),
            new Reference("Emma", new byte[1]),
            new Reference("Michael", new byte[1])
    );

    private BorderPane borderPane;

    private TableView fileList;

    private TableColumn hashColumn, downloadColumn;



    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setWidth(400);
        primaryStage.setHeight(600);

        hashColumn = new TableColumn("Hash");
        hashColumn.setCellFactory(p ->
                new TableCell<Reference, String>() {
                    TextField txt = new TextField();
                    @Override
                    protected void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);
                        if(!empty) {
                            Reference ref = getTableView().getItems().get(getIndex());
                            txt.setText(ref.getHash());
                            txt.setEditable(false);
                            txt.setFocusTraversable(false);
                            txt.setBackground(Background.EMPTY);
                            setGraphic(txt);
                        } else {
                            setText(null);
                        }

                    }
                });

        downloadColumn = new TableColumn("Download And Decrypt");
        downloadColumn.setMaxWidth(200.0);
        downloadColumn.setMinWidth(200.0);
        downloadColumn.setCellFactory(p ->
                new TableCell<Reference, String>() {
                    final Button btn = new Button("Just Do It");
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty) {
                            btn.setOnAction(event -> {
                                Reference ref = getTableView().getItems().get(getIndex());
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.setTitle("Choose Decrypted File Location");
                                File targetFile = fileChooser.showSaveDialog(primaryStage);
                                if(targetFile != null){
                                    try {
                                        OutputStream outputStream = new FileOutputStream(targetFile);
                                        InputStream inputStream = ref.getData();
                                        java.nio.file.Files.copy(
                                                inputStream,
                                                targetFile.toPath(),
                                                StandardCopyOption.REPLACE_EXISTING);
                                        outputStream.close();
                                        inputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            setGraphic(btn);
                            setAlignment(Pos.BASELINE_CENTER);
                            setText(null);
                        }
                    }
                });

        fileList = new TableView();
        fileList.setItems(data);
        fileList.getColumns().addAll(hashColumn,downloadColumn);
        fileList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        borderPane = new BorderPane();
        borderPane.setCenter(fileList);

        Button addFileButton = new Button("Add File");
        addFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File for Upload");
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if(selectedFile != null) {
                try {
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(selectedFile));
                    data.addAll(new Reference(inputStream));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        });

        borderPane.setBottom(addFileButton);

        Scene scene = new Scene(borderPane);
        primaryStage.setTitle("Example IPFS Encryption/Decryption");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Class.forName("com.ippon.rome.Reference");

        launch(args);
    }
}
