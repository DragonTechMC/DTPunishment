package me.morpheus.dtpunishment.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.configuration.MainConfig;

public class MySqlDataStore implements DataStore {

    @Inject
    private MainConfig mainConfig;

    private SqlService sql;

    private DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (!Optional.ofNullable(sql).isPresent())
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        return sql.getDataSource(jdbcUrl);
    }

    private String getJdbcUrl() {
        String name = mainConfig.database.name;
        String host = mainConfig.database.host;
        int port = mainConfig.database.port;
        String user = mainConfig.database.username;
        String pass = mainConfig.database.password;

        return String.format("jdbc:mysql://%s:%s@%s:%d/%s", user, pass, host, port, name);
    }

    @Override
    public void init() {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS dtpunishment (\n" + "ID int NOT NULL AUTO_INCREMENT,\n"
                    + "UUID varchar(64) NOT NULL,\n" + "Banpoints SMALLINT,\n" + "BanUpdatedAt VARCHAR(32),\n"
                    + "Mutepoints SMALLINT,\n" + "MuteUpdatedAt VARCHAR(32),\n" + "IsMuted BOOLEAN,\n"
                    + "Until VARCHAR(32),\n" + "PRIMARY KEY (ID)\n" + ")").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getBanpoints(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn.prepareStatement("SELECT Banpoints FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            set.next();
            return set.getInt("Banpoints");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public LocalDate getBanpointsUpdatedAt(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn
                    .prepareStatement("SELECT BanUpdatedAt FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            set.next();
            return LocalDate.parse(set.getString("BanUpdatedAt"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getMutepoints(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn.prepareStatement("SELECT Mutepoints FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            set.next();
            return set.getInt("Mutepoints");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public LocalDate getMutepointsUpdatedAt(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn
                    .prepareStatement("SELECT MuteUpdatedAt FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            set.next();
            return LocalDate.parse(set.getString("MuteUpdatedAt"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isMuted(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn.prepareStatement("SELECT IsMuted FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            set.next();
            return set.getBoolean("IsMuted");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Instant getExpiration(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn.prepareStatement("SELECT Until FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            set.next();
            return Instant.parse(set.getString("Until"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addBanpoints(UUID player, int amount) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement(
                    "UPDATE dtpunishment SET Banpoints = Banpoints + " + amount + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
            conn.prepareStatement(
                    "UPDATE dtpunishment SET BanUpdatedAt = " + LocalDate.now() + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void removeBanpoints(UUID player, int amount) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement(
                    "UPDATE dtpunishment SET Banpoints = Banpoints - " + amount + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMutepoints(UUID player, int amount) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement(
                    "UPDATE dtpunishment SET Mutepoints = Mutepoints + " + amount + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
            conn.prepareStatement(
                    "UPDATE dtpunishment SET MuteUpdatedAt = " + LocalDate.now() + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMutepoints(UUID player, int amount) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement(
                    "UPDATE dtpunishment SET Mutepoints = Mutepoints - " + amount + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mute(UUID player, Instant expiration) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement(
                    "UPDATE dtpunishment SET IsMuted = 1, Until = " + expiration + " WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unmute(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement("UPDATE dtpunishment SET IsMuted = 0, Until = null WHERE UUID=\"" + player + "\";")
                    .executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createUser(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            conn.prepareStatement("INSERT INTO dtpunishment (UUID,Banpoints,Mutepoints,IsMuted)\n" + "VALUES ('"
                    + player + "',0,0,false)").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean userExists(UUID player) {
        try (Connection conn = getDataSource(getJdbcUrl()).getConnection()) {
            ResultSet set = conn.prepareStatement("SELECT UUID FROM dtpunishment WHERE UUID=\"" + player + "\";")
                    .executeQuery();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
