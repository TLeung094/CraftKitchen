package com.example.craftkitchen.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStore implements AutoCloseable {
    private final Connection connection;

    public PlayerDataStore(File dbFile) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_levels (" +
                "uuid TEXT PRIMARY KEY, " +
                "experience INTEGER NOT NULL DEFAULT 0)"
            );
        }
    }

    public void saveExperience(UUID playerId, int experience) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO player_levels (uuid, experience) VALUES (?, ?) " +
            "ON CONFLICT(uuid) DO UPDATE SET experience = excluded.experience")) {
            ps.setString(1, playerId.toString());
            ps.setInt(2, experience);
            ps.executeUpdate();
        }
    }

    public int loadExperience(UUID playerId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT experience FROM player_levels WHERE uuid = ?")) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("experience") : 0;
            }
        }
    }

    public Map<UUID, Integer> loadAll() throws SQLException {
        Map<UUID, Integer> all = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT uuid, experience FROM player_levels")) {
            while (rs.next()) {
                all.put(UUID.fromString(rs.getString("uuid")), rs.getInt("experience"));
            }
        }
        return all;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
