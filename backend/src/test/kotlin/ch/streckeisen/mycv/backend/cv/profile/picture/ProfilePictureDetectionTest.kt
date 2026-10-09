package ch.streckeisen.mycv.backend.cv.profile.picture

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.tika.mime.MediaType
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.web.MockMultipartFile
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO

class ProfilePictureDetectionTest {
    private val storage = spyk(ProfilePictureStorageService("test-key", "test-secret", "test-cloud", 60, MockEnvironment()))
    private val service = ProfilePictureService(storage, mockk(relaxed = true))

    init {
        every { storage.store(any()) } answers { throw AssertionError("Unexpected upload") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["png", "jpeg"])
    fun `detects real images and preserves the full upload`(format: String) {
        val bytes = imageBytes(format)
        val stream = BufferedInputStream(bytes.inputStream())

        assertEquals(MediaType.image(format), storage.detectContentType(stream))
        assertThrows(IOException::class.java) { stream.read() }

        // A misleading filename and declared type must not override content detection.
        val upload = MockMultipartFile("profilePicture", "picture.txt", "text/plain", bytes)
        every { storage.store(upload) } answers {
            upload.inputStream.use { assertArrayEquals(bytes, it.readAllBytes()) }
            Result.success("stored-picture")
        }

        assertEquals("stored-picture", service.store(1, upload, null).getOrThrow())
        verify(exactly = 1) { storage.store(upload) }
    }

    @Test
    fun `rejects text disguised as a PNG without uploading`() {
        val upload = MockMultipartFile("profilePicture", "picture.png", "image/png", "not an image".toByteArray())

        val result = service.store(1, upload, null)

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(exactly = 0) { storage.store(any()) }
    }

    @Test
    fun `rejects a real image outside the allowed formats`() {
        val upload = MockMultipartFile("profilePicture", "picture.gif", "image/gif", imageBytes("gif"))

        val result = service.store(1, upload, null)

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(exactly = 0) { storage.store(any()) }
    }

    @Test
    fun `closes the detection stream even when reading fails`() {
        var closed = false
        val input = object : InputStream() {
            override fun read(): Int = throw IOException("read failed")
            override fun close() { closed = true }
        }

        assertThrows(IOException::class.java) { storage.detectContentType(BufferedInputStream(input)) }

        assertTrue(closed)
    }

    private fun imageBytes(format: String): ByteArray = ByteArrayOutputStream().use { output ->
        assertTrue(ImageIO.write(BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, output))
        output.toByteArray()
    }
}
