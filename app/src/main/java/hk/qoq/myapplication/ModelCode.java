package hk.qoq.myapplication;

/**
 * Created by jordan17 on 2016/9/20.
 */
public enum ModelCode {
    plusBrightBlack128("MN4D2ZP/A"),
     plusBrightBlack256("MN4L2ZP/A"),
     plusBlack128("MN482ZP/A"),
     plusBlack256("MN4E2ZP/A"),
     plusSilver128("MN492ZP/A"),
     plusSilver256("MN4F2ZP/A"),
     plusGold128("MN4J2ZP/A"),
     plusGold256("MN4A2ZP/A"),
     plusPink128("MN4K2ZP/A"),
     plusPink256("MN4C2ZP/A"),
    plusBlack32("MNQH2ZP/A");
    String model;
    String getModel() {return this.model;}
    ModelCode(String model) {
        this.model = model;
    }
}
