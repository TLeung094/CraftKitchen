package com.example.craftkitchen;

import com.example.craftkitchen.level.LevelManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelManagerTest {

    @Test
    void shouldAccumulateExperienceAndLevelUp() {
        LevelManager manager = new LevelManager();
        UUID alice = UUID.randomUUID();

        assertEquals(1, manager.getLevel(alice));

        manager.addExperience(alice, 10);
        assertEquals(10, manager.getExperience(alice));
        assertEquals(1, manager.getLevel(alice));

        manager.addExperience(alice, 40);
        assertEquals(2, manager.getLevel(alice));
    }

    @Test
    void shouldReturnTitleForLevel() {
        LevelManager manager = new LevelManager();
        UUID alice = UUID.randomUUID();

        assertEquals("見習廚師", manager.getTitle(alice));

        manager.addExperience(alice, 250);
        assertEquals("家常廚師", manager.getTitle(alice));
    }

    @Test
    void shouldReportLevelUpsFromGains() {
        LevelManager manager = new LevelManager();
        UUID alice = UUID.randomUUID();

        List<Integer> ups = manager.addExperience(alice, 250);
        assertEquals(List.of(2, 3, 4, 5, 6), ups);

        assertEquals(List.of(), manager.addExperience(alice, 0));
    }

    @Test
    void shouldRespectEnabledFlag() {
        LevelManager manager = new LevelManager();
        manager.setEnabled(false);
        UUID alice = UUID.randomUUID();

        manager.addExperience(alice, 100);
        assertEquals(0, manager.getExperience(alice));
        assertEquals(1, manager.getLevel(alice));
    }
}
