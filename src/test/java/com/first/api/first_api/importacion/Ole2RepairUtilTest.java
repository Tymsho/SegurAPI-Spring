package com.first.api.first_api.importacion;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class Ole2RepairUtilTest {

    @Test
    void shouldReturnSameDataWhenNullOrTooSmall() {
        assertNull(Ole2RepairUtil.repairIfCorruptedOle2(null));
        byte[] small = new byte[100];
        assertSame(small, Ole2RepairUtil.repairIfCorruptedOle2(small));
    }

    @Test
    void shouldReturnSameDataWhenNotOle2Magic() {
        byte[] nonOle2 = new byte[2048];
        nonOle2[0] = 0x50; // 'P'
        nonOle2[1] = 0x4B; // 'K' (Zip magic)
        assertSame(nonOle2, Ole2RepairUtil.repairIfCorruptedOle2(nonOle2));
    }

    @Test
    void shouldRepairCorruptedOle2FatTable() {
        byte[] data = new byte[2048];
        // Set OLE2 Magic
        byte[] magic = new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1};
        System.arraycopy(magic, 0, data, 0, 8);

        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // firstDirSector at 48 = 0 -> dirOffset = (0 + 1) * 512 = 512
        buf.putInt(48, 0);
        // fatSector0 at 76 = 1 -> fatOffset = (1 + 1) * 512 = 1024
        buf.putInt(76, 1);

        int dirOffset = 512;
        int fatOffset = 1024;

        // Directory Entry 0: rootStartSec at dirOffset + 116 = 1
        buf.putInt(dirOffset + 116, 1);
        // rootSize at dirOffset + 120 = 512
        buf.putInt(dirOffset + 120, 512);

        // FAT sector 0: FAT[1] at fatOffset + 4 = 2 (corrupted pointer to Workbook)
        buf.putInt(fatOffset + 4, 2);

        byte[] repaired = Ole2RepairUtil.repairIfCorruptedOle2(data);
        assertNotNull(repaired);
        assertNotSame(data, repaired);

        ByteBuffer repBuf = ByteBuffer.wrap(repaired).order(ByteOrder.LITTLE_ENDIAN);
        // FAT[1] should now be -2 (ENDOFCHAIN)
        assertEquals(-2, repBuf.getInt(fatOffset + 4));
    }
}
