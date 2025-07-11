package me.youhavetrouble.inviter.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
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
            // Initialize the database schema if necessary
            // For example, you might want to create a table for guild settings
            connection.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS guild_settings (
                    guild_id LONG PRIMARY KEY,
                    api_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                    api_hostname VARCHAR(256) DEFAULT NULL
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
                boolean apiEnabled = resultSet.getBoolean("api_enabled");
                String apiHostname = resultSet.getString("api_hostname");
                return new GuildSettings(guildId, apiEnabled, apiHostname);
            }
            return new GuildSettings(guildId,false, null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve guild settings", e);

        }
    }

    @Nullable
    @Override
    public GuildSettings getGuildSettings(@NotNull String hostname) {

        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement(
                "SELECT * FROM guild_settings WHERE api_hostname = ?"
            );
            statement.setString(1, hostname);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                long guildId = resultSet.getLong("guild_id");
                boolean apiEnabled = resultSet.getBoolean("api_enabled");
                return new GuildSettings(guildId, apiEnabled, hostname);
            }
            return null; // No settings found for the given hostname
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve guild settings by hostname", e);
        }
    }

    @Override
    public void saveDefaultGuildSettings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO guild_settings (guild_id) VALUES (?)"
            );
            statement.setLong(1, guildId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save default guild settings", e);
        }
    }

    @Override
    public void updateDiscordApiEnabled(long guildId, boolean enabled) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement(
                "UPDATE guild_settings SET api_enabled = ? WHERE guild_id = ?"
            );
            statement.setBoolean(1, enabled);
            statement.setLong(2, guildId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update Discord API enabled status", e);
        }

    }

    @Override
    public void updateDiscordApiHostname(long guildId, @Nullable String hostname) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement(
                "UPDATE guild_settings SET api_hostname = ? WHERE guild_id = ?"
            );
            if (hostname == null || hostname.isEmpty()) {
                statement.setNull(1, java.sql.Types.VARCHAR);
            } else {
                statement.setString(1, hostname);
            }
            statement.setLong(2, guildId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update Discord API hostname", e);
        }
    }
}
