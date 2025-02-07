package ch.streckeisen.mycv.backend.cv.profile.picture

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.tika.mime.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfilePictureServiceTest {
    private lateinit var profilePictureStorageService: ProfilePictureStorageService
    private lateinit var profilePictureService: ProfilePictureService

    @BeforeEach
    fun setup() {
        profilePictureStorageService = mockk()
        profilePictureService = ProfilePictureService(profilePictureStorageService, mockk(relaxed = true))
    }

    @Test
    fun testStoreWithEmptyFile() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns true
        }

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IllegalArgumentException }
        verify(exactly = 0) { profilePictureStorageService.store(any()) }
    }

    @Test
    fun testStoreWithNotAllowedMediaType() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { profilePictureStorageService.detectContentType(any()) } returns MediaType.OCTET_STREAM

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IllegalArgumentException }
        verify(exactly = 0) { profilePictureStorageService.store(any()) }
    }

    @Test
    fun testStore() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { profilePictureStorageService.detectContentType(any()) } returns MediaType.image("jpg")
        every { profilePictureStorageService.store(eq(file)) } returns Result.success("1.jpg")

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isSuccess }
        val pictureName = result.getOrNull()
        assertEquals("1.jpg", pictureName)
        verify(exactly = 1) { profilePictureStorageService.store(eq(file)) }
        verify(exactly = 0) { profilePictureStorageService.delete(any()) }
    }

    @Test
    fun testStoreCopyFails() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { profilePictureStorageService.detectContentType(any()) } returns MediaType.image("jpg")
        every { profilePictureStorageService.store(eq(file)) } returns Result.failure(IOException("Error"))

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IOException }
        verify(exactly = 1) { profilePictureStorageService.store(eq(file)) }
        verify(exactly = 0) { profilePictureStorageService.delete(any()) }
    }

    @Test
    fun testStoreWithOldPicture() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { profilePictureStorageService.detectContentType(any()) } returns MediaType.image("jpg")
        every { profilePictureStorageService.store(eq(file)) } returns Result.success("1.jpg")
        every { profilePictureStorageService.delete(eq("1.png")) } returns Result.success(Unit)

        val result = profilePictureService.store(1, file, "1.png")

        assertTrue { result.isSuccess }
        val pictureName = result.getOrNull()
        assertEquals("1.jpg", pictureName)
        verify(exactly = 1) { profilePictureStorageService.store(eq(file)) }
        verify(exactly = 1) { profilePictureStorageService.delete(eq("1.png")) }
    }

    @Test
    fun testStoreWithOldPictureDeleteFails() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { profilePictureStorageService.detectContentType(any()) } returns MediaType.image("jpg")
        every { profilePictureStorageService.store(eq(file)) } returns Result.success("1.jpg")
        every { profilePictureStorageService.delete(eq("1.png")) } returns Result.failure(IOException("Error"))

        val result = profilePictureService.store(1, file, "1.png")

        assertTrue { result.isSuccess }
        val pictureName = result.getOrNull()
        assertEquals("1.jpg", pictureName)
        verify(exactly = 1) { profilePictureStorageService.store(eq(file)) }
        verify(exactly = 1) { profilePictureStorageService.delete(eq("1.png")) }
    }
}