package com.ippon.rome;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Reference {
    static Connection conn = null;
    static PreparedStatement insert = null, update = null, last = null;
    static PreparedStatement getid = null, gethash = null;
    private String hash;
    private byte[] key;
    static IPFS ipfs;
    static {
        ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
        String path = System.getProperty("user.home") + File.separator + ".rome.db";
        System.out.println(path);
        // create a database connection
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:"+path);

            Statement s = getStatement();
            //s.executeUpdate("drop table if exists files");
            s.executeUpdate("create table if not exists files " +
                    "(id integer primary key autoincrement," +
                    "hash text not null, key blob not null)");

            insert = conn.prepareStatement("insert into files values (null, ?, ?);");
            update = conn.prepareStatement("update files set " +
                    "hash = ifnull(?, hash), key = ifnull(?, key) " +
                    "where id = ?;");
            last = conn.prepareStatement("select last_insert_rowid();");
            getid = conn.prepareStatement("select * from files where id = ?;");
            gethash = conn.prepareStatement("select * from files where hash = ?;");
            //System.out.println("file "+setFileRow(null, "hash", new byte[]{1,2,3}));
            ResultSet rs = s.executeQuery("select * from files");
            while(rs.next()) {
                // read the result set
                System.out.println("id   = " + rs.getInt("id"));
                System.out.println("hash = " + rs.getString("hash"));
                byte[] key = rs.getBytes("key");
                String keyStr = toHex(key);
                System.out.println("key  = " + keyStr);
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }
    }
    public static String toHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
    static Statement getStatement() {
        try {
            Statement s = conn.createStatement();
            s.setQueryTimeout(30);
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    static int setFileRow(Integer id, String hash, byte[] key) throws SQLException {
        PreparedStatement ps = id == null ? insert : update;
        ps.setString(1, hash);
        ps.setBytes(2, key);
        if(id == null) {
            ps.executeUpdate();
            // TODO figure out how to insert+select locked
            return last.executeQuery().getInt(1);
        } else {
            ps.setInt(3, id);
            ps.executeUpdate();
            return id;
        }
    }
    static Reference getFileRow(int id) throws SQLException {
        getid.setInt(1, id);
        return new Reference(getid.executeQuery());
    }
    static Reference getFileRow(String hash) throws SQLException {
        gethash.setString(1, hash);
        return new Reference(gethash.executeQuery());
    }

    private Reference(ResultSet set) throws SQLException {
        this(set.getString("hash"), set.getBytes("key"));
    }

    public Reference(String hash, byte[] key){
        this.hash = hash;
        this.key = key;
    }

    public Reference(BufferedInputStream file) throws IOException {
        EncryptionDTO dto = null;
        try {
            dto = FileProcessor.encrypt(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RefStream nstream = new RefStream(file, UUID.randomUUID().toString());

        MerkleNode mn = ipfs.add(nstream);
        this.hash = mn.hash.toBase58();
        this.key = dto.keyBytes;
    }

    public String getHash() {
        return hash;
    }

    public InputStream getData() throws IOException {
        InputStream stream = ipfs.catStream(Multihash.fromBase58(this.hash));
        EncryptionDTO dto = new EncryptionDTO(key, stream);
        try {
            return FileProcessor.decrypt(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateData(BufferedInputStream file){

    }

    public Reference getPreviousVersion(){
        //Return null if does not exist
        return null;
    }
}
class RefStream implements NamedStreamable {
    InputStream stream;
    String name;

    public RefStream(InputStream stream, String name) {
        this.stream = stream; this.name = name;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public List<NamedStreamable> getChildren() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}