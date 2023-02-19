package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class Hashmap implements Serializable {
    private HashMap<String, String> hashMap;
    // Constructor: create empty hashmap
    Hashmap() {
        // Creating an empty HashMap
        hashMap = new HashMap<>();
    }

    //Constructor: create single pair hashmap
    Hashmap(String key, String value) {
        // Creating an empty HashMap
        hashMap = new HashMap<>();

        // Mapping string values to string keys
        hashMap.put(key, value);

    }
    public Set getKeySet() {
        return hashMap.keySet();
    }

    public String getKeyAtValue(String value) {
        Set<String> keySet = this.getKeySet();
        for (String S : keySet) {
            if (this.getValue(S) == value) {
                return S;
            }
        }
        //System.out.println("No key matches this value.");
        return null;
    }

    public String getValue(String key) {
        return hashMap.get(key);
    }

    public void addToHashMap(String key, String value) {
        hashMap.put(key, value);
    }

    public void removeFromHashMap(String key) {
        hashMap.remove(key);
    }



}
