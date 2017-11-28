package redis.embedded;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

public class StateTest {

    @Test
    public void testSwitchState() {
        State state = new State();
        assertEquals("Initial state is inactive", RedisState.inactive, state.getState());
        assertTrue(state.checkAndSetState(RedisState.inactive, RedisState.activating));
        assertEquals(RedisState.activating, state.getState());

        assertFalse(state.checkAndSetState(RedisState.inactive, RedisState.failed));
        assertFalse(state.checkAndSetState(RedisState.active, RedisState.inactive));
        assertEquals(RedisState.activating, state.getState());

        assertTrue(state.checkAndSetState(RedisState.activating, RedisState.active));
        assertEquals(RedisState.active, state.getState());

        assertFalse(state.checkAndSetState(RedisState.inactive, RedisState.failed));
        assertFalse(state.checkAndSetState(RedisState.failed, RedisState.inactive));
        assertEquals(RedisState.active, state.getState());
    }

    private static final Set<RedisState> ACTIVATING_AND_ACTIVE = EnumSet.of(RedisState.activating, RedisState.active);

    @Test
    public void testSwitchFromMultipleStates() {
        State state = new State();
        assertFalse(state.checkAndSetState(ACTIVATING_AND_ACTIVE, RedisState.inactive));
        assertFalse(state.checkAndSetState(ACTIVATING_AND_ACTIVE, RedisState.active));

        assertTrue(state.checkAndSetState(RedisState.inactive, RedisState.activating));
        assertTrue(state.checkAndSetState(ACTIVATING_AND_ACTIVE, RedisState.active));
        assertEquals(RedisState.active, state.getState());
        assertTrue(state.checkAndSetState(ACTIVATING_AND_ACTIVE, RedisState.activating));
        assertEquals(RedisState.activating, state.getState());
        assertTrue(state.checkAndSetState(ACTIVATING_AND_ACTIVE, RedisState.failed));
        assertEquals(RedisState.failed, state.getState());
        assertFalse(state.checkAndSetState(ACTIVATING_AND_ACTIVE, RedisState.inactive));
    }

    @Test
    public void testStateMachine() {
        State state = new State();
        // Inactive
        assertFalse(state.isActivating());
        assertFalse(state.isActive());
        assertFalse(state.isDeactivating());

        assertFalse(state.setActive());
        assertFalse(state.setInactive());
        assertFalse(state.setDeactivating());
        assertFalse(state.setDeactivatingByTimeout());
        assertFalse(state.setFailed());

        // Activating
        assertTrue(state.setActivating());

        assertTrue(state.isActivating());
        assertFalse(state.isActive());
        assertFalse(state.isDeactivating());

        assertFalse(state.setInactive());
        assertFalse(state.setActivating());

//        assertFalse(state.setDeactivatingByTimeout());
//        assertFalse(state.setFailed());
//        assertFalse(state.setDeactivating());

        // Active
        assertTrue(state.setActive());

        assertTrue(state.isActive());
        assertFalse(state.isActivating());
        assertFalse(state.isDeactivating());

        assertFalse(state.setActive());
        assertFalse(state.setInactive());
        assertFalse(state.setDeactivatingByTimeout());
        assertFalse(state.setFailed());

        // Deactivating
        assertTrue(state.setDeactivating());

        assertFalse(state.isActivating());
        assertFalse(state.isActive());
        assertTrue(state.isDeactivating());

        assertFalse(state.setActive());
        assertFalse(state.setActivating());
        assertFalse(state.setDeactivating());
        assertFalse(state.setDeactivatingByTimeout());

//        assertFalse(state.setFailed());
//        assertFalse(state.setInactive());

        // Inactive
        assertTrue(state.setInactive());

        assertFalse(state.isActive());
        assertFalse(state.isActivating());
        assertFalse(state.isDeactivating());

        assertFalse(state.setActive());
        assertFalse(state.setInactive());
        assertFalse(state.setDeactivating());
        assertFalse(state.setDeactivatingByTimeout());
        assertFalse(state.setFailed());
//        assertTrue(state.setActivating());

        // Failed
        assertTrue(state.setActivating());
        assertTrue(state.setFailed());

        assertFalse(state.isActivating());
        assertFalse(state.isActive());
        assertFalse(state.isDeactivating());

        assertFalse(state.setActive());
        assertFalse(state.setInactive());
        assertFalse(state.setDeactivating());
        assertFalse(state.setDeactivatingByTimeout());
        assertFalse(state.setFailed());
//        assertTrue(state.setActivating());

    }


}
