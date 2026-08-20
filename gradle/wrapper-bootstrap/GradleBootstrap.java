import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Text-only bootstrap used only because the repository was created without a
 * binary gradle-wrapper.jar. It downloads the pinned Gradle distribution,
 * verifies SHA-256, expands it under GRADLE_USER_HOME and delegates to Gradle.
 * Once Gradle is available, running `./gradlew wrapper` may replace this with
 * the standard generated wrapper if desired.
 */
public class GradleBootstrap {
    private static final String VERSION = "9.5.0";
    private static final String URL = "https://services.gradle.org/distributions/gradle-9.5.0-bin.zip";
    private static final String SHA256 = "553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746";

    public static void main(String[] args) throws Exception {
        Path home = Path.of(System.getenv().getOrDefault("GRADLE_USER_HOME",
                Path.of(System.getProperty("user.home"), ".gradle").toString()));
        Path distRoot = home.resolve("wrapper/dists/gradle-" + VERSION + "-bin/fd-bootstrap");
        Path gradleHome = distRoot.resolve("gradle-" + VERSION);
        String exe = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "gradle.bat" : "gradle";
        Path gradle = gradleHome.resolve("bin").resolve(exe);

        if (!Files.isRegularFile(gradle)) {
            Files.createDirectories(distRoot);
            Path zip = distRoot.resolve("gradle-" + VERSION + "-bin.zip");
            if (!Files.isRegularFile(zip) || !SHA256.equalsIgnoreCase(sha256(zip))) {
                download(zip);
            }
            String actual = sha256(zip);
            if (!SHA256.equalsIgnoreCase(actual)) {
                throw new SecurityException("Gradle distribution checksum mismatch: " + actual);
            }
            unzip(zip, distRoot);
        }

        List<String> command = new ArrayList<>();
        command.add(gradle.toAbsolutePath().toString());
        command.addAll(Arrays.asList(args));
        Process process = new ProcessBuilder(command)
                .directory(new File(System.getProperty("user.dir")))
                .inheritIO()
                .start();
        System.exit(process.waitFor());
    }

    private static void download(Path target) throws Exception {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).GET().build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(target));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Gradle download failed with HTTP " + response.statusCode());
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) >= 0) digest.update(buffer, 0, read);
        }
        StringBuilder out = new StringBuilder();
        for (byte b : digest.digest()) out.append(String.format("%02x", b));
        return out.toString();
    }

    private static void unzip(Path zip, Path destination) throws Exception {
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                Path output = destination.resolve(entry.getName()).normalize();
                if (!output.startsWith(destination)) throw new SecurityException("Zip traversal blocked");
                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());
                    Files.copy(in, output, StandardCopyOption.REPLACE_EXISTING);
                    if (output.getFileName().toString().equals("gradle")) output.toFile().setExecutable(true);
                }
            }
        }
    }
}
