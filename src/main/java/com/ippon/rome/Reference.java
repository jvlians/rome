package com.ippon.rome;

import com.google.common.primitives.Bytes;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.sql.*;
import java.util.*;

public class Reference {
    static Connection conn = null;
    static PreparedStatement insert = null, update = null, last = null;
    static PreparedStatement delShared = null, getOurs = null;
    static PreparedStatement getid = null, gethash = null, index = null;
    private String hash;
    private String name;
    private static final int HASHLEN=46;
    private byte[] key;
    private boolean ours;
    static String pub, priv;
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
            //s.executeUpdate("drop table if exists profile");
            s.executeUpdate("create table if not exists files " +
                    "(id integer primary key autoincrement," +
                    "hash text not null, name text not null, key blob not null, ours integer)");

            insert = conn.prepareStatement("insert into files values (null, ?, ?, ?, ?);");
            update = conn.prepareStatement("update files set " +
                    "hash = ifnull(?, hash), name = ifnull(?, name), key = ifnull(?, key), ours = ifnull(?, ours) " +
                    "where id = ?;");
            delShared = conn.prepareStatement("delete from files where ours=0;");
            last = conn.prepareStatement("select last_insert_rowid();");
            getid = conn.prepareStatement("select * from files where id = ?;");
            gethash = conn.prepareStatement("select * from files where hash = ?;");
            index = conn.prepareStatement("select * from files;");
            getOurs = conn.prepareStatement("select * from files where ours = ?;");
            //System.out.println("file "+setFileRow(null, "hash", new byte[]{1,2,3}));

            s = getStatement();
            s.executeUpdate("create table if not exists profile " +
                    "(id integer primary key autoincrement," +
                    "pub text not null, priv text not null)");
            s = getStatement();
            s.execute("select * from profile");
            ResultSet rs = s.getResultSet();
            // if non empty
            if(rs.next()) {
                pub = rs.getString("pub");
                priv = rs.getString("priv");
            } else {
                KeyPair pair = KeyProcessor.generate();
                pub = KeyProcessor.serialize(pair.getPublic());
                priv = KeyProcessor.serialize(pair.getPrivate());
                PreparedStatement ps = conn.prepareStatement("insert into profile values (null, ?, ?)");
                ps.setString(1, pub);
                ps.setString(2, priv);
                ps.executeUpdate();
            }
            getIndex();
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
    static int setFileRow(Integer id, String hash, String name, byte[] key, Integer ours) throws SQLException {
        PreparedStatement ps = id == null ? insert : update;
        if(hash != null) ps.setString(1, hash);
        if(key != null) ps.setString(2, name);
        if(key != null) ps.setBytes(3, key);
        if(ours != null) ps.setInt(4, ours);
        if(id == null) {
            ps.executeUpdate();
            // TODO figure out how to insert+select locked
            return last.executeQuery().getInt(1);
        } else {
            ps.setInt(5, id);
            ps.executeUpdate();
            return id;
        }
    }
    void insertFileRow() throws SQLException {
        setFileRow(null, hash, name, key, ours?1:0);
    }
    static Reference getFileRow(int id) throws SQLException {
        getid.setInt(1, id);
        return new Reference(getid.executeQuery());
    }
    static Reference getFileRow(String hash) throws SQLException {
        gethash.setString(1, hash);
        return new Reference(gethash.executeQuery());
    }
    static void clearShared() throws SQLException {
        delShared.execute();
    }
    static List<Reference> getOurs(int ours) throws SQLException {
        getOurs.setInt(1, ours);
        return toRefList(getOurs.executeQuery());
    }
    static List<Reference> getIndex() throws SQLException {
        return toRefList(index.executeQuery());
    }
    private static List<Reference> toRefList(ResultSet rs) throws SQLException {
        ArrayList<Reference> list = new ArrayList<>();
        while(rs.next()) {
            Reference r = new Reference(rs);
            list.add(r);
            // read the result set
            System.out.println("id   = " + rs.getInt("id"));
            System.out.println("hash = " + r.hash);
            System.out.println("key  = " + toHex(r.key));
            System.out.println();
        }
        return list;
    }
    private Reference(ResultSet set) throws SQLException {
        this(set.getString("hash"),
                set.getString("name"),
                set.getBytes("key"),
                set.getInt("ours")!=0);
    }

    public Reference(String hash, String name, byte[] key, boolean ours){
        this.hash = hash;
        this.name = name;
        this.key = key;
        this.ours = ours;
    }

    public Reference(BufferedInputStream file) throws Exception {
        EncryptionDTO dto = null;
        try {
            dto = FileProcessor.encrypt(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RefStream nstream = new RefStream(dto.encrypted, UUID.randomUUID().toString());

        MerkleNode mn = ipfs.add(nstream).get(0);
        this.hash = mn.hash.toBase58();
        this.key = dto.keyBytes;
        //FIXME bufferedinputstream doesn't have name? remove?
        setFileRow(null, this.hash, this.name, this.key, 1);
    }

    public Reference(BufferedInputStream file, String fileName) throws Exception {
        EncryptionDTO dto = null;
        try {
            dto = FileProcessor.encrypt(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RefStream nstream = new RefStream(dto.encrypted, UUID.randomUUID().toString());

        MerkleNode mn = ipfs.add(nstream).get(0);
        this.hash = mn.hash.toBase58();
        this.key = dto.keyBytes;
        this.name = fileName;
        setFileRow(null, this.hash, this.name, this.key, 1);
    }

    public String getHash() {
        return hash;
    }

    public byte[] getKey() {
        return key;
    }
    public String toCatRef() {

        byte[] hashb = hash.getBytes();
        // byte[] fname = ref.getName();            // encrypted file's original filename
        byte[] cat = Bytes.concat(hashb, key);
        String enc = KeyProcessor.b64e(cat);
        return enc;
    }
    public static Reference fromCatRef(String enc) {
        byte[] cat = KeyProcessor.b64d(enc);

        byte[] hashb = Arrays.copyOfRange(cat, 0, HASHLEN);
        byte[] key = Arrays.copyOfRange(cat, HASHLEN, cat.length);

        //FIXME null
        return new Reference(new String(hashb), null, key, false);
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