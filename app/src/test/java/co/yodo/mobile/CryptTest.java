package co.yodo.mobile;

import static org.junit.Assert.*;

import org.junit.Test;

import co.yodo.mobile.helper.CryptUtils;

/**
 * Created by hei on 10/06/16.
 * Tests main functions for encryption
 */
public class CryptTest {

    /** Original strings */
    private static final String[] input = {
            "Boo2Ap1E", "xu4aeLoz", "ye0OoGo1", "ii3Ietho",
            "Ahdae7ch", "johb7Uge", "WaiJ2eil", "tahP1hi0",
            "nee8Thah", "yoophi3I", "eiX7reng", "iqui5ohX"
    };

    /** Hex strings */
    private static final String[] output = {
            "426f6f3241703145", "78753461654c6f7a", "7965304f6f476f31", "696933496574686f",
            "4168646165376368", "6a6f686237556765", "5761694a3265696c", "7461685031686930",
            "6e65653854686168", "796f6f7068693349", "6569583772656e67", "69717569356f6858"
    };

    @Test
    public void testHextTransformation() {
        for( int i = 0; i < input.length; i++ ) {
            final byte[] bytes = CryptUtils.hexToBytes( output[i] );
            final String actual = CryptUtils.bytesToHex( bytes );
            assertEquals( "Hex transformation failed", output[i], actual );
        }
    }

    @Test
    public void testBinaryToHex() {
        for( int i = 0; i < input.length; i++ ) {
            final String actual = CryptUtils.bytesToHex( input[i].getBytes() );
            assertEquals( "Hex transformation failed", output[i], actual );
        }
    }
}
