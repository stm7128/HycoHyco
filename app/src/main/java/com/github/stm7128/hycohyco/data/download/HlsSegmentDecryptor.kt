package com.github.stm7128.hycohyco.data.download

import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * HLS AES-128 暗号化セグメントの復号ユーティリティ
 */
object HlsSegmentDecryptor {

    /**
     * 暗号化されたセグメントバイト配列を AES-128-CBC で復号する
     *
     * @param encryptedData 暗号化バイナリデータ
     * @param key 16バイトの AES 暗号化鍵
     * @param iv 16バイトの初期化ベクトル (IV)
     * @return 復号された平文バイナリデータ
     */
    fun decrypt(
        encryptedData: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(encryptedData)
    }

    /**
     * 16進数文字列 (例: "0x0123456789ABCDEF..." または "0123...") を 16 バイト配列に変換する
     */
    fun parseIvHex(ivHex: String): ByteArray {
        val clean = ivHex.removePrefix("0x").removePrefix("0X").padStart(32, '0')
        val bytes = ByteArray(16)
        for (i in 0 until 16) {
            val index = i * 2
            bytes[i] = clean.substring(index, index + 2).toInt(16).toByte()
        }
        return bytes
    }

    /**
     * セグメントシーケンス番号から 16 バイト Big-Endian の IV を生成する (RFC 8216 準拠)
     */
    fun createSequenceIv(sequenceNumber: Long): ByteArray {
        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(0L) // 上位 8 バイトは 0
        buffer.putLong(sequenceNumber) // 下位 8 バイトにシーケンス番号
        return buffer.array()
    }
}
