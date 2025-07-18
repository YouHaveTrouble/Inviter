package me.youhavetrouble.inviter.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteStorage implements Storage {

    private final DataSource dataSource;

    public SqliteStorage() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new RuntimeException("Failed to create data folder");
            }
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("DataSQLitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:data/data.db");
        config.setConnectionTestQuery("PRAGMA journal_mode=WAL;");
        config.setMaxLifetime(60000); // 60 Sec
        config.setMaximumPoolSize(Math.min(4, Runtime.getRuntime().availableProcessors() / 4));
        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS guild_settings (
                    guild_id LONG PRIMARY KEY,
                    invites_enabled BOOLEAN NOT NULL DEFAULT TRUE
                );
                """
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }

    }


    @NotNull
    @Override
    public GuildSettings getGuildSettings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement(
                "SELECT * FROM guild_settings WHERE guild_id = ?"
            );
            statement.setLong(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean invitesEnabled = resultSet.getBoolean("invites_enabled");
                return new GuildSettings(guildId, invitesEnabled);
            }
            return new GuildSettings(guildId,true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve guild settings", e);

        }
    }

    @Override
    public void saveDefaultGuildSettings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO guild_settings (guild_id) VALUES (?)"
            );
            statement.setLong(1, guildId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save default guild settings", e);
        }
    }

    @Override
    public void removeGuildSettings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM guild_settings WHERE guild_id = ?"
            );
            statement.setLong(1, guildId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove guild settings", e);
        }
    }

    @Override
    public void updateInvitesEnabled(long guildId, boolean enabled) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "UPDATE guild_settings SET invites_enabled = ? WHERE guild_id = ?"
            );
            statement.setBoolean(1, enabled);
            statement.setLong(2, guildId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update Discord API enabled status", e);
        }

    }

}
