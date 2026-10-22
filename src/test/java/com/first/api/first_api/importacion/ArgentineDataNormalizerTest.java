package com.first.api.first_api.importacion;

import com.first.api.first_api.models.Sexo;
import com.first.api.first_api.models.TipoIva;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgentineDataNormalizerTest {

    @Test
    void shouldStripAccentsAndClean() {
        assertEquals("CORDOBA", ArgentineDataNormalizer.stripAccentsAndClean("  córdoba  "));
        assertEquals("NEUQUEN CAPITAL", ArgentineDataNormalizer.stripAccentsAndClean("Neuquén    Capital"));
        assertEquals("", ArgentineDataNormalizer.stripAccentsAndClean(null));
    }

    @Test
    void shouldNormalizeCompanyOrPersonKeyMatchingVariants() {
        String key1 = ArgentineDataNormalizer.normalizeCompanyOrPersonKey("LA BUENA SUERTE SOC COL");
        String key2 = ArgentineDataNormalizer.normalizeCompanyOrPersonKey("LA BUENA SUERTE S.C.");
        String key3 = ArgentineDataNormalizer.normalizeCompanyOrPersonKey("La Buena Suerte S.A.");

        assertEquals("LA BUENA SUERTE", key1);
        assertEquals("LA BUENA SUERTE", key2);
        assertEquals("LA BUENA SUERTE", key3);
    }

    @Test
    void shouldExtractDocumentoForPersonaJuridica() {
        var res1 = ArgentineDataNormalizer.extractDocumento("30-99903475-2", "Empresa Ejemplo S.A.");
        assertEquals("30999034752", res1.dniLimpio());
        assertTrue(res1.esPersonaJuridica());
        assertEquals(Sexo.OTRO, res1.sexoInferido());
        assertEquals(TipoIva.RESPONSABLE_INSCRIPTO, res1.ivaInferido());

        var resNobleConComa = ArgentineDataNormalizer.extractDocumento("30999034752,", "Empresa Ejemplo S.R.L.");
        assertEquals("30999034752", resNobleConComa.dniLimpio());
        assertTrue(resNobleConComa.esPersonaJuridica());
    }

    @Test
    void shouldExtractDocumentoForPersonaFisicaCuil() {
        var resMasc = ArgentineDataNormalizer.extractDocumento("20-30123456-4", "Perez Juan");
        assertEquals("30123456", resMasc.dniLimpio());
        assertFalse(resMasc.esPersonaJuridica());
        assertEquals(Sexo.MASCULINO, resMasc.sexoInferido());
        assertEquals(TipoIva.CONSUMIDOR_FINAL, resMasc.ivaInferido());

        var resFem = ArgentineDataNormalizer.extractDocumento("27211401945, 21140194", "Gomez Maria");
        assertEquals("21140194", resFem.dniLimpio());
        assertFalse(resFem.esPersonaJuridica());
        assertEquals(Sexo.FEMENINO, resFem.sexoInferido());
    }

    @Test
    void shouldExtractDocumentoFromDniDirecto() {
        var res = ArgentineDataNormalizer.extractDocumento("35.123.456", "Lopez Carlos");
        assertEquals("35123456", res.dniLimpio());
        assertFalse(res.esPersonaJuridica());

        var resClub = ArgentineDataNormalizer.extractDocumento("12345678", "Club Atletico Pringles");
        assertEquals("12345678", resClub.dniLimpio());
        assertTrue(resClub.esPersonaJuridica());
        assertEquals(TipoIva.RESPONSABLE_INSCRIPTO, resClub.ivaInferido());
    }

    @Test
    void shouldNormalizeProvinciaName() {
        assertEquals("CIUDAD AUTONOMA DE BUENOS AIRES", ArgentineDataNormalizer.normalizeProvinciaName("CABA", ""));
        assertEquals("CIUDAD AUTONOMA DE BUENOS AIRES", ArgentineDataNormalizer.normalizeProvinciaName("CAPITAL FEDERAL", ""));
        assertEquals("CIUDAD AUTONOMA DE BUENOS AIRES", ArgentineDataNormalizer.normalizeProvinciaName("", "CAPITAL"));
        assertEquals("BUENOS AIRES", ArgentineDataNormalizer.normalizeProvinciaName("BS. AS.", ""));
        assertEquals("CORDOBA", ArgentineDataNormalizer.normalizeProvinciaName("CBA", ""));
        assertEquals("BUENOS AIRES", ArgentineDataNormalizer.normalizeProvinciaName("", ""));
    }

    @Test
    void shouldNormalizeLocalidadName() {
        assertEquals("PILAR", ArgentineDataNormalizer.normalizeLocalidadName("Pilar (Bs. As.)"));
        assertEquals("FLORIDA", ArgentineDataNormalizer.normalizeLocalidadName("Florida (Vicente Lopez))"));
        assertEquals("CORONEL PRINGLES", ArgentineDataNormalizer.normalizeLocalidadName("Cnel. Pringles"));
        assertEquals("GENERAL SAN MARTIN", ArgentineDataNormalizer.normalizeLocalidadName("Gral. San Martin"));
        assertEquals("CAPITAL FEDERAL", ArgentineDataNormalizer.normalizeLocalidadName("CABA"));
    }
}
