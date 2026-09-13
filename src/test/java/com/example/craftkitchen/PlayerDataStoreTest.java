package com.example.craftkitchen;

import com.example.craftkitchen.database.PlayerDataStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataStoreTest {

    @TempDir
    File tempDir;

    @Test
    void shouldPersistAndReloadExperience() throws Exception {
        File dbFile = new File(tempDir, "data.db");
        UUID alice = UUID.randomUUID();

        try (PlayerDataStore store = new PlayerDataStore(dbFile)) {
            store.saveExperience(alice, 120);
        }

        try (PlayerDataStore store = new PlayerDataStore(dbFile)) {
            assertEquals(120, store.loadExperience(alice));
        }
    }

    @Test
    void shouldReturnZeroForUnknownPlayer() throws Exception {
        try (PlayerDataStore store = new PlayerDataStore(new File(tempDir, "data.db"))) {
            assertEquals(0, store.loadExperience(UUID.randomUUID()));
        }
    }

    @Test
    void shouldLoadAllRecords() throws Exception {
        File dbFile = new File(tempDir, "data.db");
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();

        try (PlayerDataStore store = new PlayerDataStore(dbFile)) {
            store.saveExperience(alice, 50);
            store.saveExperience(bob, 250);
        }

        try (PlayerDataStore store = new PlayerDataStore(dbFile)) {
            var all = store.loadAll();
            assertEquals(2, all.size());
            assertEquals(50, all.get(alice));
            assertEquals(250, all.get(bob));
        }
    }

    @Test
    void shouldOverwriteExistingExperience() throws Exception {
        File dbFile = new File(tempDir, "data.db");
        UUID alice = UUID.randomUUID();

        try (PlayerDataStore store = new PlayerDataStore(dbFile)) {
            store.saveExperience(alice, 50);
            store.saveExperience(alice, 99);
            assertEquals(99, store.loadExperience(alice));
        }
        assertTrue(dbFile.exists());
    }
}
