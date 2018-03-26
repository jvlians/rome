package com.ippon.rome;

import com.google.common.primitives.Bytes;
import com.mashape.unirest.http.JsonNode;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.security.KeyPair;
import java.util.UUID;

public class Main extends Application {

    private final ObservableList<Reference> myFiles
            = FXCollections.observableArrayList();
    private final ObservableList<Reference> sharedFiles
            = FXCollections.observableArrayList();

    private BorderPane borderPane;

    private TableView myFileList;
    private TableView sharedFileList;

    private TableColumn myHashColumn, myDownloadColumn, myShareColumn, sharedHashColumn, sharedDownloadColumn, reshareColumn;

    private static HyperLedgerApi hlapi = new HyperLedgerApi("http://184.172.247.54:31090");

    @Override
    public void start(Stage primaryStage) throws Exception{
        myFiles.addAll(Reference.getOurs(1));
        loadShared();
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
        sharedHashColumn = new TableColumn("Files Shared with Me");
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
                    final Button btn = new Button("Download Button");
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
                    final Button btn = new Button("Download");
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
                                shareRef(ref);
                            });
                            setGraphic(btn);
                            setAlignment(Pos.BASELINE_CENTER);
                            setText(null);
                        }
                    }
                });

        reshareColumn = new TableColumn("Share File");
        reshareColumn.setMaxWidth(200.0);
        reshareColumn.setMinWidth(200.0);
        reshareColumn.setCellFactory(p ->
                new TableCell<Reference, String>() {
                    final Button btn = new Button("Share File");
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty) {
                            btn.setOnAction(event -> {
                                Reference ref = getTableView().getItems().get(getIndex());
                                shareRef(ref);
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
        sharedFileList.getColumns().addAll(sharedHashColumn, sharedDownloadColumn, reshareColumn);
        sharedFileList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        borderPane = new BorderPane();

        borderPane.setTop(sharedFileList);
        borderPane.setCenter(myFileList);


        HBox bottomPane = new HBox(8);
        bottomPane.setAlignment(Pos.CENTER_LEFT);

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


        String myPublicKey = Reference.pub;
        Text publicKeyText = new Text("Public Key:");
        publicKeyText.autosize();
        TextField publicKey = new TextField(myPublicKey);
        publicKey.setEditable(false);
        publicKey.setMinWidth(200);

        bottomPane.getChildren().addAll(addFileButton, publicKeyText, publicKey);
        borderPane.setBottom(bottomPane);

        Scene scene = new Scene(borderPane);
        primaryStage.setTitle("Example IPFS Encryption/Decryption");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    public void loadShared() {
        JsonNode node = null;
        try {
            node = hlapi.getFilesSharedWithUser(Reference.pub);
        } catch(IOException e) {
            System.err.println("failed to get shared files");
            return;
        }
        try {
            Reference.clearShared();
        } catch(SQLException e) {
            System.err.println("failed to delete shared files");
            return;
        }
        JSONArray arr = node.getArray();
        ArrayList<Reference> next = new ArrayList();
        for(int i=0; i<arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String encref = o.getString("encryptedReference");
            try {
                byte[] catref = KeyProcessor.decrypt(Reference.priv, encref);
                Reference ref = Reference.fromCatRef(new String(catref));
                ref.insertFileRow();
                next.add(ref);
            } catch (Exception e) {
                System.err.println("decryption failed :(");
                return;
            }
        }
        sharedFiles.removeAll();
        sharedFiles.addAll(next);
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
    static void shareRef(Reference ref) {

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Share File");
        dialog.setHeaderText("Please enter the recipient's public key");
        dialog.setContentText("Public Key:");
        Optional<String> result = dialog.showAndWait();
        if(result.isPresent()){
            String cat = ref.toCatRef();
            String pubkey = result.get();  // recipient's public key
            try {
                String encrypted = KeyProcessor.encrypt(pubkey, cat.getBytes());
                hlapi.shareWithUser(UUID.randomUUID().toString(),encrypted,pubkey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
