package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static gitlet.Utils.*;


/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Person
 */
public class Main {

    //Create branch... what is branch Main? There could be multiple branches

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {


        if ((args == null) || (args.length == 0)) {
            System.out.println("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                init();
                break;
            case "add":

                add(args);
                break;
            case "commit":

                commit(args);
                break;
            case "log":
                try {
                    log();
                    break;
                } catch (ParseException e) {
                    throw new SecurityException("hello");
                }
            case "checkout":
                //String secondArg = args[1];
                //if ((args.length = 2) && (secondArg == "--")) {
                checkOut(args);
                break;
            case "branch":
                branch(args);
                break;
            case "rm-branch":
                rmBranch(args);
                break;
            case "reset":
                reset(args);
                break;

            case "rm":
                rm(args);
                break;

            case "status":
                status(args);
                break;
            case "global-log":
                globalLog();
                break;
            case "find":
                find(args);
                break;
            case "merge":
                merge(args);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
            //if ()
        }
    }

    public static void init() {
        Repository R = new Repository();

        R.saveRepository();

        //first commit

        //return argument
    }

    public static void add(String[] fileNames) {
        // Load Repo
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);

        // check if files in CWD
        // Loop over the file names following the add comment.
        //below is the test using just one element.
        String filename = fileNames[1];
        //for (String filename: fileName) {
        if (R.getRemovalStage().contains(fileNames[1])) {
            R.dropFromRemovalStage(filename);
            R.saveRepository();
        }

        Repository R2 = readObject(loadPath, Repository.class);

        // Get the paths of files to be added from the directory.
        File fileToAdd = Utils.join(Repository.CWD, filename);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        //Read file content as bytes
        String fileToAddContent = readContentsAsString(fileToAdd);
        //Get unique SHA1 content tag for the file
        //Experiment to see if reading to byte is needed in the first place to get the tag.
        String contentTag = sha1(fileToAddContent);

        //Create subdirectory in blob folder (staging area) under the filename
        File fileToAddFolderPath = Utils.join(Repository.BLOBFOLDER, filename);
        // Make subdirectory under the added file name if it does not already exist.
        if (!fileToAddFolderPath.exists()) {
            fileToAddFolderPath.mkdir();
        }

        // Check if the same file already exists. If yes then no need to add.
        List allTagsInFolderPath = List.of(fileToAddFolderPath.list());
        if ((fileToAddFolderPath.exists()) && (allTagsInFolderPath.contains(contentTag))) {
            return;
        }
        // Write new file content to the subdirectory as a serializable
        // object. Name it under the SHA1 content tag.
        File fileToAddContentPath = Utils.join(fileToAddFolderPath,
                contentTag);
        // Check SHA1 content tag of the current file to add. If
        // identical tag already in the file, continue and return message.
        // Actually no need to check if identical file already exists.
        // If it does, just overwrite it.
        // Write to blob folder (staging area), under the folder
        // with the same name as the file to add,
        // and named after the unique SHA1 content tag, as a
        // serializable object.
        //Confirm file name is written instad of another subdirectory
        // being created
        writeContents(fileToAddContentPath, fileToAddContent);
        //Repo.resetStagingBlobMap();
        R2.addToStagingBlobMap(filename, contentTag);

        //System.out.println(Repo.getStagingBlobMap().getKeySet());

        //Repo.resetRemovalStage();

        R2.saveRepository();

        //System.out.println(Repo.getStagingBlobMap().getValue("Sammie"));
        //}
        //CONVERT FILE TO ADD TO UNIQUE KEY.
        //Save the key for later commit
        // Set StagingBlobMap

    }

    public static void commit(String[] args) {
        if (args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        //Load Repo
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.repoNewCommit(args[1], R);
        R.saveRepository();
    }

    public static void log() throws ParseException {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.printLogInfo(R.getHeadCommitTag());
    }


    public static void checkOut(String[] args) {

        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);

        if (args.length == 3) {


            //File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
            //Repository Repo = readObject(loadPath, Repository.class);

            //CurrentCommitPath
            String currentCommitTag = R.getHeadCommitTag();
            //String commitFolderPath = args[2] + "/" + currentCommitTag;
            File commitPath = join(Repository.COMMITFOLDER, currentCommitTag);

            Commit readCurrentCommit = readObject(commitPath, Commit.class);

            R.checkOut(args[2], readCurrentCommit);


        } else if (args.length == 4) {
            //Failure case for wrong format.
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }

            //java gitlet.Main checkout [commit id] -- [file name]

            R.checkOutPrevious(args[3], args[1]);
        } else if (args.length == 2) {
            R.checkOutBranch(args[1]);
        }

        R.saveRepository();

    }

    public static void branch(String[] args) {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.newBranch(args[1]);
        R.saveRepository();
    }

    public static void rmBranch(String[] args) {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.removeBranch(args[1]);
        R.saveRepository();

    }

    public static void reset(String[] args) {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.reset(args[1]);
        R.saveRepository();
    }

    public static void rm(String[] args) {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);

        R.removeFile(args[1]);

        R.saveRepository();

    }

    public static void status(String[] args) {
        // For test only. Where should I put this?
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);


        R.printStatus();
        R.saveRepository();

    }


    public static void globalLog() {

        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.printGlobalLogInfo();
        R.saveRepository();

    }


    public static void find(String[] args) {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.find(args[1]);
        R.saveRepository();
    }

    public static void merge(String[] args) throws IOException, ClassNotFoundException {
        File loadPath = Utils.join(Repository.GITLET_DIR, "Repo");
        Repository R = readObject(loadPath, Repository.class);
        R.merge(args[1]);
        R.saveRepository();
    }


}
