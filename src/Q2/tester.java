package Q2;

public class tester {
    public static void main(String args[]){
        SegmentedHashMap seg = new SegmentedHashMap(10,10);
        seg.add("Josh", "Moore");
        seg.add("Matt", "Sloan");
        seg.add("Beth", "Mogey");
        seg.add("Harry", "Kane");
        seg.add("James", "Davids");
        seg.add("David", "Cassy");
        seg.add("Hamlet", "McMonroe");

        System.out.println(seg.contains("Josh"));
        System.out.println(seg.contains("Matt"));
        System.out.println(seg.remove("Josh"));
        System.out.println(seg.contains("Josh"));
        System.out.println(seg.get("Josh"));
        System.out.println(seg.get("Matt"));
        System.out.println(seg.debuggingCountElements());








    }
}
