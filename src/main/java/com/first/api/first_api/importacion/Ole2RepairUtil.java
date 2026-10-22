package com.first.api.first_api.importacion;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Utilidad de bajo nivel para reparar en memoria archivos BIFF8 (.xls) exportados
 * con tabla FAT corrupta (ejemplo: Noble Seguros, donde el stream Root Entry
 * tiene un sector asignado en FAT[1] que apunta erróneamente al sector 2 / stream Workbook).
 */
public final class Ole2RepairUtil {

    private static final byte[] OLE2_MAGIC = new byte[]{
        (byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0,
        (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1
    };

    private Ole2RepairUtil() {
    }

    public static byte[] repairIfCorruptedOle2(byte[] data) {
        if (data == null || data.length < 1536) {
            return data;
        }

        for (int i = 0; i < OLE2_MAGIC.length; i++) {
            if (data[i] != OLE2_MAGIC[i]) {
                return data;
            }
        }

        try {
            byte[] copy = Arrays.copyOf(data, data.length);
            ByteBuffer buf = ByteBuffer.wrap(copy).order(ByteOrder.LITTLE_ENDIAN);

            int firstDirSector = buf.getInt(48);
            int fatSector0 = buf.getInt(76);

            int dirOffset = (firstDirSector + 1) * 512;
            int fatOffset = (fatSector0 + 1) * 512;

            if (dirOffset < 0 || dirOffset + 128 > copy.length || fatOffset < 0 || fatOffset + 16 > copy.length) {
                return data;
            }

            int rootStartSec = buf.getInt(dirOffset + 116);
            int rootSize = buf.getInt(dirOffset + 120);

            if (rootStartSec == 1 && rootSize <= 512 && buf.getInt(fatOffset + 4) == 2) {
                buf.putInt(fatOffset + 4, -2); // 0xFFFFFFFE (ENDOFCHAIN)
                return copy;
            }

            return data;
        } catch (Exception e) {
            return data;
        }
    }
}
