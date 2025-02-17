package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {

    static Connection conn;
    static List<Client> clients;

    public static void main(String[] args) throws NoSuchAlgorithmException, SQLException, InterruptedException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        conn = DriverManager.getConnection("jdbc:sqlite:src/server/data.db");
        clients = new ArrayList<>();

        try (ServerSocket server = new ServerSocket(5555)) {

            while (true) {

                Socket client = server.accept();

                // TODO: make server accept login check for several clients in the same time

                DataInputStream dis = new DataInputStream(client.getInputStream());
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());

                String username = dis.readUTF();
                String password = dis.readUTF();

                md.update(password.getBytes());
                password = init.App.byte2hex(md.digest());

                if (checkLogin(username, password)) {
                    dos.writeUTF("success");
                    Client c = new Client(username, getFullName(username), client, dis, dos);
                    clients.add(c);
                    Sender sender = new Sender(c);
                    sender.start();
                    Receiver receiver = new Receiver(c, clients);
                    receiver.start();

                } else {
                    dos.writeUTF("fail");
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static String getFullName(String username) {
        String fullname = "";

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM clients WHERE username LIKE ?;");
            ps.setString(1, "%" + username + "%");
            if (ps.execute()) {
                ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    fullname = rs.getString("name");

                }
            } else {
                System.out.println("\nNothing found!\n");
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return fullname;
    }

    private static boolean checkLogin(String username, String password) {
        String usernameRes = "";
        String passRes = "";
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM clients WHERE username = ? AND password = ?;");
            ps.setString(1, username);
            ps.setString(2, password);
            if (ps.execute()) {

                ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    usernameRes = rs.getString("username");
                    passRes = rs.getString("password");

                }

            } else {
                System.out.println("\nWorng Criedentials\n");
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return username.equalsIgnoreCase(usernameRes) && password.equalsIgnoreCase(passRes);
    }

}
