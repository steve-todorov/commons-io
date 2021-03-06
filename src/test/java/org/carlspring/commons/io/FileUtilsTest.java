package org.carlspring.commons.io;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class FileUtilsTest
{

    public static final long SMALL_FILE_SIZE = 8L;

    public static final long LARGE_FILE_SIZE = 128000000L;

    public static final long FILE_SIZE = SMALL_FILE_SIZE;

    public static final Path srcDir = Paths.get("target/test-resources/src/foo").toAbsolutePath();

    public static boolean INITIALIZED = false;


    @Before
    public void setUp()
            throws Exception
    {
        //noinspection ResultOfMethodCallIgnored
        srcDir.toFile().mkdirs();

        mkdirs(srcDir.toFile(), "blah/blahblah", "yadee/boo/hoo");

        // generateTestResource(new File(srcDir.toFile().getAbsolutePath(), "foo/bar.bin"), 128000000L);
        generateTestResource(new File(srcDir.toFile(), "bar.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "blah/blah1.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "blah/blah2.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "blah/blahblah/moreblah1.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "blah/blahblah/moreblah2.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "yadee/yadda1.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "yadee/yadda2.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "yadee/yadda3.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "yadee/boo/hoo1.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "yadee/boo/hoo2.bin"), SMALL_FILE_SIZE);
        generateTestResource(new File(srcDir.toFile(), "yadee/boo/hoo/wow1.bin"), SMALL_FILE_SIZE);
    }

    private void mkdirs(File basedir, String... dirs)
    {
        for (String dir : dirs)
        {
            //noinspection ResultOfMethodCallIgnored
            new File(basedir.getAbsolutePath(), dir).mkdirs();
        }
    }

    private void generateTestResource(File file, long length)
            throws IOException
    {
        RandomInputStream ris = new RandomInputStream(length);
        FileOutputStream fos = new FileOutputStream(file);

        IOUtils.copy(ris, fos);

        ris.close();
        fos.close();
    }

    @Test
    public void testMoveDirectory()
            throws IOException
    {
        Path destDir = Paths.get("target/test-resources/move-directory-dest-non-existent/dest").toAbsolutePath();

        // Prepare the resources:
        //noinspection ResultOfMethodCallIgnored
        destDir.toFile().mkdirs();

        long startTime = System.currentTimeMillis();

        // Execute the actual test:
        FileUtils.moveDirectory(srcDir, destDir);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        File srcFile = new File(srcDir.toFile().getPath(), "bar.bin");
        File destFile = new File(destDir.toFile().getPath(), "bar.bin");

        assertTrue("Failed to move file!", destFile.exists());
        assertTrue("Failed to move file!", !srcFile.exists());

        System.out.println("Successfully performed recursive directory move in " + duration + " ms," +
                           " (where the destination directory does not contain any files in advance).");
    }

    @Test
    public void testMoveDirectoryWhereDestinationExists()
            throws IOException
    {
        Path destDir = Paths.get("target/test-resources/move-directory-dest-contains-foo/foo").toAbsolutePath();

        // Prepare the resources:
        //noinspection ResultOfMethodCallIgnored
        destDir.toFile().mkdirs();

        File barBin = new File(destDir.toFile(), "bar.bin");
        generateTestResource(barBin, 2 * SMALL_FILE_SIZE);

        File fooDir = new File(destDir.toFile().getAbsolutePath(), "blah");
        //noinspection ResultOfMethodCallIgnored
        fooDir.mkdirs();

        long startTime = System.currentTimeMillis();

        // Follow links when copying files
        EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        RecursiveMover mover = new RecursiveMover(srcDir, destDir.getParent());
        Files.walkFileTree(srcDir, options, Integer.MAX_VALUE, mover);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        File srcFile = new File(srcDir.toFile().getPath(), "bar.bin");
        File destFile = new File(destDir.toFile().getPath(), "bar.bin");

        assertTrue("Failed to move file!", destFile.exists());
        assertTrue("Failed to move file!", !srcFile.exists());

        assertEquals("Failed to replace file!", SMALL_FILE_SIZE, barBin.length());

        System.out.println("Successfully performed recursive directory move in " + duration + " ms," +
                           " (where the destination directory contains some files in advance).");
    }

}
