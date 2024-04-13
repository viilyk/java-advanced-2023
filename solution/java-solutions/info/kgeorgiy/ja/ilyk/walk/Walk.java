package info.kgeorgiy.ja.ilyk.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    public void walk(String input, String output) {
        MessageDigest digest;
        Path inputPath, outputPath;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            inputPath = Path.of(input);
            outputPath = Path.of(output);
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Can't use sha-256 hash, " + e.getMessage());
            return;
        } catch (InvalidPathException | IOException e) {
            System.err.println("Got wrong path, " + e.getMessage());
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                String file;
                while ((file = reader.readLine()) != null) {
                    try (
                            InputStream fileInputStream = Files.newInputStream(Path.of(file))
                    ) {
                        digest.reset();
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) > 0) {
                            digest.update(buffer, 0, bytesRead);
                        }
                        byte[] digestBytes = digest.digest();
                        BigInteger hash = new BigInteger(1, digestBytes);
                        writer.write(String.format("%064x %s%n", hash, file));
                    } catch (InvalidPathException | IOException | SecurityException e) {
                        writer.write(String.format("%064x %s%n", 0, file));
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Output file not found: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("An error occurred while writing output file: " + e.getMessage());
            } catch (SecurityException e) {
                System.err.println("Failed to get access to output file" + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("An error occurred while reading input file: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Failed to get access to input file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect arguments");
            return;
        }
        new Walk().walk(args[0], args[1]);
    }
}
