package ch.streckeisen.mycv.backend.cv.profile.picture

import ch.streckeisen.mycv.backend.files.FileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.io.FileUtils
import org.apache.tika.mime.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfilePictureServiceTest {
    private lateinit var fileService: FileService
    private lateinit var profilePictureService: ProfilePictureService

    @BeforeEach
    fun setup() {
        fileService = mockk()
        profilePictureService = ProfilePictureService(FileUtils.getTempDirectoryPath(), fileService)
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
        verify(exactly = 0) { fileService.copyFile(any(), any()) }
    }

    @Test
    fun testStoreWithNotAllowedMediaType() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { fileService.detectContentType(any()) } returns MediaType.OCTET_STREAM

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IllegalArgumentException }
        verify(exactly = 0) { fileService.copyFile(any(), any()) }
    }

    @Test
    fun testStore() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { fileService.detectContentType(any()) } returns MediaType.image("jpg")
        val newPicture = Paths.get(FileUtils.getTempDirectoryPath()).resolve("1.jpg")
        every { fileService.copyFile(eq(file), eq(newPicture)) } returns Result.success(Unit)

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isSuccess }
        val pictureName = result.getOrNull()
        assertEquals("1.jpg", pictureName)
        verify(exactly = 1) { fileService.copyFile(eq(file), eq(newPicture)) }
        verify(exactly = 0) { fileService.deleteFile(any()) }
    }

    @Test
    fun testStoreCopyFails() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        every { fileService.detectContentType(any()) } returns MediaType.image("jpg")
        val newPicture = Paths.get(FileUtils.getTempDirectoryPath()).resolve("1.jpg")
        every { fileService.copyFile(eq(file), eq(newPicture)) } returns Result.failure(IOException("Error"))

        val result = profilePictureService.store(1, file, null)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IOException }
        verify(exactly = 1) { fileService.copyFile(eq(file), eq(newPicture)) }
        verify(exactly = 0) { fileService.deleteFile(any()) }
    }

    @Test
    fun testStoreWithOldPicture() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        val newPicture = Paths.get(FileUtils.getTempDirectoryPath()).resolve("1.jpg")
        val oldPicture = Paths.get(FileUtils.getTempDirectoryPath()).resolve("1.png")
        every { fileService.detectContentType(any()) } returns MediaType.image("jpg")
        every { fileService.copyFile(eq(file), eq(newPicture)) } returns Result.success(Unit)
        every { fileService.deleteFile(eq(oldPicture)) } returns Result.success(Unit)

        val result = profilePictureService.store(1, file, "1.png")

        assertTrue { result.isSuccess }
        val pictureName = result.getOrNull()
        assertEquals("1.jpg", pictureName)
        verify(exactly = 1) { fileService.copyFile(eq(file), eq(newPicture)) }
        verify(exactly = 1) { fileService.deleteFile(eq(oldPicture)) }
    }

    @Test
    fun testStoreWithOldPictureDeleteFails() {
        val file = mockk<MultipartFile> {
            every { isEmpty } returns false
            every { inputStream } returns ByteArrayInputStream(ByteArray(1))
        }
        val newPicture = Paths.get(FileUtils.getTempDirectoryPath()).resolve("1.jpg")
        val oldPicture = Paths.get(FileUtils.getTempDirectoryPath()).resolve("1.png")
        every { fileService.detectContentType(any()) } returns MediaType.image("jpg")
        every { fileService.copyFile(eq(file), eq(newPicture)) } returns Result.success(Unit)
        every { fileService.deleteFile(eq(oldPicture)) } returns Result.failure(IOException("Error"))

        val result = profilePictureService.store(1, file, "1.png")

        assertTrue { result.isSuccess }
        val pictureName = result.getOrNull()
        assertEquals("1.jpg", pictureName)
        verify(exactly = 1) { fileService.copyFile(eq(file), eq(newPicture)) }
        verify(exactly = 1) { fileService.deleteFile(eq(oldPicture)) }
    }
}