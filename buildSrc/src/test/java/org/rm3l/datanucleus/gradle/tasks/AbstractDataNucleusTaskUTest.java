package org.rm3l.datanucleus.gradle.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AbstractDataNucleusTaskUTest {

    @Test
    void testSetDescription_NotAllowed() {
        final AbstractDataNucleusTask dnTask = mock(AbstractDataNucleusTask.class);

        assertThrows(UnsupportedOperationException.class,
                () -> dnTask.setDescription("Overridden description: fake DN task"));
    }
}
