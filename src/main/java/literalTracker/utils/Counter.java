package literalTracker.utils;

public class Counter {
    private int count = 0;
    public void add(int num){
        count += num;
    }
    public int getCount(){
        return count;
    }
}
