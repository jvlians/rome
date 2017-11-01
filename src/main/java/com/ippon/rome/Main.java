package com.ippon.rome;

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
import java.security.KeyPair;

public class Main extends Application {

    private final ObservableList<Reference> myFiles
            = FXCollections.observableArrayList();
    private final ObservableList<Reference> sharedFiles
            = FXCollections.observableArrayList();

    private BorderPane borderPane;

    private TableView myFileList;
    private TableView sharedFileList;

    private TableColumn myHashColumn, myDownloadColumn, myShareColumn, sharedHashColumn, sharedDownloadColumn;



    @Override
    public void start(Stage primaryStage) throws Exception{
        myFiles.addAll(Reference.getIndex());
        sharedFiles.addAll(Reference.getIndex());
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        myHashColumn = new TableColumn("Hash");
        myHashColumn.setCellFactory(p ->
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
        sharedHashColumn = new TableColumn("Hash");
        sharedHashColumn.setCellFactory(p ->
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

        myDownloadColumn = new TableColumn("Download And Decrypt");
        myDownloadColumn.setMaxWidth(200.0);
        myDownloadColumn.setMinWidth(200.0);
        myDownloadColumn.setCellFactory(p ->
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
        sharedDownloadColumn = new TableColumn("Download And Decrypt");
        sharedDownloadColumn.setMaxWidth(200.0);
        sharedDownloadColumn.setMinWidth(200.0);
        sharedDownloadColumn.setCellFactory(p ->
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

        myShareColumn = new TableColumn("Share File");
        myShareColumn.setMaxWidth(200.0);
        myShareColumn.setMinWidth(200.0);
        myShareColumn.setCellFactory(p ->
                new TableCell<Reference, String>() {
                    final Button btn = new Button("Share File");
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty) {
                            btn.setOnAction(event -> {
                                Reference ref = getTableView().getItems().get(getIndex());
                                //TODO prompt for public key of sharee
                                //TODO encrypt decryption key and publish to HyperLedger

                            });
                            setGraphic(btn);
                            setAlignment(Pos.BASELINE_CENTER);
                            setText(null);
                        }
                    }
                });

        myFileList = new TableView();
        myFileList.setItems(myFiles);
        myFileList.getColumns().addAll(myHashColumn, myDownloadColumn, myShareColumn);
        myFileList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        sharedFileList = new TableView();
        sharedFileList.setItems(sharedFiles);
        sharedFileList.getColumns().addAll(sharedHashColumn, sharedDownloadColumn);
        sharedFileList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        borderPane = new BorderPane();

        borderPane.setTop(sharedFileList);
        borderPane.setCenter(myFileList);

        Button addFileButton = new Button("Add File");
        addFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File for Upload");
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if(selectedFile != null) {
                try {
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(selectedFile));
                    myFiles.addAll(new Reference(inputStream));
                } catch (Exception e) {
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

        /*
        KeyPair pair = KeyProcessor.generate();
        String input = "foo! :)";
        System.out.println(input);
        String pub = KeyProcessor.serialize(pair.getPublic());
        String priv = KeyProcessor.serialize(pair.getPrivate());
        System.out.println(pub+" "+priv);
        byte[] data = KeyProcessor.encrypt(pub, input.getBytes());
        System.out.println(new String(data));
        byte[] str = KeyProcessor.decrypt(priv, data);
        System.out.println(new String(str));
        */
        launch(args);
    }
}
