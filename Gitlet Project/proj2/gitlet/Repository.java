package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import static gitlet.Utils.*;
import java.io.IOException;


/** Represents a gitlet repository.
 *  Act as a repository
 *
 *  @author Person
 */
public class Repository implements Serializable {

    // head and branch as string tags to Commits
    private String HEAD;

    private Status status = new Status();
    private Hashmap BRANCHES = new Hashmap();


    //private String Main;
    // For each add comment update the static stageKey to be used in gitlet commit
    //private static String stageKey;
    private Hashmap stagingBlobMap = new Hashmap();

    private Hashmap removalStage = new Hashmap();

    private Hashmap removedFiles = new Hashmap();

    //Add static instance vars for

    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    // Create blob folder
    public static final File BLOBFOLDER = Utils.join(GITLET_DIR, "blobs");
    // Create commit folder
    public static final File COMMITFOLDER = Utils.join(GITLET_DIR, "commit");

    // Create gitlet repository object
    public Repository() {
        //Check if .gitlet already exists
        //create init folder .gitlet
        if (GITLET_DIR.exists()) {
            System.out.print("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        // Create staging area
        //File blobs = new File(".gitlet/blobs");
        BLOBFOLDER.mkdir();
        //Create add and delete areas
        //File stageAdd = new File(".gitlet/staging/stageAdd");
        //stageAdd.mkdir();
        //File stageDelete = new File(".gitlet/staging/stageDelete");
        //stageDelete.mkdir();
        //Create commit folder
        //File commit = new File(".gitlet/commit");
        COMMITFOLDER.mkdir();
        //Create first commit
        Commit firstCommit = new Commit("initial commit", null);
        firstCommit.updateThisTag();
        //Update static currentCommitTag and save current commit
        firstCommit.saveCommit();
        //Point head and main to the first commit
        //System.out.println(firstCommit.getTimeStamp());

        BRANCHES.addToHashMap("main", firstCommit.getThisCommitTag());
        HEAD = "main";
        //HEAD = firstCommit.getThisCommitTag();
    }

    //Create new repo with args as the message

    //Repo does not have to be in argument
    public void repoNewCommit(String message, Repository R) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        if ((stagingBlobMap.getKeySet().isEmpty()) && (removalStage.getKeySet().isEmpty())) {
            System.out.println("No changes added to the commit.");
            return;
        }
        // Create new commit in repo.
        Commit newCommit = new Commit(message, R.getHeadCommitTag());
        //Add parent tag from static currentCommitTag (from the last commit)
        newCommit.updateThisTag();
        newCommit.addParentTag(getHeadCommitTag());
        //System.out.println(newCommit.getTimeStamp());

        newCommit.mapToBlobs(R.getStagingBlobMap());
        //newCommit.setParent(Repo);
        //Save new commit to directory as serializable object.

        // Remove files staged for removal from blob map of this commit.
        Set<String> aSet = removalStage.getKeySet();
        for (String fileToRemove : aSet) {
            if ((!removedFiles.getKeySet().contains(fileToRemove))
                    && (newCommit.getBlobMap().getKeySet().contains(fileToRemove))) {
                removedFiles.addToHashMap(fileToRemove, null);
                newCommit.removeFromBlobMap(fileToRemove);
            }
        }
        // if (!removedFiles.contains(filename)){
        // removedFiles.add(filename);}

        newCommit.saveCommit();


        //Main needs to be any branch indicated by HEAD
        //String currentBranch = BRANCHES.getKeyAtValue(getHeadCommitTag());
        //String currentBranch = HEAD;

        //Overwrite current branch pointer with the new commit tag.
        BRANCHES.addToHashMap(HEAD, newCommit.getThisCommitTag());

        //HEAD = newCommit.getThisCommitTag();

        //Main = newCommit.getThisCommitTag();
        // After commit, reset removal staging in current repo.
        removalStage = new Hashmap();

        // After commit, reset blob mapping in current repo.
        resetStagingBlobMap();

    }

    public void repoNewSecondParentCommit(String message, Repository R,
                                          String secondParent) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        if ((stagingBlobMap.getKeySet().isEmpty()) && (removalStage.getKeySet().isEmpty())) {
            System.out.println("No changes added to the commit.");
            return;
        }
        // Create new commit in repo.
        Commit newCommit = new Commit(message, R.getHeadCommitTag());
        //Add parent tag from static currentCommitTag (from the last commit)
        newCommit.updateThisTag();
        newCommit.addParentTag(getHeadCommitTag());
        newCommit.addSecondParent(secondParent);
        //System.out.println(newCommit.getTimeStamp());

        newCommit.mapToBlobs(R.getStagingBlobMap());
        //newCommit.setParent(Repo);
        //Save new commit to directory as serializable object.

        // Remove files staged for removal from blob map of this commit.
        Set<String> aSet = removalStage.getKeySet();
        for (String fileToRemove : aSet) {
            if ((!removedFiles.getKeySet().contains(fileToRemove))
                    && (newCommit.getBlobMap().getKeySet().contains(fileToRemove))) {
                removedFiles.addToHashMap(fileToRemove, null);
                newCommit.removeFromBlobMap(fileToRemove);
            }
        }
        // if (!removedFiles.contains(filename)){
        // removedFiles.add(filename);}

        newCommit.saveCommit();


        //Main needs to be any branch indicated by HEAD
        //String currentBranch = BRANCHES.getKeyAtValue(getHeadCommitTag());
        //String currentBranch = HEAD;

        //Overwrite current branch pointer with the new commit tag.
        BRANCHES.addToHashMap(HEAD, newCommit.getThisCommitTag());

        //HEAD = newCommit.getThisCommitTag();

        //Main = newCommit.getThisCommitTag();
        // After commit, reset removal staging in current repo.
        removalStage = new Hashmap();

        // After commit, reset blob mapping in current repo.
        resetStagingBlobMap();

    }


    // Get the commit tag from head via branch maps.
    public String getHeadCommitTag() {
        // HEAD below is the name of the branch
        return BRANCHES.getValue(HEAD);

    }


    public void removeFile(String filename) {

        //only remove file. Do not commit.

        //Load current commit
        File commitPath = Utils.join(COMMITFOLDER, getHeadCommitTag());
        Commit readCommit = readObject(commitPath, Commit.class);

        //If the file is neither staged nor tracked by the head commit
        if (!(stagingBlobMap.getKeySet().contains(filename))
                && !(readCommit.getBlobMap().getKeySet().contains(filename))) {
            System.out.print("No reason to remove the file.");
            return;
        }

        //Unstage the file if it is currently staged for addition.

        if (stagingBlobMap.getKeySet().contains(filename)) {
            stagingBlobMap.removeFromHashMap(filename);
        }

        //If the file is tracked in the current commit, stage it for removal
        if (readCommit.getBlobMap().getKeySet().contains(filename)) {
            removalStage.addToHashMap(filename, null);
        }

        //move to commit
        //If the file is tracked in the current commit, stage it for removal
        //if (readCommit.getBlobMap().getKeySet().contains(filename)){

        //    };

        // Locate file in CWD
        File filePathCWD = join(CWD, filename);
        if ((filePathCWD.exists()) && (readCommit.getBlobMap().
                getKeySet().contains(filename))) {
            readCommit.removeFromBlobMap(filename);
            filePathCWD.delete();
        }


    }

    public void newBranch(String branchName) {
        // Get commit tag name from HEAD. HEAD is just a reference to the tag.

        if (BRANCHES.getKeySet().contains(branchName)) {
            System.out.print("A branch with that name already exists.");
            return;
        }

        BRANCHES.addToHashMap(branchName, getHeadCommitTag());
    }

    public void removeBranch(String branchName) {
        //Determine current branch
        //Current commit is HEAD
        //String currentBranch = BRANCHES.getKeyAtValue(HEAD);
        String currentBranch = HEAD;
        if (branchName.compareTo(currentBranch) == 0) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        if (!(BRANCHES.getKeySet().contains(branchName))) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        //Remove key from hash map.
        BRANCHES.removeFromHashMap(branchName);

        //Remove all files tracked by this branch but not the others???


    }

    public void reset(String commitID) {
        // 1. Checks out all the files tracked by the given commit.
        // 2. Removes tracked files that are not present in that commit.
        // 3. Also moves the current branch’s head to that commit node.
        // See the intro for an example of what happens to
        // the head pointer after using reset. The [commit id] may be abbreviated as
        // for checkout.
        // 4.The staging area is cleared. The command is essentially checkout
        // of an arbitrary commit that also changes the current branch head.


        //FAILURE CASES: Similar to CHECKOUT BRANCH.
        // load current commit info.
        File targetCommitPath = join(COMMITFOLDER, commitID);
        if (!targetCommitPath.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit targetCommit = readObject(targetCommitPath,
                Commit.class);
        Set<String> targetCommitTrackedFiles = targetCommit.
                getBlobMap().getKeySet();
        File commitPath = join(COMMITFOLDER,
                getHeadCommitTag()); //list all files tracked in this branch (HEAD)
        Commit readCommit = readObject(commitPath,
                Commit.class); //list all files tracked in this branch (HEAD)
        Set<String> trackedFiles = readCommit.getBlobMap().
                getKeySet(); //list all files tracked in this branch (HEAD)

        //Failure case. Check files that could potentially be overwritten.
        List<String> filesInCWD = plainFilenamesIn(CWD); //list all files in CWD
        //String targetBranchCommitTag = BRANCHES.getValue(branchName);


        for (String f : filesInCWD) { // For all plain files in CWD
            if (!trackedFiles.contains(f)) { //
                // If this is an untracked file, continue
                // checking the target branch.
                if (targetCommitTrackedFiles.contains(f)) {
                    //If target branch contains the untracked file, exit.
                    System.out.print("There is an untracked file in the "
                               + "way; delete it, or add and commit it first.");
                    return;
                    //}
                }
            }
            //Hidden failure case. This was built based on Test38.
            //Ask about this one
            if (removalStage.getKeySet().contains(f)) {
                if (targetCommitTrackedFiles.contains(f)) {
                    System.out.print("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }

            //Any files that are tracked in the current
            // branch but are not present in the checked-out
            // branch are deleted.
            if ((trackedFiles.contains(f))
                    && (!targetCommitTrackedFiles.contains(f))) {
                File thisFileCWDPath = join(CWD, f); //Get this
                // file in CWD for deletion.
                thisFileCWDPath.delete();
            }
            for (String t : targetCommitTrackedFiles) {

                checkOutPrevious(t, commitID);
            }
            //BRANCHES.addTo_Hash_Map(branchName, getHeadCommitTag());
            //Get the branch of commitID (target commit ID)
            String currentBranch = BRANCHES.
                    getKeyAtValue(getHeadCommitTag());
            BRANCHES.addToHashMap(currentBranch, commitID); //
            HEAD = currentBranch;
            //clear staging area

            resetStagingBlobMap();
        }
    }

    public void resetRemovedFiles() {
        removedFiles = new Hashmap();
    }
    public void resetRemovalStage() {
        removalStage = new Hashmap();
    }

    public Hashmap getStagingBlobMap() {
        return stagingBlobMap;
    }

    //Add to staging blob map
    public void addToStagingBlobMap(String key, String value) {
        stagingBlobMap.addToHashMap(key, value);
    }

    public void resetStagingBlobMap() {
        //Empty hash map
        stagingBlobMap = new Hashmap();

    }

    public void printStatus() {
        Status statusToPrint = new Status();

        statusToPrint.updateStatus();

        System.out.println("=== Branches ===");
        Set<String> branchKeys = BRANCHES.getKeySet();

        List<String> arrayBranch = new ArrayList<String>(branchKeys);


        for (String B : arrayBranch) {
            if (B.compareTo(HEAD) == 0) {
                System.out.println("*" + B);
            }
        }
        // Pritn head and others separately so head always appears first.
        for (String C : arrayBranch) {
            if (C.compareTo(HEAD) != 0) {
                System.out.println(C);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        Set<String> stagingBlobKeys = stagingBlobMap.
                getKeySet();

        List<String> arrayStaged = new
                ArrayList<String>(stagingBlobKeys);
        java.util.Collections.sort(arrayStaged);

        for (String S : arrayStaged) {
            System.out.println(S);
        }

        System.out.println();

        System.out.println("=== Removed Files ===");

        //List<String> setList = new ArrayList<>();
        //Set<String> aSet = removalStage.getKeySet();
        //for (String S: aSet) {
        //    setList.add(S);
        //}
        if (!(statusToPrint.removed == null)) {
            java.util.Collections.sort(statusToPrint.removed);
            for (String R : statusToPrint.removed) {
                System.out.println(R);
            }
        }

        System.out.println();


        System.out.println("=== Modifications Not "
                + "Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");

    }


    public Set<String> getRemovalStage() {
        return removalStage.getKeySet();

    }
    public void setRemovalStage() {
        removedFiles = new Hashmap();
    }


    public void dropFromRemovalStage(String item) {
        removalStage.removeFromHashMap(item);
    }


    //create STATUS class and update it in every function.
    public class Status implements Serializable {
        List<String> branches;
        List<String> staged;
        List<String> removed;
        //Modifications Not Staged For Commit
        List<String> MNSFC;
        List<String> untracked;

        public Status() {
            branches = null;
            staged = null;
            removed = null;
            //Modifications Not Staged For Commit
            MNSFC = null;
            untracked = null;
        }

        public void updateStatus() {
            // Reset branches and add items.
            branches = new ArrayList<>();
            Set<String> branchSet = BRANCHES.getKeySet();
            for (String B : branchSet) {
                branches.add(B);
            }

            // Similarly reset staged and append from blob map
            staged = new ArrayList<>();
            Set<String> stageSet = stagingBlobMap.getKeySet();
            for (String S : stageSet) {
                staged.add(S);
            }

            //finish removal first

            removed = new ArrayList<>();
            if (removalStage != null) {
                Set<String> removalSet = removalStage.getKeySet();
                for (String R : removalSet) {
                    removed.add(R);
                }
            }

        }



    }


    public void saveRepository() {
        File savePath = Utils.join(GITLET_DIR, "Repo");
        Utils.writeObject(savePath, this);
    }

    //put log here
    public void printLogInfo(String currentCommitTag) {

        // for now use commit count. But recursion needs to be fixed for merge.

        //int commitCount = new File(String.valueOf(CommitFolder)).list().length;

        File commitPath = Utils.join(COMMITFOLDER,
                currentCommitTag);
        Commit readCommit = readObject(commitPath,
                Commit.class);
        String parentTag = readCommit.getParent();

        if (parentTag == null) {
            System.out.println("===");
            System.out.println("commit "
                    + readCommit.getThisCommitTag());
            System.out.println("Date: "
                    + readCommit.getTimeStamp());
            System.out.println(readCommit.getMessage());
            System.out.println();
            return;
        }
        //Current commit tag is pointed to by head.

        //for (int i = 0; i< commitCount; i++) {
        System.out.println("===");
        System.out.println("commit "
                + readCommit.getThisCommitTag());
        System.out.println("Date: "
                + readCommit.getTimeStamp());
        System.out.println(readCommit.getMessage());
        System.out.println();

        //currentCommitTag = readCommit.getParent();
        //}
        printLogInfo(parentTag);

    }


    public void printGlobalLogInfo() {

        for (String file : COMMITFOLDER.list()) {
            File commitPath = join(COMMITFOLDER, file);
            Commit readCommit = readObject(commitPath,
                    Commit.class);
            //if (readCommit.getParent() == null) {
            System.out.println("===");
            System.out.println("commit "
                    + readCommit.getThisCommitTag());
            System.out.println("Date: "
                    + readCommit.getTimeStamp());
            System.out.println(readCommit.getMessage());
            System.out.println();
            //}
        }
        return;
    }


    public void find(String commitMessage) {

        int count = 0;
        for (String file : COMMITFOLDER.list()) {
            File commitPath = join(COMMITFOLDER, file);
            Commit readCommit = readObject(commitPath,
                    Commit.class);
            if (readCommit.getMessage().
                    compareTo(commitMessage) == 0) {
                System.out.println(readCommit.
                        getThisCommitTag());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit "
                    + "with that message.");
            return;

        }
    }


    public void checkOut(String filename,
                         Commit currentCommit) {


        Hashmap currentBlobMap = currentCommit.
                getBlobMap();

        //If the file does not exist in the previous commit, abort,
        // printing the error message File does not exist in that commit.
        // Do not change the CWD.
        if (!currentBlobMap.getKeySet().
                contains(filename)) {
            System.out.println("File does not "
                    + "exist in that commit.");
            return;
        }

        //Get the unique SHA1 tag from the commit based on the file name.
        String fileTag = currentBlobMap.getValue(filename);
        String realFilePath = filename + "/" + fileTag;
        File fileToReadPath = join(BLOBFOLDER,
                realFilePath);
        if (fileToReadPath.isFile()) {
            String fileRead = readContentsAsString(fileToReadPath);
            File fileToWritePath = join(CWD, filename);
            writeContents(fileToWritePath, fileRead);
        }
    }

    //Takes the version of the file as it exists in the head commit and puts it
    // in the working directory, overwriting the version of the file that’s
    // already there if there is one. The new version of the file is not staged.


    public void checkOutPrevious(String filename,
                                 String prevCommitTag) {

        //Get full commitID from partial commit ID.
        List<String> allCommitID = List.of(COMMITFOLDER.list());
        for (String commitName : allCommitID) {
            if (commitName.contains(prevCommitTag)) {
                prevCommitTag = commitName;
            }
        }
        // Prev commit path based on tag
        File prevCommitPath = join(COMMITFOLDER,
                prevCommitTag);

        //failure case
        if (!prevCommitPath.exists()) {
            System.out.println("No commit with "
                    + "that id exists.");
            return;
        }

        Commit prevCommit = readObject(prevCommitPath,
                Commit.class);

        checkOut(filename, prevCommit);

    }

    public void checkOutBranch(String branchName) {
        //If no branch with that name exists, print No such branch exists.
        // If that branch is the current branch, print No need to checkout the current branch.
        // If a working file is untracked in the current branch and would be
        // overwritten by the checkout, print There is an untracked file in
        // the way; delete it, or add and commit it first. and exit; perform
        // this check before doing anything else. Do not change the CWD.

        if (!(BRANCHES.getKeySet().contains(branchName))) {
            System.out.print("No such branch exists.");
            return;
        }

        //Get current branch from HEAD
        if (branchName.compareTo(HEAD) == 0) {
            System.out.print("No need to checkout the "
                    + "current branch.");
            return;
        }

        //Check files that could potentially be overwritten.
        List<String> filesInCWD = plainFilenamesIn(CWD);
        //list all files in CWD
        File commitPath = join(COMMITFOLDER, getHeadCommitTag());
        //list all files tracked in this branch (HEAD)
        Commit readCommit = readObject(commitPath, Commit.class);
        //list all files tracked in this branch (HEAD)
        Set<String> trackedFiles = readCommit.getBlobMap().
                getKeySet(); //list all files tracked in this branch (HEAD)
        String targetBranchCommitTag = BRANCHES.
                getValue(branchName);
        File targetBranchCommitPath = join(COMMITFOLDER,
                targetBranchCommitTag);
        Commit targetBranchCommit = readObject(targetBranchCommitPath,
                Commit.class);
        Set<String> targetBranchTrackedFiles = targetBranchCommit.
                getBlobMap().getKeySet();

        for (String f : filesInCWD) { // For all files in CWD
            if (!trackedFiles.contains(f)) { // If this is an
                // untracked file, continue checking the target branch.
                if (targetBranchTrackedFiles.contains(f)) {
                    //If target branch contains the untracked file, exit.
                    File thisFileCWDPath = join(CWD, f);
                    //Get this file's SHA1 tag in CWD
                    String thisFileCWDTag = sha1(
                            readContents(thisFileCWDPath));
                    //Get this file's SHA1 tag in CWD
                    if (thisFileCWDTag.compareTo(
                            targetBranchCommit.getBlobMap().
                                    getValue(f)) != 0) {
                        //Check if the content tag of the untracked file is
                        // the same as the file content that the target branch points to.
                        // If, given that all above conditions have been passed,
                        // the target file content is different from the
                        // untracked file content.
                        System.out.print("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                }
            }
            //Any files that are tracked in the current
            // branch but are not present in the checked-out branch are deleted.
            if ((trackedFiles.contains(f))
                    && (!targetBranchTrackedFiles.contains(f))) {
                File thisFileCWDPath = join(CWD, f); //Get this file in CWD for deletion.
                thisFileCWDPath.delete();
            }
        }

        for (String f : targetBranchTrackedFiles) {
            checkOut(f, targetBranchCommit);

        }

        //BRANCHES.addTo_Hash_Map(branchName, getHeadCommitTag());
        HEAD = branchName;
    }


    public void merge(String branchName) throws IOException, ClassNotFoundException {
        {
            File currentCommitPath = new File(COMMITFOLDER, getHeadCommitTag());
            Commit currentCommit = readObject(currentCommitPath, Commit.class);
            if (checkMerge(branchName)) {
                return;
            }
            String splitPoint = findingSplit(branchName);
            if (splitPoint == null) {
                System.out.println("No split found");
                return;
            }
            if (splitPoint.compareTo(BRANCHES.getValue(branchName)) == 0) {
                System.out.println("Given branch is an ancestor of the current branch.");
                return;
            }
            if (splitPoint.compareTo(BRANCHES.getValue(HEAD)) == 0) {
                checkOutBranch(branchName);
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            File givenBranchCommitPath = join(COMMITFOLDER, BRANCHES.getValue(branchName));
            Commit givenBranchCommit = readObject(givenBranchCommitPath, Commit.class);
            File splitCommitPath = join(COMMITFOLDER, splitPoint);
            Commit splitCommit = readObject(splitCommitPath, Commit.class);

            List<String> otherMod = new ArrayList<String>();
            Set<String> currentCommitBlobMap = currentCommit.getBlobMap().getKeySet();
            Set<String> givenBracnCommitBlobMap = givenBranchCommit.getBlobMap().getKeySet();
            Set<String> splitCommitBlobMap = splitCommit.getBlobMap().getKeySet();
            List<String> currentMod = addToCurrentMod(currentCommitBlobMap, currentCommit,
                    splitCommit, splitCommitBlobMap);
            for (String G : givenBracnCommitBlobMap) {
                String givenCommitFileTag = givenBranchCommit.getBlobMap().getValue(G);
                String splitCommitFileTag = splitCommit.getBlobMap().getValue(G);
                if (splitCommitFileTag != null) {
                    if (givenCommitFileTag.compareTo(splitCommitFileTag) != 0
                            && splitCommitBlobMap.contains(G)) {
                        otherMod.add(G);
                    }
                }
            }
            checkOutOperations(givenBranchCommit, splitCommit, currentMod, otherMod, branchName,
                    currentCommitBlobMap, givenBracnCommitBlobMap, splitCommitBlobMap);
            for (String filename : splitCommitBlobMap) {
                if (!givenBracnCommitBlobMap.contains(filename)
                        && currentCommitBlobMap.contains(filename)
                        && !currentMod.contains(filename)) {
                    File temp2 = join(CWD, filename);
                    if (temp2.exists()) {
                        removeFile(filename);
                    }
                }
            }
            for (String filename : splitCommitBlobMap) {
                if (!currentCommitBlobMap.contains(filename)
                        && givenBracnCommitBlobMap.contains(filename)
                        && !otherMod.contains(filename)) {
                    File temp2 = new File(CWD, filename);
                    if (temp2.exists()) {
                        removeFile(filename);
                    }
                }
            }
            boolean isConflict = false;
            isConflict = writeToFiles(isConflict, otherMod, currentMod,
                    givenBranchCommit.getBlobMap(), currentCommit.getBlobMap(),
                    givenBranchCommit, currentCommit, splitCommit);
            repoNewCommit("Merged " + branchName + " into " + HEAD + ".", this);
            File newMergedCommitPath = join(COMMITFOLDER, BRANCHES.getValue(HEAD));
            Commit newMergedCommit = readObject(newMergedCommitPath, Commit.class);
            newMergedCommit.addSecondParent(BRANCHES.getValue(branchName));
            newMergedCommit.saveCommit();
            if (isConflict) {
                System.out.println("Encountered a merge conflict.");
                return;
            }
        }
    }

    public List<String> addToCurrentMod(Set<String> currentCommitBlobMap, Commit currentCommit,
                           Commit splitCommit, Set<String> splitCommitBlobMap) {
        List<String> currentMod = new ArrayList<String>();
        for (String S : currentCommitBlobMap) {
            String thisCommitFileTag = currentCommit.getBlobMap().getValue(S);
            String splitCommitFileTag = splitCommit.getBlobMap().getValue(S);
            if (splitCommitFileTag != null) {
                if (thisCommitFileTag.compareTo(splitCommitFileTag) != 0
                        && splitCommitBlobMap.contains(S)) {
                    currentMod.add(S);
                }
            }
        }
        return currentMod;
    }

    private void checkOutOperations(Commit givenBranchCommit,
                                    Commit splitCommit,
                                   List<String> currentMod, List<String> otherMod,
                                    String branchName,
                                   Set<String> currentCommitBlobMap,
                                   Set<String> givenBracnCommitBlobMap,
                                   Set<String> splitCommitBlobMap) {

        File currentCommitPath = new File(COMMITFOLDER, getHeadCommitTag());
        Commit currentCommit = readObject(currentCommitPath, Commit.class);

        for (String filename : otherMod) {
            if (currentCommit.getBlobMap().getKeySet().contains(filename)
                    && !currentMod.contains(filename)) {
                checkOutPrevious(filename, BRANCHES.getValue(branchName));
            }
        }
        for (String filename : currentMod) {
            if (givenBracnCommitBlobMap.contains(filename)
                    && !otherMod.contains(filename)) {
                checkOutPrevious(filename, getHeadCommitTag());
            }
        }
        for (String filename : currentCommitBlobMap) {
            if (!givenBracnCommitBlobMap.contains(filename)
                    && !splitCommitBlobMap.contains(filename)) {
                checkOutPrevious(filename, getHeadCommitTag());
            }
        }
        for (String filename : givenBracnCommitBlobMap) {
            if (!currentCommitBlobMap.contains(filename)
                    && !splitCommitBlobMap.contains(filename)) {
                checkOutPrevious(filename, BRANCHES.getValue(branchName));
                addToStagingBlobMap(filename, BRANCHES.getValue(branchName));
            }
        }
    }




    private boolean checkMerge(String branchName) {
        File currentCommitPath = new File(
                COMMITFOLDER, getHeadCommitTag());
        Commit currentCommit = readObject(
                currentCommitPath, Commit.class);

        if (!getStagingBlobMap().getKeySet().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!BRANCHES.getKeySet().contains(branchName)) {
            System.out.println("A branch with "
                    + "that name does not exist.");
            return true;
        }
        if (BRANCHES.getKeyAtValue(getHeadCommitTag()).
                    compareTo(branchName) == 0) {
            System.out.println("Cannot merge a "
                    + "branch with itself.");
            return true;
        }
        for (String S : plainFilenamesIn(CWD)) {
            if ((!currentCommit.getBlobMap().
                    getKeySet().contains(S)) //&& !a.equals(".gitignore")
                    //&& !a.equals("proj2.iml")
                    || !getStagingBlobMap().
                    getKeySet().isEmpty()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    private String findingSplit(String branchName) {
        ArrayList<String> thisBranchAncestors = new ArrayList<>();
        ArrayList<String> givenBranchAncestors = new ArrayList<>();
        File someCommitPath = join(GITLET_DIR, getHeadCommitTag());
        String branchCommitPointer = BRANCHES.getValue(HEAD);
        while (branchCommitPointer != null) {
            someCommitPath = join(COMMITFOLDER, branchCommitPointer);
            Commit thisCommit = readObject(someCommitPath,
                    Commit.class);
            thisBranchAncestors.add(thisCommit.
                    getThisCommitTag());
            branchCommitPointer = thisCommit.getParent();
        }
        branchCommitPointer = BRANCHES.getValue(branchName);
        while (branchCommitPointer != null) {
            someCommitPath = join(COMMITFOLDER,
                    branchCommitPointer);
            Commit branchCommit = readObject(
                    someCommitPath, Commit.class);
            givenBranchAncestors.add(
                    branchCommit.getThisCommitTag());
            branchCommitPointer = branchCommit.getParent();
        }
        String splitPoint = null;
        boolean isSplit = false;
        int generationOne = 0;
        for (int i = 0; i < thisBranchAncestors.size()
                && !isSplit; i++) {
            for (int j = 0; j < givenBranchAncestors.
                    size() && !isSplit; j++) {
                if (thisBranchAncestors.get(i).
                        compareTo(givenBranchAncestors.get(j)) == 0) {
                    splitPoint = thisBranchAncestors.get(i);
                    isSplit = true;
                    generationOne = j;
                }
            }
        }
        ArrayList<String> thisBranchOtherAncestors = new ArrayList<>();
        branchCommitPointer = BRANCHES.getValue(HEAD); // Redefine
        while (branchCommitPointer != null) {
            someCommitPath = join(COMMITFOLDER, branchCommitPointer);
            Commit thisCommit = readObject(someCommitPath,
                    Commit.class);
            thisBranchOtherAncestors.add(thisCommit.getThisCommitTag());
            //if (thisCommit.getSecondParent() != null) {
            branchCommitPointer = thisCommit.getSecondParent();
            //} //else {
            //    branchCommitPointer = thisCommit.getParent();
            //}
        }
        //System.out.println(thisBranchAncestors);
        String anotherSplitPoint = null;
        int generationTwo = 0;
        for (int i = 0; i < thisBranchOtherAncestors.size()
                && !isSplit; i++) {
            for (int j = 0; j < givenBranchAncestors.
                    size() && !isSplit; j++) {
                if (thisBranchOtherAncestors.get(i).
                        compareTo(givenBranchAncestors.get(j)) == 0) {
                    anotherSplitPoint = thisBranchOtherAncestors.get(i);
                    isSplit = true;
                    generationTwo = j; // Which generation more recent.
                }
            }
        }
        if ((splitPoint != null) && (anotherSplitPoint == null)) {
            return splitPoint;
        } else if ((splitPoint == null) && (anotherSplitPoint != null)) {
            return anotherSplitPoint;
        } else if ((splitPoint != null) && (anotherSplitPoint != null)) {
            if ((generationTwo > generationOne)) {
                return splitPoint;
            } else {
                return anotherSplitPoint;
            }
        } else {
            return splitPoint;
        }
    }
}


