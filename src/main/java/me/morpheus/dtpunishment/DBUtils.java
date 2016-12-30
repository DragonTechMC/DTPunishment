package me.morpheus.dtpunishment;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtils {


    public static String JDBC;

    private static SqlService sql;

    private static javax.sql.DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return sql.getDataSource(jdbcUrl);
    }

    public static int getBanpoints(String player) {
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            ResultSet set = conn.prepareStatement("SELECT Banpoints FROM dtpunishment WHERE PlayerName=\""+player+"\";").executeQuery();
            conn.close();
            set.next();
            return set.getInt("Banpoints");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getMutepoints(String player) {
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            ResultSet set = conn.prepareStatement("SELECT Mutepoints FROM dtpunishment WHERE PlayerName=\""+player+"\";").executeQuery();
            conn.close();
            set.next();
            return set.getInt("Mutepoints");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isMuted(String player) {
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            ResultSet set = conn.prepareStatement("SELECT isMuted FROM dtpunishment WHERE PlayerName=\""+player+"\";").executeQuery();
            conn.close();
            set.next();
            return set.getBoolean("isMuted");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getUntil(String player) {
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            ResultSet set = conn.prepareStatement("SELECT Until FROM dtpunishment WHERE PlayerName=\""+player+"\";").executeQuery();
            conn.close();
            set.next();
            return set.getString("Until");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void addBanpoints(String player, int amount){
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET Banpoints = Banpoints + "+amount+" WHERE PlayerName=\""+player+"\" LIMIT 1;").executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addMutepoints(String player, int amount){
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET Mutepoints = Mutepoints + "+amount+" WHERE PlayerName=\""+player+"\" LIMIT 1;").executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void mute(String player, String end){
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET isMuted = 1, Until = "+end+" WHERE PlayerName=\""+player+"\" LIMIT 1;").executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createUser(String player){
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            conn.prepareStatement("INSERT INTO dtpunishment (PlayerName,Banpoints,Mutepoints,isMuted)\n" +
                    "VALUES ('"+player+"',0,0,false)").executeQuery();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean userExists(String player){
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            ResultSet set = conn.prepareStatement("SELECT PlayerName FROM dtpunishment WHERE PlayerName=\""+player+"\";").executeQuery();
            conn.close();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void unmute(String player){
        try {
            Connection conn = getDataSource(JDBC).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET isMuted = 0, Until = null WHERE PlayerName=\""+player+"\" LIMIT 1;").executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

}
