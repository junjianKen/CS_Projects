package gitlet;

import java.io.File;
import java.util.*;
import java.io.Serializable;

import static gitlet.Utils.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.Date;

/**
 * Represents a gitlet commit object.
 *
 * @author Name of author
 * @ Date Date of creation
 * @
 */
public class Commit implements Serializable {

    //public static final File CommitPath = Utils.join(Repository.GITLET_DIR, "commits");;

    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    // How to track the number of commits?
    //private static int commitTracker = 0;


    /**
     * The message of this Commit.
     */
    private String message = null;

    //private HashMap<String, String> [] blobMap;

    private Hashmap blobMap;


    //Keep parent as a string tag instead of the actual commit.
    // Otherwise serialization converts the whole thing.
    private String parent;

    //For now only consider the case of two parents max.
    private String secondParent;
    // SHA1 converted string of this commit
    private String thisCommitTag;
    //Create metadata;
    MetaData metadata;

    // Make sure parent is passed in as a string tag
    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.secondParent = null;
        // The initial hash map is empty.
        blobMap = new Hashmap();
        if (this.parent == null) {
            String timestamp = "Wed Dec 31 16:00:00 1969 -0800";
            String log = null;
            metadata = new MetaData(timestamp, log);
        } else {
            // Date generation


            String pattern = "EEE MMM dd HH:mm:ss yyyy Z";
            DateFormat df = new SimpleDateFormat(pattern);
            Date today = Calendar.getInstance().getTime();
            String timestamp = df.format(today);

            //as a result, the timestamp for the initial
            // commit does not read Thursday, January 1st, 1970, 00:00:00,
            // but rather the equivalent Pacific Standard
            // Time (or Pacific Daylight Time, which is -0700).


            //String timestamp = String.valueOf(Calendar.getInstance().getTime());
            String log = null;
            metadata = new MetaData(timestamp, log);
        }

    }



    // Declare
    public void mapToBlobs(Hashmap stagingBlobMap) {
        //get parent commit content tag as a whole string path.
        File parentSavePath = join(Repository.COMMITFOLDER, parent);
        Commit parentCommit = readObject(parentSavePath, Commit.class);

        // Load parent blob maps.
        Hashmap parentBlobMap = parentCommit.getBlobMap();

        // Get the list of folder names (SERIALIZED) pointed to by the parent commit.
        Set<String> parentBlobFilenames = parentBlobMap.getKeySet();

        //Read HashMap of new blob object created for staging.
        //Hash_Map stagingBlobMap = stagingBlobMap;

        //Load keys of staging blob file names.
        Set<String> stagingBlobFilenames = stagingBlobMap.getKeySet();


        for (String stageFile : stagingBlobFilenames) {
            // Get SHA1 content tag of the new stage file.
            String newStageContent = stagingBlobMap.
                    getValue(stageFile);
            // Check if the parent's commit files already
            // include the new staging files.
            // If yes, point to the different SHA1 tag in
            // the same commit file provided by the staging blob map.
            // Actually existing key will overwrite old value.
            // New keep will be added to hashmap with new value.
            parentBlobMap.addToHashMap(stageFile,
                    newStageContent);
        }

        // If delete do not remove blob. Remove hashmap pointers only.
        //Assign the modified parent blob map to the current commit's blob map.
        blobMap = parentBlobMap;

        // Clear current staging blob map as it is no longer needed

    }

    // Return the blobmap of parent or self.
    public Hashmap getBlobMap() {
        return blobMap;
    }

    public void removeFromBlobMap(String K) {

        blobMap.removeFromHashMap(K);
    }

    //Call in Main or Repository
    public void saveCommit() {


        //if (parent != null){
        //Save static thisCommitTag to this commit's parent commit tag.
        //addParentTag();}
        //Set thisCommitTag as a SHA1 string.
        updateThisTag();
        //Serialize and write entire commit to file.
        //Commit file name is the path + SHA1 tag name.
        File savePath = Utils.join(Repository.COMMITFOLDER, thisCommitTag);
        Utils.writeObject(savePath, this);
    }

    //Add parent tag from the repository Head and Main
    public void addParentTag(String previousCommit) {
        //System.out.println(previousHead);
        // Get parent tag from a static variable in Main?
        parent = previousCommit;
    }

    public void addSecondParent(String previousMergeCommit) {
        secondParent = previousMergeCommit;
    }

    public String getSecondParent() {
        return secondParent;
    }

    // Update the static tag from the repository tag (pointed to by head and Main)
    // to this commit's tag
    public void updateThisTag() {
        //Does the tag include the tag information itself? Likely not.
        //Serialize commit and then write to sha1. Otherwise improper type to sha1

        //for now, use the number of commits as the extra piece of information in tagString
        int numCommit = Repository.COMMITFOLDER.list().length;

        //need all of metadata instead of just timetamp.
        byte[] metadataSerial = serialize(metadata);
        String tagString = sha1(message + numCommit);
        thisCommitTag = tagString;
    }

    public String getThisCommitTag() {
        return thisCommitTag;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return metadata.getTimestamp();
    }

    //Get parent tag
    public String getParent() {
        return parent;
    }


    //Set parent tag
    public void setParent(Repository R) {
        parent = R.getHeadCommitTag();
    }


    //HashMap helper subclass
    //does static work?
    //
    public class MetaData implements Serializable {
        //complete log


        // timestamp should be string?
        private String timestamp;
        private HashMap log;
        //private String message;


        MetaData(String updateTimestamp, String updateLog) {
            timestamp = updateTimestamp;
            //log.addTo_Hash_Map();


        }


        public String getTimestamp() {
            return timestamp;
        }


    }


}
