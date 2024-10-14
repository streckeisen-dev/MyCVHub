package ch.streckeisen.mycv.backend.files

import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.name

@Service
class FileService {
    fun detectContentType(stream: BufferedInputStream): MediaType {
        val detector = DefaultDetector()
        val metadata = Metadata()

        return detector.detect(stream, metadata)
    }

    fun copyFile(file: MultipartFile, target: Path): Result<Unit> {
        file.inputStream.use { inputStream ->
            try {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING)
            } catch (ex: IOException) {
                return Result.failure(ex)
            }
        }
        return Result.success(Unit)
    }

    fun deleteFile(file: Path): Result<Unit> {
        try {
            Files.delete(file)
        } catch (ex: IOException) {
            return Result.failure(IOException("Failed to delete file ${file.name}", ex))
        }
        return Result.success(Unit)
    }
}