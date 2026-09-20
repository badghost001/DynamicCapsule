package com.dynamiccapsule.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CapsuleStateTest {
    @Test
    fun defaultStateIsIdle() {
        assertEquals(CapsuleKind.IDLE, CapsuleState().kind)
        assertEquals("No live activity", CapsuleState().subtitle)
    }
}
