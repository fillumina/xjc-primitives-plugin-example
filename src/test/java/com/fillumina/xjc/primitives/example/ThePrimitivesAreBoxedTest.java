package com.fillumina.xjc.primitives.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.meter.MeterType;
import org.junit.jupiter.api.Test;

/**
 * The functional test of this wiring, and the description of what the plugin does to
 * {@code src/main/xsd/meter.xsd}.
 *
 * <p>The build has already run the plugin and compiled what it wrote, so a plugin that writes
 * something that does not compile fails before this test runs. What is left to check is that the
 * fields that were primitives are boxed, that the getter and the setter followed, and that a field
 * the selector leaves alone keeps its primitive type.
 */
class ThePrimitivesAreBoxedTest {

    @Test
    void everyPrimitiveFieldIsBoxed() throws Exception {
        assertEquals(Long.class, fieldType("reading"));
        assertEquals(Short.class, fieldType("precision"));
        assertEquals(Double.class, fieldType("factor"));
        assertEquals(Float.class, fieldType("offset"));
        assertEquals(Boolean.class, fieldType("active"));
        assertEquals(Boolean.class, fieldType("calibrated"));
    }

    @Test
    void aFieldTheSelectorLeavesAloneKeepsItsPrimitiveType() throws Exception {
        assertEquals(byte.class, fieldType("scale"));
        assertEquals(String.class, fieldType("serial"));
    }

    @Test
    void theGetterAndTheSetterFollowTheField() throws Exception {
        assertEquals(Long.class, MeterType.class.getMethod("getReading").getReturnType());
        assertEquals(void.class, MeterType.class.getMethod("setReading", Long.class).getReturnType());
        assertEquals(Boolean.class, MeterType.class.getMethod("isActive").getReturnType());
        assertEquals(void.class, MeterType.class.getMethod("setActive", Boolean.class).getReturnType());
        // the primitive signature is not declared any more
        assertThrows(NoSuchMethodException.class,
                () -> MeterType.class.getMethod("setReading", long.class));
        // and where the selector left the field alone it is still the primitive one
        assertEquals(byte.class, MeterType.class.getMethod("getScale").getReturnType());
        assertEquals(void.class, MeterType.class.getMethod("setScale", byte.class).getReturnType());
    }

    @Test
    void anAbsentValueIsNullAndNotZero() throws Exception {
        MeterType meter = new MeterType();

        assertNull(meter.getReading());
        assertNull(meter.isActive());
        assertNull(meter.isCalibrated());

        meter.setReading(0L);
        assertEquals(Long.valueOf(0L), meter.getReading());
    }

    private static Class<?> fieldType(String name) throws Exception {
        return MeterType.class.getDeclaredField(name).getType();
    }
}
