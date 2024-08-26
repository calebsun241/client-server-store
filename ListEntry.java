import java.util.*;
/**
 * Project 5 -- ListEntry
 *
 * Description of class
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class ListEntry {
    public ArrayList<String> entry;

    public ListEntry(String... a) {
        entry = new ArrayList<String>(Arrays.asList(a));
    }

    public String get(int index) {
        return entry.get(index);
    }

    public static Comparator<ListEntry> makeComparator(int index) {
        return makeComparator(index, false);
    }

    public static Comparator<ListEntry> makeComparator(int index, boolean isNum) {
        if (isNum) {
        return new Comparator<ListEntry>() {
            @Override
            public int compare(ListEntry e1, ListEntry e2) {
                return Integer.valueOf(Integer.parseInt(e1.entry.get(index))).compareTo(Integer.valueOf(Integer.parseInt(e2.entry.get(index))));
            }
        };
        }
        else {
        return new Comparator<ListEntry>() {
            @Override
            public int compare(ListEntry e1, ListEntry e2) {
                return e1.entry.get(index).compareTo(e2.entry.get(index));
            }
        };
        }
    }

    public String toString() {
        return entry.toString();
    }

    /*
    public static void main(String[] args) {
        ListEntry e = new ListEntry("a", "b", "c");
        ListEntry e2 = new ListEntry("c", "a", "b");
        ArrayList<ListEntry> l = new ArrayList<ListEntry>();
        l.add(e);
        l.add(e2);

        Collections.sort(l, ListEntry.makeComparator(1));
        System.out.println(l);
    }
    */
}
