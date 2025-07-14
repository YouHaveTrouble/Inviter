package me.youhavetrouble.inviter.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
                    api_enabled BOOLEAN NOT NULL DEFAULT FALSE
                );
                """
            );
            connection.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS hostnames (
                    hostname VARCHAR(256) PRIMARY KEY,
                    guild_id LONG NOT NULL,
                    failed_checks INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (guild_id) REFERENCES guild_settings(guild_id) ON DELETE CASCADE
                )
                """);
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
                String apiHostname = resultSet.getString("hostname");
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
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM guild_settings WHERE hostname = ?"
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
    public void updateDiscordApiEnabled(long guildId, boolean enabled) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
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
    public void addHostname(long guildId, @Nullable String hostname) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO hostnames (hostname, guild_id) VALUES (?, ?)"
            );
            statement.setString(1, hostname);
            statement.setLong(2, guildId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add hostname", e);
        }
    }

    @Override
    public void removeHostname(@NotNull String hostname) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM hostnames WHERE hostname = ?"
            );
            statement.setString(1, hostname);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove hostname", e);
        }

    }

    @Override
    public List<String> listHostnames(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT hostname FROM hostnames WHERE guild_id = ?"
            );
            statement.setLong(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            List<String> hostnames = new java.util.ArrayList<>();
            while (resultSet.next()) {
                hostnames.add(resultSet.getString("hostname"));
            }
            return hostnames;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list hostnames", e);
        }
    }

    @Override
    public void cleanUpHostnames() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM hostnames WHERE failed_checks >= 3"
            );
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean up hostnames", e);
        }
    }

}
