package com.objectcomputing.checkins.services.action_item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionItemBadArgExceptionTest {

    @Test
    void testExceptionMessage() {
        final String message = "Hello world";
        ActionItemBadArgException argException = new ActionItemBadArgException(message);
        assertEquals(argException.getMessage(), message);
    }

}
