package gitlet;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;

/** Assorted utilities.
 *
 * Give this file a good read as it provides several useful utility functions
 * to save you some time.
 *
 *  @author P. N. Hilfinger
 */
class Utils {

    /** The length of a complete SHA-1 UID as a hexadecimal numeral. */
    static final int UID_LENGTH = 40;

    /* SHA-1 HASH VALUES. */

    /** Returns the SHA-1 hash of the concatenation of VALS, which may
     *  be any mixture of byte arrays and Strings. */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in
     *  VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp);
        }
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                    new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp);
        }
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Throws IllegalArgumentException in case of problems. */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp);
        }
    }

    /** Write OBJ to FILE. */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* DIRECTORIES */

    /** Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES =
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };




    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }



    /** Filter out all but subdirectories. */
    private static final FilenameFilter SUBDIR =
            new FilenameFilter() {

        // Do not overwrite using fila name filter. Overwrite using DirectoryFilter instead.
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            };


    /** Returns a list of the names of all subdirectories in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> directoryNamesIn(File dir) {
        String[] files = dir.list(SUBDIR);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    static List<String> directoryNamesIn(String dir) {
        return directoryNamesIn(new File(dir));
    }



    /* OTHER FILE UTILITIES */

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths#get(String, String[])}
     *  method. */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths#get(String, String[])}
     *  method. */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES */

    /** Returns a byte array containing the serialized contents of OBJ. */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /** Return a GitletException whose message is composed from MSG and ARGS as
     *  for the String.format method. */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /** Print a message composed from MSG and ARGS as for the String.format
     *  method, followed by a newline. */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    public static boolean writeToFiles(boolean conflict,
                                       List<String> otherMod,
                                       List<String> currentMod,
                                       Hashmap otherFiles,
                                       Hashmap currentFiles,
                                       Commit givenBranch,
                                       Commit currBranch,
                                       Commit splitBranch)
            throws IOException {
        ArrayList<String> combined = new ArrayList<>();
        for (String filename : currentMod) {
            if ((otherMod.contains(filename) // Modified in both
                    && !otherFiles.getValue(filename) //Check tags
                    .equals(currentFiles.getValue(filename)))
                    || !otherMod.contains(filename)) {
                //System.out.println(splitBranch.getBlobMap().getValue(filename));
                //System.out.println(currentFiles.getValue(filename));
                // Check strings directly
                //System.out.println(splitBranch.getBlobMap().getKeySet());
                String splitFileTag = splitBranch.getBlobMap().getValue(filename);
                //String currentFileTag = currentFiles.getValue(filename);
                //String splitStringsFolderPath = filename + "/" + splitFileTag;
                //String currentStringsFolderPath = filename + "/" + currentFileTag;
                //File splitStringsPath = join(Repository.BLOBFOLDER,splitStringsFolderPath);
                //File currentStringsPath = join(Repository.BLOBFOLDER,currentStringsFolderPath);

                //String currentStrings = readContentsAsString(currentStringsPath);
                //String splitStrings = readContentsAsString(splitStringsPath);

                if (otherFiles.getValue(filename) == null) {
                    combined.add(filename);
                    conflict = true;
                    currentModCase(filename, givenBranch,
                            currBranch, splitBranch);

                } else if (!otherFiles.getValue(filename) //Check tags
                    .equals(splitFileTag)) {
                    conflict = true;
                    combined.add(filename);
                    currentModCase(filename, givenBranch,
                            currBranch, splitBranch);
                    //System.out.println(otherMod);
                    //System.out.println(currentMod);

                    //System.out.println("This conflict happened 1.");
                }
            }
        }

        for (String filename : otherMod) {
            if ((!combined.contains(filename)
                    && currentMod.contains(filename)
                    && !otherFiles.getValue(filename)
                    .equals(currentFiles.getValue(filename)))
                    || !currentMod.contains(filename)) {

                if (currentFiles.getValue(filename) == null) {
                    conflict = true;
                    otherModCase(combined, filename, givenBranch,
                            currBranch, splitBranch);
                } else if (!splitBranch.getBlobMap().getValue(filename)
                    .equals(currentFiles.getValue(filename))) {
                    conflict = true;
                    otherModCase(combined, filename, givenBranch,
                            currBranch, splitBranch);

                }

            }
        }
        return conflict;
    }


    public static void currentModCase(String filename, Commit givenBranch,
                                      Commit currBranch, Commit splitBranch) {
        String givFile = givenBranch.getBlobMap().getValue(filename);
        String curFile = currBranch.getBlobMap().getValue(filename);
        String splFile = splitBranch.getBlobMap().getValue(filename);
        String topDiv = "<<<<<<< HEAD\n";
        String realCurFilePath = filename + "/" + curFile;
        File currFile
                = join(Repository.BLOBFOLDER, realCurFilePath);
        String midDiv = "=======\n";
        String realGivFilePath = filename + "/" + givFile;
        File givenFile
                = join(Repository.BLOBFOLDER, realGivFilePath);
        String btmDiv = ">>>>>>>\n";
        String x = "";
        if (currFile.exists()) {
            byte[] readCurrFile = readContents(currFile);
            x = readContentsAsString(currFile);
        }
        String y = "";
        if (givenFile.exists()) {
            y = readContentsAsString(givenFile);
        }
        String total = topDiv + x + midDiv + y + btmDiv;
        File outp = join(Repository.CWD, filename);
        writeContents(outp, total.getBytes());

    }


    public static void otherModCase(List combined, String filename, Commit givenBranch,
                                    Commit currBranch, Commit splitBranch) {
        combined.add(filename);
        String givFile = givenBranch.getBlobMap().getValue(filename);
        String curFile = currBranch.getBlobMap().getValue(filename);
        String splFile = splitBranch.getBlobMap().getValue(filename);
        String topDiv = "<<<<<<< HEAD\n";
        String realCurFilePath = filename + "/" + curFile;
        File currFile
                = join(Repository.BLOBFOLDER, realCurFilePath);
        String midDiv = "=======\n";

        String realGivFilePath = filename + "/" + givFile;
        File givenFile
                = join(Repository.BLOBFOLDER, realGivFilePath);
        String btmDiv = ">>>>>>>\n";
        String x = "";
        if (currFile.exists()) {
            x = readContentsAsString(currFile);
        }
        String y = "";
        if (givenFile.exists()) {
            y = readContentsAsString(givenFile);
        }
        String total = topDiv + x + midDiv + y + btmDiv;
        File outp = new File(Repository.CWD, filename);
        writeContents(outp, total.getBytes());
    }

}
